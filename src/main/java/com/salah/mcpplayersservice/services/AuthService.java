package com.salah.mcpplayersservice.services;

import com.salah.mcpplayersservice.dto.request.LoginRequest;
import com.salah.mcpplayersservice.dto.request.SignupRequest;
import com.salah.mcpplayersservice.dto.response.AuthResponseDto;
import com.salah.mcpplayersservice.exceptions.UserAlreadyExistsException;
import com.salah.mcpplayersservice.models.Player;
import com.salah.mcpplayersservice.models.Role;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.repository.PlayerRepository;
import com.salah.mcpplayersservice.repository.UserRepository;
import com.salah.mcpplayersservice.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;

	private final PlayerRepository playerRepository;

	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	private final JwtUtil jwtUtil;

	public AuthService(UserRepository userRepository, PlayerRepository playerRepository,
			PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
		this.userRepository = userRepository;
		this.playerRepository = playerRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	@Transactional
	public User signup(SignupRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new UserAlreadyExistsException("A user with this email already exists");
		}
		if (userRepository.existsByUserName(request.userName())) {
			throw new UserAlreadyExistsException("A user with this username already exists");
		}

		Player player = Player.builder()
			.firstName(request.firstName())
			.lastName(request.lastName())
			.gender(request.gender())
			.build();
		player = playerRepository.save(player);

		User user = User.builder()
			.userName(request.userName())
			.email(request.email())
			.password(passwordEncoder.encode(request.password()))
			.role(Role.PLAYER)
			.player(player)
			.build();
		user = userRepository.save(user);

		player.setUser(user);

		return user;
	}

	public AuthResponseDto login(LoginRequest request) {
		Authentication authentication = authenticationManager
			.authenticate(new UsernamePasswordAuthenticationToken(request.userName(), request.password()));

		User user = (User) authentication.getPrincipal();
		String token = jwtUtil.generateToken(user);
		return new AuthResponseDto(token, user.getRole().name(), "Login successful");
	}

}