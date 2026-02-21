package com.salah.mcpplayersservice.controllers;

import com.salah.mcpplayersservice.dto.request.RecruitmentNeedRequest;
import com.salah.mcpplayersservice.dto.response.RecruitmentNeedResponseDto;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.services.RecruitmentNeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/recruitment-needs")
@Tag(name = "Recruitment Needs", description = "Team recruitment need endpoints")
@SecurityRequirement(name = "bearerAuth")
public class RecruitmentNeedController {

	private final RecruitmentNeedService recruitmentNeedService;

	public RecruitmentNeedController(RecruitmentNeedService recruitmentNeedService) {
		this.recruitmentNeedService = recruitmentNeedService;
	}

	@Operation(summary = "Create a recruitment need",
			description = "Creates a new recruitment need (TEAM_MANAGER only)")
	@PostMapping
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<?> create(@Valid @RequestBody RecruitmentNeedRequest request,
			@AuthenticationPrincipal User user) {
		Team team = user.getTeam();
		if (team == null) {
			return ResponseEntity.status(403).body(Map.of("message", "You are not associated with a team"));
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(recruitmentNeedService.create(team, request));
	}

	@Operation(summary = "Get recruitment needs by team", description = "Returns recruitment needs for a specific team")
	@GetMapping("/team/{teamId}")
	public ResponseEntity<List<RecruitmentNeedResponseDto>> getByTeam(@PathVariable UUID teamId) {
		return ResponseEntity.ok(recruitmentNeedService.getByTeam(teamId));
	}

	@Operation(summary = "Update a recruitment need", description = "Updates a recruitment need (TEAM_MANAGER only)")
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<?> update(@PathVariable UUID id, @Valid @RequestBody RecruitmentNeedRequest request,
			@AuthenticationPrincipal User user) {
		Team team = user.getTeam();
		if (team == null) {
			return ResponseEntity.status(403).body(Map.of("message", "You are not associated with a team"));
		}
		return ResponseEntity.ok(recruitmentNeedService.update(id, team, request));
	}

	@Operation(summary = "Delete a recruitment need", description = "Deletes a recruitment need (TEAM_MANAGER only)")
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<?> delete(@PathVariable UUID id, @AuthenticationPrincipal User user) {
		Team team = user.getTeam();
		if (team == null) {
			return ResponseEntity.status(403).body(Map.of("message", "You are not associated with a team"));
		}
		recruitmentNeedService.delete(id, team);
		return ResponseEntity.noContent().build();
	}

}
