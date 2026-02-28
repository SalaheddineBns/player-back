package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.Player;
import com.salah.mcpplayersservice.models.PlayerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

	List<Player> findByTeamIsNull();

	List<Player> findByStatus(PlayerStatus status);

	@Query("SELECT p FROM Player p WHERE p.status = :status "
			+ "AND (CAST(:position AS String) IS NULL OR LOWER(p.position) LIKE LOWER(CONCAT('%', CAST(:position AS String), '%'))) "
			+ "AND (CAST(:nationality AS String) IS NULL OR LOWER(p.nationality) LIKE LOWER(CONCAT('%', CAST(:nationality AS String), '%'))) "
			+ "AND (CAST(:city AS String) IS NULL OR LOWER(p.city) LIKE LOWER(CONCAT('%', CAST(:city AS String), '%')))")
	Page<Player> searchByStatusWithFilters(@Param("status") PlayerStatus status, @Param("position") String position,
			@Param("nationality") String nationality, @Param("city") String city, Pageable pageable);

}