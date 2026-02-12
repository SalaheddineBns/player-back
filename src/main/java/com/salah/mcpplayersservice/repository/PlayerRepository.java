package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

}