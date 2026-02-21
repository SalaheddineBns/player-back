package com.salah.mcpplayersservice.controllers;

import com.salah.mcpplayersservice.dto.request.LoginRequest;
import com.salah.mcpplayersservice.dto.request.SignupRequest;
import com.salah.mcpplayersservice.dto.response.AuthResponseDto;
import com.salah.mcpplayersservice.dto.response.ErrorResponseDto;
import com.salah.mcpplayersservice.dto.response.PlayerResponseDto;
import com.salah.mcpplayersservice.mappers.PlayerMapper;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User signup and login endpoints")
public class AuthController {

	private final AuthService authService;

	private final PlayerMapper playerMapper;

	public AuthController(AuthService authService, PlayerMapper playerMapper) {
		this.authService = authService;
		this.playerMapper = playerMapper;
	}

	@Operation(summary = "Register a new player",
			description = "Creates a new user account with PLAYER role and linked player profile")
	@ApiResponse(responseCode = "201", description = "Player registered successfully",
			content = @Content(schema = @Schema(implementation = PlayerResponseDto.class)))
	@ApiResponse(responseCode = "409", description = "Email or username already exists",
			content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
	@ApiResponse(responseCode = "400", description = "Validation error",
			content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
	@PostMapping("/signup")
	public ResponseEntity<PlayerResponseDto> signup(@Valid @RequestBody SignupRequest request) {
		User user = authService.signup(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(playerMapper.toPlayerResponseDto(user.getPlayer(), user));
	}

	@Operation(summary = "Login", description = "Authenticates a user and returns a JWT token with role")
	@ApiResponse(responseCode = "200", description = "Login successful",
			content = @Content(schema = @Schema(implementation = AuthResponseDto.class)))
	@ApiResponse(responseCode = "401", description = "Invalid credentials",
			content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
	@PostMapping("/login")
	public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequest request) {
		AuthResponseDto response = authService.login(request);
		return ResponseEntity.ok(response);
	}

}
