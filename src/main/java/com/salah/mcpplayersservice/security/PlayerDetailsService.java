package com.salah.mcpplayersservice.security;

import com.salah.mcpplayersservice.repository.PlayerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PlayerDetailsService implements UserDetailsService {

	private final PlayerRepository playerRepository;

	public PlayerDetailsService(PlayerRepository playerRepository) {
		this.playerRepository = playerRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		return playerRepository.findByUserName(userName)
			.orElseThrow(() -> new UsernameNotFoundException("Player not found with username: " + userName));
	}

}
