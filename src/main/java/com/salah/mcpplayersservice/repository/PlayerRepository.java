package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

	Optional<Player> findByEmail(String email);

	Optional<Player> findByUserName(String userName);

	boolean existsByEmail(String email);

	boolean existsByUserName(String userName);

}
