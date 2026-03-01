package com.salah.mcpplayersservice.controllers;

import com.salah.mcpplayersservice.dto.request.TrialRequest;
import com.salah.mcpplayersservice.dto.response.TrialCandidateResponseDto;
import com.salah.mcpplayersservice.dto.response.TrialResponseDto;
import com.salah.mcpplayersservice.models.Trial;
import com.salah.mcpplayersservice.models.TrialCandidate;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.services.TrialService;
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
@RequestMapping("/api/trials")
@Tag(name = "Trials", description = "Team trial session endpoints")
public class TrialController {

	private final TrialService trialService;

	public TrialController(TrialService trialService) {
		this.trialService = trialService;
	}

	@Operation(summary = "Create a trial", description = "Team manager creates a trial session")
	@PostMapping
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<TrialResponseDto> createTrial(@Valid @RequestBody TrialRequest request,
			@AuthenticationPrincipal User user) {
		Trial trial = trialService.createTrial(request, user);
		return ResponseEntity.status(HttpStatus.CREATED).body(toDto(trial));
	}

	@Operation(summary = "Get all trials", description = "Returns all trials ordered by date")
	@GetMapping
	public ResponseEntity<List<TrialResponseDto>> getAllTrials() {
		return ResponseEntity.ok(trialService.getAllTrials().stream().map(this::toDto).toList());
	}

	@Operation(summary = "Get my team's trials", description = "Returns trials for the current team manager's team")
	@GetMapping("/me")
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<List<TrialResponseDto>> getMyTrials(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(trialService.getMyTrials(user).stream().map(this::toDto).toList());
	}

	@Operation(summary = "Delete a trial")
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<Void> deleteTrial(@PathVariable UUID id, @AuthenticationPrincipal User user) {
		trialService.deleteTrial(id, user);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Apply for a trial", description = "Player applies to a trial session")
	@PostMapping("/{id}/apply")
	@PreAuthorize("hasRole('PLAYER')")
	public ResponseEntity<Void> applyForTrial(@PathVariable UUID id, @AuthenticationPrincipal User user) {
		trialService.applyForTrial(id, user);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@Operation(summary = "Withdraw trial application")
	@DeleteMapping("/{id}/apply")
	@PreAuthorize("hasRole('PLAYER')")
	public ResponseEntity<Void> withdrawApplication(@PathVariable UUID id, @AuthenticationPrincipal User user) {
		trialService.withdrawApplication(id, user);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Get candidates for a trial", description = "Team manager sees who applied")
	@GetMapping("/{id}/candidates")
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<List<TrialCandidateResponseDto>> getCandidates(@PathVariable UUID id,
			@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(trialService.getCandidates(id, user).stream().map(this::toCandidateDto).toList());
	}

	@Operation(summary = "Get applied trial IDs", description = "Player gets all trial IDs they applied to")
	@GetMapping("/my-applications")
	@PreAuthorize("hasRole('PLAYER')")
	public ResponseEntity<List<UUID>> getMyApplications(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(trialService.getAppliedTrialIds(user));
	}

	private TrialResponseDto toDto(Trial trial) {
		return new TrialResponseDto(trial.getTrialId(), trial.getLocation(), trial.getTrialDate(), trial.getPosition(),
				trial.getDescription(), trial.getCreatedAt(), trial.getTeam().getTeamId(),
				trial.getTeam().getTeamName(), trial.getTeam().getLogoUrl(), trial.getCandidates().size());
	}

	private TrialCandidateResponseDto toCandidateDto(TrialCandidate c) {
		return new TrialCandidateResponseDto(c.getCandidateId(), c.getPlayer().getPlayerId(),
				c.getPlayer().getFirstName(), c.getPlayer().getLastName(), c.getPlayer().getPosition(), c.getStatus(),
				c.getAppliedAt());
	}

}
