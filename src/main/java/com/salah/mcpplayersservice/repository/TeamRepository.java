package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {

	boolean existsByTeamName(String teamName);

	Optional<Team> findByTeamName(String teamName);

	Page<Team> findByTeamNameContainingIgnoreCase(String keyword, Pageable pageable);

}
