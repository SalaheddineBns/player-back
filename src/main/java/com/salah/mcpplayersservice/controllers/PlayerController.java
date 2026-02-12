package com.salah.mcpplayersservice.controllers;

import com.salah.mcpplayersservice.dto.request.PlayerUpdateRequest;
import com.salah.mcpplayersservice.dto.response.ErrorResponseDto;
import com.salah.mcpplayersservice.dto.response.PlayerResponseDto;
import com.salah.mcpplayersservice.mappers.PlayerMapper;
import com.salah.mcpplayersservice.models.Player;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.repository.PlayerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@Tag(name = "Players", description = "Player profile endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PlayerController {

	private final PlayerMapper playerMapper;

	private final PlayerRepository playerRepository;

	public PlayerController(PlayerMapper playerMapper, PlayerRepository playerRepository) {
		this.playerMapper = playerMapper;
		this.playerRepository = playerRepository;
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
	public ResponseEntity<PlayerResponseDto> getMyProfile(@AuthenticationPrincipal User user) {
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
	public ResponseEntity<PlayerResponseDto> updateMyProfile(@AuthenticationPrincipal User user,
			@RequestBody PlayerUpdateRequest request) {
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
		playerRepository.save(player);

		return ResponseEntity.ok(playerMapper.toPlayerResponseDto(player, user));
	}

}