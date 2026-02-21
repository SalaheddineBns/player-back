package com.salah.mcpplayersservice.controllers;

import com.salah.mcpplayersservice.dto.request.TeamUpdateRequest;
import com.salah.mcpplayersservice.dto.response.TeamOptionResponseDto;
import com.salah.mcpplayersservice.dto.response.TeamPageResponseDto;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.repository.TeamRepository;
import com.salah.mcpplayersservice.repository.UserRepository;
import com.salah.mcpplayersservice.services.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Team endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class TeamController {

	private final TeamService teamService;

	private final TeamRepository teamRepository;

	private final UserRepository userRepository;

	@Value("${media.upload-dir}")
	private String uploadDir;

	@Operation(summary = "Get all teams", description = "Returns all teams for profile dropdown selection")
	@GetMapping
	public ResponseEntity<List<TeamOptionResponseDto>> getAllTeams() {
		return ResponseEntity.ok(teamService.getAllTeamOptions());
	}

	@Operation(summary = "Get team page", description = "Returns team details with publications")
	@GetMapping("/{teamId}")
	public ResponseEntity<TeamPageResponseDto> getTeamPage(@PathVariable UUID teamId) {
		return ResponseEntity.ok(teamService.getTeamPage(teamId));
	}

	@Operation(summary = "Search teams", description = "Search teams by name with pagination")
	@GetMapping("/search")
	public ResponseEntity<Page<TeamOptionResponseDto>> searchTeams(
			@RequestParam(value = "q", defaultValue = "") String query,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		return ResponseEntity.ok(teamService.searchTeams(query, page, size));
	}

	@Operation(summary = "Subscribe to a team", description = "Subscribes the current player to a team")
	@PostMapping("/{teamId}/subscribe")
	public ResponseEntity<?> subscribe(Authentication authentication, @PathVariable UUID teamId) {
		User user = resolveUser(authentication);
		if (user == null || user.getPlayer() == null) {
			return ResponseEntity.status(403).body(Map.of("message", "Only players can subscribe to teams"));
		}
		try {
			teamService.subscribe(user.getPlayer(), teamId);
			return ResponseEntity.ok(Map.of("message", "Subscribed successfully"));
		}
		catch (IllegalStateException ex) {
			return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
		}
	}

	@Operation(summary = "Unsubscribe from a team", description = "Unsubscribes the current player from a team")
	@DeleteMapping("/{teamId}/subscribe")
	public ResponseEntity<?> unsubscribe(Authentication authentication, @PathVariable UUID teamId) {
		User user = resolveUser(authentication);
		if (user == null || user.getPlayer() == null) {
			return ResponseEntity.status(403).body(Map.of("message", "Only players can unsubscribe from teams"));
		}
		try {
			teamService.unsubscribe(user.getPlayer(), teamId);
			return ResponseEntity.ok(Map.of("message", "Unsubscribed successfully"));
		}
		catch (IllegalStateException ex) {
			return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
		}
	}

	@Operation(summary = "Get subscribed team IDs",
			description = "Returns the IDs of all teams the current player is subscribed to")
	@GetMapping("/subscriptions")
	public ResponseEntity<?> getSubscriptions(Authentication authentication) {
		User user = resolveUser(authentication);
		if (user == null || user.getPlayer() == null) {
			return ResponseEntity.status(403).body(Map.of("message", "Only players can view subscriptions"));
		}
		Set<UUID> teamIds = teamService.getSubscribedTeamIds(user.getPlayer());
		return ResponseEntity.ok(teamIds);
	}

	@Operation(summary = "Get team subscribers",
			description = "Returns the list of players subscribed to the current user's team")
	@GetMapping("/me/subscribers")
	public ResponseEntity<?> getSubscribers(Authentication authentication) {
		User user = resolveUser(authentication);
		if (user == null) {
			return ResponseEntity.status(401).build();
		}
		Team team = user.getTeam();
		if (team == null) {
			return ResponseEntity.status(403).body(Map.of("message", "You are not associated with a team"));
		}
		return ResponseEntity.ok(teamService.getSubscribers(team));
	}

	@Operation(summary = "Update team profile", description = "Updates the current user's team profile")
	@PutMapping("/me")
	public ResponseEntity<?> updateTeamProfile(Authentication authentication, @RequestBody TeamUpdateRequest request) {
		User user = resolveUser(authentication);
		if (user == null) {
			return ResponseEntity.status(401).build();
		}
		Team team = user.getTeam();
		if (team == null) {
			return ResponseEntity.status(403).body(Map.of("message", "You are not associated with a team"));
		}
		return ResponseEntity.ok(teamService.updateTeamProfile(team, request));
	}

	@Operation(summary = "Upload team logo", description = "Uploads a logo for the current user's team")
	@PutMapping("/me/logo")
	public ResponseEntity<?> uploadTeamLogo(Authentication authentication, @RequestParam("file") MultipartFile file) {
		User user = resolveUser(authentication);
		if (user == null) {
			return ResponseEntity.status(401).build();
		}
		Team team = user.getTeam();
		if (team == null) {
			return ResponseEntity.status(403).body(Map.of("message", "You are not associated with a team"));
		}

		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			return ResponseEntity.badRequest().body(Map.of("message", "Only image files are allowed"));
		}

		try {
			Path uploadPath = Paths.get(uploadDir, "logos");
			Files.createDirectories(uploadPath);

			String extension = getExtension(file.getOriginalFilename());
			String fileName = "team-" + team.getTeamId() + extension;
			Path filePath = uploadPath.resolve(fileName);
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			String logoUrl = "/api/teams/" + team.getTeamId() + "/logo";
			team.setLogoUrl(logoUrl);
			teamRepository.save(team);

			return ResponseEntity.ok(Map.of("logoUrl", logoUrl));
		}
		catch (IOException ex) {
			return ResponseEntity.internalServerError().body(Map.of("message", "Failed to upload logo"));
		}
	}

	@GetMapping("/{teamId}/logo")
	public ResponseEntity<byte[]> getTeamLogo(@PathVariable UUID teamId) {
		Team team = teamRepository.findById(teamId).orElse(null);
		if (team == null || team.getLogoUrl() == null) {
			return ResponseEntity.notFound().build();
		}

		try {
			Path uploadPath = Paths.get(uploadDir, "logos");
			// Find the file matching team-{id}.*
			Path logoFile = Files.list(uploadPath)
				.filter(p -> p.getFileName().toString().startsWith("team-" + teamId))
				.findFirst()
				.orElse(null);

			if (logoFile == null || !Files.exists(logoFile)) {
				return ResponseEntity.notFound().build();
			}

			byte[] bytes = Files.readAllBytes(logoFile);
			String contentType = Files.probeContentType(logoFile);
			if (contentType == null) {
				contentType = "image/png";
			}

			return ResponseEntity.ok()
				.header("Content-Type", contentType)
				.header("Cache-Control", "max-age=86400")
				.body(bytes);
		}
		catch (IOException ex) {
			return ResponseEntity.internalServerError().build();
		}
	}

	private String getExtension(String filename) {
		if (filename == null) {
			return ".png";
		}
		int dot = filename.lastIndexOf('.');
		return dot >= 0 ? filename.substring(dot) : ".png";
	}

	private User resolveUser(Authentication authentication) {
		if (authentication == null || authentication.getPrincipal() == null) {
			return null;
		}
		Object principal = authentication.getPrincipal();
		String userName;
		if (principal instanceof UserDetails userDetails) {
			userName = userDetails.getUsername();
		}
		else {
			userName = principal.toString();
		}
		return userRepository.findByUserNameWithPlayer(userName).orElse(null);
	}

}
