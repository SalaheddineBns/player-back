package com.salah.mcpplayersservice.controllers;

import com.salah.mcpplayersservice.dto.request.PlayerUpdateRequest;
import com.salah.mcpplayersservice.dto.response.ErrorResponseDto;
import com.salah.mcpplayersservice.dto.response.MediaResponseDto;
import com.salah.mcpplayersservice.dto.response.PlayerProfileResponseDto;
import com.salah.mcpplayersservice.dto.response.PlayerResponseDto;
import com.salah.mcpplayersservice.exceptions.RessourceNotFoundException;
import com.salah.mcpplayersservice.mappers.MediaMapper;
import com.salah.mcpplayersservice.mappers.PlayerMapper;
import com.salah.mcpplayersservice.models.Player;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.repository.MediaViewRepository;
import com.salah.mcpplayersservice.repository.PlayerRepository;
import com.salah.mcpplayersservice.repository.TeamRepository;
import com.salah.mcpplayersservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.salah.mcpplayersservice.models.PlayerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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

	private final MediaMapper mediaMapper;

	private final MediaViewRepository mediaViewRepository;

	@Value("${media.upload-dir}")
	private String uploadDir;

	public PlayerController(PlayerMapper playerMapper, PlayerRepository playerRepository, TeamRepository teamRepository,
			UserRepository userRepository, MediaMapper mediaMapper, MediaViewRepository mediaViewRepository) {
		this.playerMapper = playerMapper;
		this.playerRepository = playerRepository;
		this.teamRepository = teamRepository;
		this.userRepository = userRepository;
		this.mediaMapper = mediaMapper;
		this.mediaViewRepository = mediaViewRepository;
	}

	@Operation(summary = "Get available players",
			description = "Returns players whose status is AVAILABLE or LOOKING_FOR_TEAM")
	@ApiResponse(responseCode = "200", description = "List returned successfully")
	@GetMapping("/available")
	public ResponseEntity<List<PlayerResponseDto>> getAvailablePlayers() {
		List<PlayerResponseDto> available = playerRepository.findByStatus(PlayerStatus.AVAILABLE)
			.stream()
			.map(player -> playerMapper.toPlayerResponseDto(player, player.getUser()))
			.toList();
		List<PlayerResponseDto> lookingForTeam = playerRepository.findByStatus(PlayerStatus.LOOKING_FOR_TEAM)
			.stream()
			.map(player -> playerMapper.toPlayerResponseDto(player, player.getUser()))
			.toList();
		List<PlayerResponseDto> result = new java.util.ArrayList<>(available);
		result.addAll(lookingForTeam);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Search players by status",
			description = "Search players with optional position, nationality, and city filters")
	@ApiResponse(responseCode = "200", description = "Search results returned successfully")
	@GetMapping("/search")
	public ResponseEntity<Page<PlayerResponseDto>> searchPlayers(
			@RequestParam(defaultValue = "LOOKING_FOR_TEAM") PlayerStatus status,
			@RequestParam(required = false) String position, @RequestParam(required = false) String nationality,
			@RequestParam(required = false) String city, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Page<PlayerResponseDto> results = playerRepository
			.searchByStatusWithFilters(status, position, nationality, city, PageRequest.of(page, size))
			.map(player -> playerMapper.toPlayerResponseDto(player, player.getUser()));
		return ResponseEntity.ok(results);
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
		if (request.city() != null) {
			player.setCity(request.city());
		}
		if (request.preferredLeg() != null) {
			player.setPreferredLeg(request.preferredLeg());
		}
		if (request.preferredNumber() != null) {
			player.setPreferredNumber(request.preferredNumber());
		}
		if (request.status() != null) {
			player.setStatus(request.status());
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

	@Operation(summary = "Get player profile by ID",
			description = "Returns the public profile of a player including media items")
	@ApiResponse(responseCode = "200", description = "Profile returned successfully")
	@ApiResponse(responseCode = "404", description = "Player not found")
	@GetMapping("/{playerId}")
	public ResponseEntity<PlayerProfileResponseDto> getPlayerProfile(@PathVariable UUID playerId) {
		Player player = playerRepository.findById(playerId)
			.orElseThrow(() -> new RessourceNotFoundException("Player", "id", playerId));

		List<MediaResponseDto> mediaDtos = player.getMediaItems().stream().map(media -> {
			long viewCount = mediaViewRepository.countByMediaMediaId(media.getMediaId());
			return mediaMapper.toMediaResponseDto(media, viewCount);
		}).toList();

		return ResponseEntity.ok(playerMapper.toPlayerProfileResponseDto(player, player.getUser(), mediaDtos));
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
