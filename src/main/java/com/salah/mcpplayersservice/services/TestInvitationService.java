package com.salah.mcpplayersservice.services;

import com.salah.mcpplayersservice.dto.request.CancellationRequest;
import com.salah.mcpplayersservice.dto.request.InvitationRespondRequest;
import com.salah.mcpplayersservice.dto.request.TestInvitationRequest;
import com.salah.mcpplayersservice.dto.request.TestResultRequest;
import com.salah.mcpplayersservice.dto.response.RequiredDocumentResponseDto;
import com.salah.mcpplayersservice.dto.response.TestInvitationResponseDto;
import com.salah.mcpplayersservice.dto.response.TestResultResponseDto;
import com.salah.mcpplayersservice.dto.response.TestSlotResponseDto;
import com.salah.mcpplayersservice.exceptions.RessourceNotFoundException;
import com.salah.mcpplayersservice.models.*;
import com.salah.mcpplayersservice.notification.NotificationService;
import com.salah.mcpplayersservice.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TestInvitationService {

	private static final Logger log = LoggerFactory.getLogger(TestInvitationService.class);

	private final TestInvitationRepository invitationRepository;

	private final TestSlotRepository slotRepository;

	private final TestResultRepository resultRepository;

	private final RequiredDocumentRepository documentRepository;

	private final UserRepository userRepository;

	private final MessageService messageService;

	private final NotificationService notificationService;

	public TestInvitationService(TestInvitationRepository invitationRepository, TestSlotRepository slotRepository,
			TestResultRepository resultRepository, RequiredDocumentRepository documentRepository,
			UserRepository userRepository, MessageService messageService, NotificationService notificationService) {
		this.invitationRepository = invitationRepository;
		this.slotRepository = slotRepository;
		this.resultRepository = resultRepository;
		this.documentRepository = documentRepository;
		this.userRepository = userRepository;
		this.messageService = messageService;
		this.notificationService = notificationService;
	}

	// ── Manager: send invitation ──────────────────────────────────────────────

	@Transactional
	public TestInvitationResponseDto sendInvitation(TestInvitationRequest request, User manager) {
		Team team = manager.getTeam();
		if (team == null) {
			throw new IllegalStateException("You must be associated with a team to send test invitations");
		}

		User playerUser = userRepository.findById(request.playerUserId())
			.orElseThrow(() -> new RessourceNotFoundException("User", "id", request.playerUserId()));
		if (playerUser.getRole() != Role.PLAYER) {
			throw new IllegalStateException("Target user is not a player");
		}

		// Prevent duplicate pending invitation to the same player
		invitationRepository.findByManagerAndPlayerAndStatus(manager, playerUser, TestInvitationStatus.PENDING)
			.ifPresent(existing -> {
				throw new IllegalStateException("A pending invitation already exists for this player");
			});

		TestInvitation invitation = TestInvitation.builder()
			.manager(manager)
			.player(playerUser)
			.proposedDate(request.proposedDate())
			.message(request.message())
			.status(TestInvitationStatus.PENDING)
			.build();
		invitation = invitationRepository.save(invitation);

		// Send a message in the conversation so the player sees it in their chat
		String msgContent = buildInvitationMessageContent(team.getTeamName(), request.proposedDate(),
				request.message());
		messageService.sendMessage(manager, playerUser.getUserId(), msgContent);

		// Notify the player
		notificationService.createTestInvitationReceivedNotification(playerUser, team.getTeamName(),
				manager.getUsername(), request.proposedDate(), invitation.getInvitationId().toString());

		log.info("[sendInvitation] manager={} -> player={} invitationId={}", manager.getUserId(),
				playerUser.getUserId(), invitation.getInvitationId());
		return toDto(invitation);
	}

	// ── Player: accept or decline ─────────────────────────────────────────────

	@Transactional
	public TestInvitationResponseDto respond(UUID invitationId, InvitationRespondRequest request, User player) {
		TestInvitation invitation = invitationRepository.findById(invitationId)
			.orElseThrow(() -> new RessourceNotFoundException("TestInvitation", "id", invitationId));

		if (!invitation.getPlayer().getUserId().equals(player.getUserId())) {
			throw new IllegalStateException("This invitation was not sent to you");
		}
		if (invitation.getStatus() != TestInvitationStatus.PENDING) {
			throw new IllegalStateException("Invitation is no longer pending");
		}

		invitation.setRespondedAt(LocalDateTime.now());
		User manager = invitation.getManager();
		Team team = manager.getTeam();
		String playerFullName = player.getFirstName() + " " + player.getLastName();

		if (Boolean.TRUE.equals(request.accepted())) {
			invitation.setStatus(TestInvitationStatus.ACCEPTED);
			invitationRepository.save(invitation);

			// Create the test slot
			TestSlot slot = TestSlot.builder()
				.invitation(invitation)
				.scheduledDate(invitation.getProposedDate())
				.status(TestSlotStatus.SCHEDULED)
				.build();
			slotRepository.save(slot);

			// Notify manager
			notificationService.createTestInvitationAcceptedNotification(manager, playerFullName,
					invitation.getProposedDate(), invitationId.toString());

			// Confirm in chat
			String confirmMsg = buildAcceptMessageContent(playerFullName, invitation.getProposedDate());
			messageService.sendMessage(player, manager.getUserId(), confirmMsg);

			log.info("[respond] player={} ACCEPTED invitationId={} slotId={}", player.getUserId(), invitationId,
					slot.getSlotId());
		}
		else {
			invitation.setStatus(TestInvitationStatus.DECLINED);
			invitationRepository.save(invitation);

			// Notify manager
			notificationService.createTestInvitationDeclinedNotification(manager, playerFullName,
					invitationId.toString());

			// Inform in chat
			String declineMsg = playerFullName + " a décliné votre invitation au test.";
			messageService.sendMessage(player, manager.getUserId(), declineMsg);

			log.info("[respond] player={} DECLINED invitationId={}", player.getUserId(), invitationId);
		}

		return toDto(invitationRepository.findById(invitationId).orElseThrow());
	}

	// ── Cancel (manager or player) ────────────────────────────────────────────

	@Transactional
	public TestSlotResponseDto cancelSlot(UUID invitationId, CancellationRequest request, User caller) {
		TestInvitation invitation = invitationRepository.findById(invitationId)
			.orElseThrow(() -> new RessourceNotFoundException("TestInvitation", "id", invitationId));

		TestSlot slot = slotRepository.findByInvitationInvitationId(invitationId)
			.orElseThrow(() -> new RessourceNotFoundException("TestSlot", "invitationId", invitationId));

		if (slot.getStatus() != TestSlotStatus.SCHEDULED) {
			throw new IllegalStateException("Only SCHEDULED slots can be cancelled");
		}

		boolean isManager = invitation.getManager().getUserId().equals(caller.getUserId());
		boolean isPlayer = invitation.getPlayer().getUserId().equals(caller.getUserId());
		if (!isManager && !isPlayer) {
			throw new IllegalStateException("You are not a participant of this test slot");
		}

		slot.setCancellationReason(request.reason());
		slot.setCancelledBy(caller);
		slot.setCancelledAt(LocalDateTime.now());

		Team team = invitation.getManager().getTeam();
		String playerFullName = invitation.getPlayer().getFirstName() + " " + invitation.getPlayer().getLastName();

		if (isManager) {
			slot.setStatus(TestSlotStatus.CANCELLED_BY_MANAGER);
			slotRepository.save(slot);
			notificationService.createSlotCancelledByManagerNotification(invitation.getPlayer(),
					team != null ? team.getTeamName() : "l'équipe", slot.getScheduledDate(), invitationId.toString(),
					request.reason());
			String msg = "Le test du " + slot.getScheduledDate().toLocalDate()
					+ " a été annulé par le manager. Raison : " + request.reason();
			messageService.sendMessage(caller, invitation.getPlayer().getUserId(), msg);
		}
		else {
			slot.setStatus(TestSlotStatus.CANCELLED_BY_PLAYER);
			slotRepository.save(slot);
			notificationService.createSlotCancelledByPlayerNotification(invitation.getManager(), playerFullName,
					slot.getScheduledDate(), invitationId.toString(), request.reason());
			String msg = playerFullName + " a annulé le test du " + slot.getScheduledDate().toLocalDate()
					+ ". Raison : " + request.reason();
			messageService.sendMessage(caller, invitation.getManager().getUserId(), msg);
		}

		log.info("[cancelSlot] slotId={} cancelledBy={} status={}", slot.getSlotId(), caller.getUserId(),
				slot.getStatus());
		return toSlotDto(slot);
	}

	// ── Manager: publish result ───────────────────────────────────────────────

	@Transactional
	public TestResultResponseDto publishResult(UUID invitationId, TestResultRequest request, User manager) {
		TestInvitation invitation = invitationRepository.findById(invitationId)
			.orElseThrow(() -> new RessourceNotFoundException("TestInvitation", "id", invitationId));

		if (!invitation.getManager().getUserId().equals(manager.getUserId())) {
			throw new IllegalStateException("You can only publish results for your own invitations");
		}

		TestSlot slot = slotRepository.findByInvitationInvitationId(invitationId)
			.orElseThrow(() -> new RessourceNotFoundException("TestSlot", "invitationId", invitationId));

		if (slot.getStatus() != TestSlotStatus.SCHEDULED && slot.getStatus() != TestSlotStatus.COMPLETED) {
			throw new IllegalStateException("Cannot publish result for a cancelled slot");
		}
		if (slot.getResult() != null) {
			throw new IllegalStateException("Result already published for this slot");
		}

		// Mark slot as completed
		slot.setStatus(TestSlotStatus.COMPLETED);
		slotRepository.save(slot);

		TestResult result = TestResult.builder().slot(slot).status(request.status()).notes(request.notes()).build();
		resultRepository.save(result);

		Team team = manager.getTeam();
		String teamName = team != null ? team.getTeamName() : "l'équipe";

		// Notify the player
		notificationService.createTestResultPublishedNotification(invitation.getPlayer(), teamName,
				request.status().name(), invitationId.toString());

		// Send result message in chat
		String msg = buildResultMessageContent(teamName, request.status(), request.notes());
		messageService.sendMessage(manager, invitation.getPlayer().getUserId(), msg);

		log.info("[publishResult] slotId={} result={}", slot.getSlotId(), request.status());

		List<RequiredDocumentResponseDto> docs = request.status() == TestResultStatus.ACCEPTED
				? documentRepository.findAll()
					.stream()
					.map(d -> new RequiredDocumentResponseDto(d.getDocumentId(), d.getTitle(), d.getDescription()))
					.toList()
				: List.of();

		return new TestResultResponseDto(result.getResultId(), result.getStatus(), result.getNotes(),
				result.getPublishedAt(), docs);
	}

	// ── Queries ───────────────────────────────────────────────────────────────

	public List<TestInvitationResponseDto> getSentInvitations(User manager) {
		return invitationRepository.findByManagerOrderByCreatedAtDesc(manager).stream().map(this::toDto).toList();
	}

	public List<TestInvitationResponseDto> getReceivedInvitations(User player) {
		return invitationRepository.findByPlayerOrderByCreatedAtDesc(player).stream().map(this::toDto).toList();
	}

	// ── Mapping helpers ───────────────────────────────────────────────────────

	TestInvitationResponseDto toDto(TestInvitation inv) {
		Team team = inv.getManager().getTeam();
		User invPlayer = inv.getPlayer();
		return new TestInvitationResponseDto(inv.getInvitationId(), inv.getManager().getUserId(),
				team != null ? team.getTeamName() : null, team != null ? team.getLogoUrl() : null,
				invPlayer.getUserId(), invPlayer.getFirstName(), invPlayer.getLastName(), inv.getProposedDate(),
				inv.getMessage(), inv.getStatus(), inv.getCreatedAt(), inv.getRespondedAt(),
				inv.getSlot() != null ? toSlotDto(inv.getSlot()) : null);
	}

	private TestSlotResponseDto toSlotDto(TestSlot slot) {
		return new TestSlotResponseDto(slot.getSlotId(), slot.getInvitation().getInvitationId(),
				slot.getScheduledDate(), slot.getStatus(), slot.getCancellationReason(), slot.getCancelledAt(),
				slot.getCreatedAt(), slot.getResult() != null ? toResultDto(slot.getResult()) : null);
	}

	private TestResultResponseDto toResultDto(TestResult result) {
		List<RequiredDocumentResponseDto> docs = result.getStatus() == TestResultStatus.ACCEPTED
				? documentRepository.findAll()
					.stream()
					.map(d -> new RequiredDocumentResponseDto(d.getDocumentId(), d.getTitle(), d.getDescription()))
					.toList()
				: List.of();
		return new TestResultResponseDto(result.getResultId(), result.getStatus(), result.getNotes(),
				result.getPublishedAt(), docs);
	}

	private String buildInvitationMessageContent(String teamName, LocalDateTime date, String message) {
		StringBuilder sb = new StringBuilder();
		sb.append("Bonjour, je vous invite à passer un test avec ").append(teamName).append(".\n");
		sb.append("Date proposée : ").append(date.toLocalDate()).append(" à ").append(date.toLocalTime()).append(".\n");
		if (message != null && !message.isBlank()) {
			sb.append("Message : ").append(message);
		}
		return sb.toString();
	}

	private String buildAcceptMessageContent(String playerName, LocalDateTime date) {
		return playerName + " a accepté votre invitation. Le test est confirmé pour le " + date.toLocalDate() + " à "
				+ date.toLocalTime() + ".";
	}

	private String buildResultMessageContent(String teamName, TestResultStatus status, String notes) {
		String statusLabel = switch (status) {
			case ACCEPTED -> "Accepté";
			case REJECTED -> "Non retenu";
			case SECOND_CHOICE -> "Second choix";
		};
		StringBuilder sb = new StringBuilder();
		sb.append(teamName).append(" a publié le résultat de votre test : ").append(statusLabel).append(".");
		if (notes != null && !notes.isBlank()) {
			sb.append("\nNote : ").append(notes);
		}
		return sb.toString();
	}

}
