package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.Player;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.models.TeamSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TeamSubscriptionRepository extends JpaRepository<TeamSubscription, UUID> {

	boolean existsByPlayerAndTeam(Player player, Team team);

	Optional<TeamSubscription> findByPlayerAndTeam(Player player, Team team);

	@Query("SELECT ts.team.teamId FROM TeamSubscription ts WHERE ts.player = :player")
	Set<UUID> findTeamIdsByPlayer(@Param("player") Player player);

	List<TeamSubscription> findByTeam(Team team);

}
