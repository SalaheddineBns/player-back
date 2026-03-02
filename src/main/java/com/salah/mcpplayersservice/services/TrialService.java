package com.salah.mcpplayersservice.services;

import com.salah.mcpplayersservice.dto.request.TrialRequest;
import com.salah.mcpplayersservice.dto.response.PlayerApplicationSummaryDto;
import com.salah.mcpplayersservice.models.TrialApplicationStatus;
import com.salah.mcpplayersservice.exceptions.RessourceNotFoundException;
import com.salah.mcpplayersservice.models.*;
import com.salah.mcpplayersservice.notification.NotificationService;
import com.salah.mcpplayersservice.repository.TrialCandidateRepository;
import com.salah.mcpplayersservice.repository.TrialRepository;
import com.salah.mcpplayersservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TrialService {

	private static final Logger log = LoggerFactory.getLogger(TrialService.class);

	private final TrialRepository trialRepository;

	private final TrialCandidateRepository trialCandidateRepository;

	private final NotificationService notificationService;

	private final UserRepository userRepository;

	public TrialService(TrialRepository trialRepository, TrialCandidateRepository trialCandidateRepository,
			NotificationService notificationService, UserRepository userRepository) {
		this.trialRepository = trialRepository;
		this.trialCandidateRepository = trialCandidateRepository;
		this.notificationService = notificationService;
		this.userRepository = userRepository;
	}

	@Transactional
	public Trial createTrial(TrialRequest request, User user) {
		Team team = user.getTeam();
		if (team == null) {
			throw new IllegalStateException("User is not associated with a team");
		}
		Trial trial = Trial.builder()
			.location(request.location())
			.trialDate(request.trialDate())
			.position(request.position())
			.description(request.description())
			.team(team)
			.build();
		return trialRepository.save(trial);
	}

	public Trial getTrialById(UUID trialId) {
		return trialRepository.findById(trialId)
			.orElseThrow(() -> new RessourceNotFoundException("Trial", "id", trialId));
	}

	public List<Trial> getAllTrials() {
		return trialRepository.findAllByOrderByTrialDateAsc();
	}

	public List<Trial> getMyTrials(User user) {
		Team team = user.getTeam();
		if (team == null) {
			throw new IllegalStateException("User is not associated with a team");
		}
		return trialRepository.findByTeamTeamIdOrderByTrialDateAsc(team.getTeamId());
	}

	@Transactional
	public void deleteTrial(UUID trialId, User user) {
		Trial trial = trialRepository.findById(trialId)
			.orElseThrow(() -> new RessourceNotFoundException("Trial", "id", trialId));
		Team team = user.getTeam();
		if (team == null || !team.getTeamId().equals(trial.getTeam().getTeamId())) {
			throw new IllegalStateException("You can only delete your own team's trials");
		}
		trialRepository.delete(trial);
	}

	@Transactional
	public TrialCandidate applyForTrial(UUID trialId, User user) {
		Trial trial = trialRepository.findById(trialId)
			.orElseThrow(() -> new RessourceNotFoundException("Trial", "id", trialId));
		Player player = user.getPlayer();
		if (player == null) {
			throw new IllegalStateException("User is not a player");
		}
		if (trialCandidateRepository.findByTrialTrialIdAndPlayerPlayerId(trialId, player.getPlayerId()).isPresent()) {
			throw new IllegalStateException("Already applied to this trial");
		}
		TrialCandidate candidate = new TrialCandidate();
		candidate.setTrial(trial);
		candidate.setPlayer(player);
		candidate.setStatus(TrialApplicationStatus.PENDING);
		TrialCandidate saved = trialCandidateRepository.save(candidate);

		// Notify the team manager
		Team team = trial.getTeam();
		log.info("[applyForTrial] teamId={} teamName={}", team.getTeamId(), team.getTeamName());
		User manager = team.getUser();
		if (manager == null) {
			manager = userRepository.findByPlayerTeamAndRole(team.getTeamId(), Role.TEAM_MANAGER).orElse(null);
			log.info("[applyForTrial] team.getUser() was null, fallback lookup => manager={}",
					manager != null ? manager.getUserId() : "NOT FOUND");
		}
		else {
			log.info("[applyForTrial] manager={}", manager.getUserId());
		}
		if (manager != null) {
			String playerName = player.getFirstName() + " " + player.getLastName();
			log.info("[applyForTrial] creating notification for manager={} playerName={}", manager.getUserId(),
					playerName);
			notificationService.createNotification(manager, playerName, trial.getLocation(), trial.getTrialDate(),
					trial.getTrialId().toString());
			log.info("[applyForTrial] notification saved OK");
		}
		else {
			log.warn("[applyForTrial] no manager found for team={}, notification skipped", team.getTeamId());
		}

		return saved;
	}

	@Transactional
	public void withdrawApplication(UUID trialId, User user) {
		Player player = user.getPlayer();
		if (player == null) {
			throw new IllegalStateException("User is not a player");
		}
		TrialCandidate candidate = trialCandidateRepository
			.findByTrialTrialIdAndPlayerPlayerId(trialId, player.getPlayerId())
			.orElseThrow(() -> new RessourceNotFoundException("Application", "trial", trialId));
		trialCandidateRepository.delete(candidate);
	}

	public List<TrialCandidate> getCandidates(UUID trialId, User user) {
		Trial trial = trialRepository.findById(trialId)
			.orElseThrow(() -> new RessourceNotFoundException("Trial", "id", trialId));
		Team team = user.getTeam();
		if (team == null || !team.getTeamId().equals(trial.getTeam().getTeamId())) {
			throw new IllegalStateException("You can only view candidates for your own trials");
		}
		return trialCandidateRepository.findByTrialTrialId(trialId);
	}

	@Transactional
	public TrialCandidate updateCandidateStatus(UUID trialId, UUID candidateId, TrialApplicationStatus newStatus,
			String message, User user) {
		Trial trial = trialRepository.findById(trialId)
			.orElseThrow(() -> new RessourceNotFoundException("Trial", "id", trialId));
		Team team = user.getTeam();
		if (team == null || !team.getTeamId().equals(trial.getTeam().getTeamId())) {
			throw new IllegalStateException("You can only manage candidates for your own trials");
		}
		TrialCandidate candidate = trialCandidateRepository.findById(candidateId)
			.orElseThrow(() -> new RessourceNotFoundException("Candidate", "id", candidateId));

		TrialApplicationStatus previousStatus = candidate.getStatus();
		candidate.setStatus(newStatus);
		TrialCandidate saved = trialCandidateRepository.save(candidate);

		// Notify the player about the status change (only when status actually changes)
		if (!newStatus.equals(previousStatus)) {
			User playerUser = userRepository.findByPlayerId(candidate.getPlayer().getPlayerId()).orElse(null);
			if (playerUser != null) {
				String trimmedMessage = (message != null && !message.isBlank()) ? message.trim() : null;
				notificationService.createStatusChangeNotification(playerUser, trial.getLocation(),
						trial.getTrialDate(), trial.getTrialId().toString(), newStatus.name(), trimmedMessage);
				log.info("[updateCandidateStatus] status change notification sent to player={}",
						playerUser.getUserId());
			}
		}

		return saved;
	}

	@Transactional
	public TrialCandidate updateCandidateNotes(UUID trialId, UUID candidateId, String notes, User user) {
		Trial trial = trialRepository.findById(trialId)
			.orElseThrow(() -> new RessourceNotFoundException("Trial", "id", trialId));
		Team team = user.getTeam();
		if (team == null || !team.getTeamId().equals(trial.getTeam().getTeamId())) {
			throw new IllegalStateException("You can only manage candidates for your own trials");
		}
		TrialCandidate candidate = trialCandidateRepository.findById(candidateId)
			.orElseThrow(() -> new RessourceNotFoundException("Candidate", "id", candidateId));
		candidate.setNotes((notes != null && !notes.isBlank()) ? notes.trim() : null);
		return trialCandidateRepository.save(candidate);
	}

	public List<PlayerApplicationSummaryDto> getMyApplications(User user) {
		Player player = user.getPlayer();
		if (player == null) {
			throw new IllegalStateException("User is not a player");
		}
		return trialCandidateRepository.findByPlayerPlayerId(player.getPlayerId())
			.stream()
			.map(c -> new PlayerApplicationSummaryDto(c.getTrial().getTrialId(), c.getCandidateId(), c.getStatus(),
					c.getAppliedAt(), c.getStatusUpdatedAt()))
			.toList();
	}

}
