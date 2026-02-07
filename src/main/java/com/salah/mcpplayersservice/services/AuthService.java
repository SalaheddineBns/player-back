package com.salah.mcpplayersservice.services;

import com.salah.mcpplayersservice.dto.request.LoginRequest;
import com.salah.mcpplayersservice.dto.request.SignupRequest;
import com.salah.mcpplayersservice.dto.response.AuthResponseDto;
import com.salah.mcpplayersservice.exceptions.PlayerAlreadyExistsException;
import com.salah.mcpplayersservice.models.Player;
import com.salah.mcpplayersservice.repository.PlayerRepository;
import com.salah.mcpplayersservice.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final PlayerRepository playerRepository;

	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	private final JwtUtil jwtUtil;

	public AuthService(PlayerRepository playerRepository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
		this.playerRepository = playerRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	public Player signup(SignupRequest request) {
		if (playerRepository.existsByEmail(request.email())) {
			throw new PlayerAlreadyExistsException("A player with this email already exists");
		}

		Player player = Player.builder()
			.firstName(request.firstName())
			.lastName(request.lastName())
			.userName(request.userName())
			.email(request.email())
			.password(passwordEncoder.encode(request.password()))
			.build();

		return playerRepository.save(player);
	}

	public AuthResponseDto login(LoginRequest request) {
		Authentication authentication = authenticationManager
			.authenticate(new UsernamePasswordAuthenticationToken(request.userName(), request.password()));

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String token = jwtUtil.generateToken(userDetails);
		return new AuthResponseDto(token, "Login successful");
	}

}
