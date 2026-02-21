package com.salah.mcpplayersservice.controllers;

import com.salah.mcpplayersservice.dto.request.PlayerUpdateRequest;
import com.salah.mcpplayersservice.dto.response.ErrorResponseDto;
import com.salah.mcpplayersservice.dto.response.PlayerResponseDto;
import com.salah.mcpplayersservice.exceptions.RessourceNotFoundException;
import com.salah.mcpplayersservice.mappers.PlayerMapper;
import com.salah.mcpplayersservice.models.Player;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.repository.PlayerRepository;
import com.salah.mcpplayersservice.repository.TeamRepository;
import com.salah.mcpplayersservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/players")
@Tag(name = "Players", description = "Player profile endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PlayerController {

	private final PlayerMapper playerMapper;

	private final PlayerRepository playerRepository;

	private final TeamRepository teamRepository;

	private final UserRepository userRepository;

	@Value("${media.upload-dir}")
	private String uploadDir;

	public PlayerController(PlayerMapper playerMapper, PlayerRepository playerRepository, TeamRepository teamRepository,
			UserRepository userRepository) {
		this.playerMapper = playerMapper;
		this.playerRepository = playerRepository;
		this.teamRepository = teamRepository;
		this.userRepository = userRepository;
	}

	@Operation(summary = "Get available players", description = "Returns players that are not assigned to any team")
	@ApiResponse(responseCode = "200", description = "List returned successfully")
	@GetMapping("/available")
	public ResponseEntity<List<PlayerResponseDto>> getAvailablePlayers() {
		List<PlayerResponseDto> available = playerRepository.findByTeamIsNull()
			.stream()
			.map(player -> playerMapper.toPlayerResponseDto(player, player.getUser()))
			.toList();
		return ResponseEntity.ok(available);
	}

	@Operation(summary = "Get current player profile",
			description = "Returns the profile of the currently authenticated player")
	@ApiResponse(responseCode = "200", description = "Profile returned successfully",
			content = @Content(schema = @Schema(implementation = PlayerResponseDto.class)))
	@ApiResponse(responseCode = "401", description = "Not authenticated",
			content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
	@ApiResponse(responseCode = "403", description = "User is not a player",
			content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
	@GetMapping("/me")
	public ResponseEntity<PlayerResponseDto> getMyProfile(Authentication authentication) {
		User user = resolveCurrentUser(authentication);
		if (user == null) {
			return ResponseEntity.status(401).build();
		}
		if (user.getPlayer() == null) {
			return ResponseEntity.status(403).build();
		}
		return ResponseEntity.ok(playerMapper.toPlayerResponseDto(user.getPlayer(), user));
	}

	@Operation(summary = "Update current player profile",
			description = "Updates the profile of the currently authenticated player")
	@ApiResponse(responseCode = "200", description = "Profile updated successfully",
			content = @Content(schema = @Schema(implementation = PlayerResponseDto.class)))
	@ApiResponse(responseCode = "403", description = "User is not a player",
			content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
	@PutMapping("/me")
	public ResponseEntity<PlayerResponseDto> updateMyProfile(Authentication authentication,
			@RequestBody PlayerUpdateRequest request) {
		User user = resolveCurrentUser(authentication);
		if (user == null) {
			return ResponseEntity.status(401).build();
		}
		if (user.getPlayer() == null) {
			return ResponseEntity.status(403).build();
		}

		Player player = user.getPlayer();
		if (request.firstName() != null) {
			player.setFirstName(request.firstName());
		}
		if (request.lastName() != null) {
			player.setLastName(request.lastName());
		}
		if (request.gender() != null) {
			player.setGender(request.gender());
		}
		if (request.position() != null) {
			player.setPosition(request.position());
		}
		if (request.nationality() != null) {
			player.setNationality(request.nationality());
		}
		if (request.preferredLeg() != null) {
			player.setPreferredLeg(request.preferredLeg());
		}
		if (request.preferredNumber() != null) {
			player.setPreferredNumber(request.preferredNumber());
		}
		if (request.teamId() != null) {
			Team selectedTeam = teamRepository.findById(request.teamId())
				.orElseThrow(() -> new RessourceNotFoundException("Team", "id", request.teamId()));
			player.setTeam(selectedTeam);
		}

		playerRepository.save(player);

		return ResponseEntity.ok(playerMapper.toPlayerResponseDto(player, user));
	}

	@Operation(summary = "Upload profile picture", description = "Uploads a profile picture for the current player")
	@PutMapping("/me/picture")
	public ResponseEntity<?> uploadProfilePicture(Authentication authentication,
			@RequestParam("file") MultipartFile file) {
		User user = resolveCurrentUser(authentication);
		if (user == null || user.getPlayer() == null) {
			return ResponseEntity.status(403).build();
		}

		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			return ResponseEntity.badRequest().body(Map.of("message", "Only image files are allowed"));
		}

		try {
			Path uploadPath = Paths.get(uploadDir, "profiles");
			Files.createDirectories(uploadPath);

			String extension = getExtension(file.getOriginalFilename());
			String fileName = "player-" + user.getPlayer().getPlayerId() + extension;
			Path filePath = uploadPath.resolve(fileName);
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			String pictureUrl = "/api/players/" + user.getPlayer().getPlayerId() + "/picture";
			user.getPlayer().setProfilePictureUrl(pictureUrl);
			playerRepository.save(user.getPlayer());

			return ResponseEntity.ok(Map.of("profilePictureUrl", pictureUrl));
		}
		catch (IOException ex) {
			return ResponseEntity.internalServerError().body(Map.of("message", "Failed to upload picture"));
		}
	}

	@GetMapping("/{playerId}/picture")
	public ResponseEntity<byte[]> getProfilePicture(@PathVariable UUID playerId) {
		Player player = playerRepository.findById(playerId).orElse(null);
		if (player == null || player.getProfilePictureUrl() == null) {
			return ResponseEntity.notFound().build();
		}

		try {
			Path uploadPath = Paths.get(uploadDir, "profiles");
			Path pictureFile = Files.list(uploadPath)
				.filter(p -> p.getFileName().toString().startsWith("player-" + playerId))
				.findFirst()
				.orElse(null);

			if (pictureFile == null || !Files.exists(pictureFile)) {
				return ResponseEntity.notFound().build();
			}

			byte[] bytes = Files.readAllBytes(pictureFile);
			String type = Files.probeContentType(pictureFile);
			if (type == null) {
				type = "image/png";
			}

			return ResponseEntity.ok()
				.header("Content-Type", type)
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

	private User resolveCurrentUser(Authentication authentication) {
		if (authentication == null || authentication.getPrincipal() == null) {
			return null;
		}

		String userName;
		Object principal = authentication.getPrincipal();
		if (principal instanceof User userPrincipal) {
			return userPrincipal;
		}
		if (principal instanceof UserDetails userDetails) {
			userName = userDetails.getUsername();
		}
		else {
			userName = principal.toString();
		}

		return userRepository.findByUserNameWithPlayer(userName).orElse(null);
	}

}
