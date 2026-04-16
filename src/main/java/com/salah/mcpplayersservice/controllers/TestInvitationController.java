package com.salah.mcpplayersservice.controllers;

import com.salah.mcpplayersservice.dto.request.CancellationRequest;
import com.salah.mcpplayersservice.dto.request.InvitationRespondRequest;
import com.salah.mcpplayersservice.dto.request.TestInvitationRequest;
import com.salah.mcpplayersservice.dto.request.TestResultRequest;
import com.salah.mcpplayersservice.dto.response.TestInvitationResponseDto;
import com.salah.mcpplayersservice.dto.response.TestResultResponseDto;
import com.salah.mcpplayersservice.dto.response.TestSlotResponseDto;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.services.TestInvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/test-invitations")
@Tag(name = "Test Invitations", description = "Private test invitation flow (MA-R-1)")
public class TestInvitationController {

	private final TestInvitationService testInvitationService;

	public TestInvitationController(TestInvitationService testInvitationService) {
		this.testInvitationService = testInvitationService;
	}

	@Operation(summary = "Send a test invitation to a player",
			description = "Manager sends a private invitation to a specific player. A message is also sent in the chat.")
	@PostMapping
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<TestInvitationResponseDto> sendInvitation(@Valid @RequestBody TestInvitationRequest request,
			@AuthenticationPrincipal User user) {
		return ResponseEntity.status(HttpStatus.CREATED).body(testInvitationService.sendInvitation(request, user));
	}

	@Operation(summary = "Accept or decline a test invitation",
			description = "Player accepts (creates a TestSlot) or declines the invitation.")
	@PatchMapping("/{id}/respond")
	@PreAuthorize("hasRole('PLAYER')")
	public ResponseEntity<TestInvitationResponseDto> respond(@PathVariable UUID id,
			@Valid @RequestBody InvitationRespondRequest request, @AuthenticationPrincipal User user) {
		return ResponseEntity.ok(testInvitationService.respond(id, request, user));
	}

	@Operation(summary = "Cancel a confirmed test slot",
			description = "Manager or player cancels the slot with a mandatory reason.")
	@PostMapping("/{id}/cancel")
	@PreAuthorize("hasRole('TEAM_MANAGER') or hasRole('PLAYER')")
	public ResponseEntity<TestSlotResponseDto> cancelSlot(@PathVariable UUID id,
			@Valid @RequestBody CancellationRequest request, @AuthenticationPrincipal User user) {
		return ResponseEntity.ok(testInvitationService.cancelSlot(id, request, user));
	}

	@Operation(summary = "Publish test result",
			description = "Manager publishes ACCEPTED / REJECTED / SECOND_CHOICE. If ACCEPTED, required documents are returned.")
	@PostMapping("/{id}/result")
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<TestResultResponseDto> publishResult(@PathVariable UUID id,
			@Valid @RequestBody TestResultRequest request, @AuthenticationPrincipal User user) {
		return ResponseEntity.status(HttpStatus.CREATED).body(testInvitationService.publishResult(id, request, user));
	}

	@Operation(summary = "Get invitations sent by the manager")
	@GetMapping("/sent")
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<List<TestInvitationResponseDto>> getSent(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(testInvitationService.getSentInvitations(user));
	}

	@Operation(summary = "Get invitations received by the player")
	@GetMapping("/received")
	@PreAuthorize("hasRole('PLAYER')")
	public ResponseEntity<List<TestInvitationResponseDto>> getReceived(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(testInvitationService.getReceivedInvitations(user));
	}

}
