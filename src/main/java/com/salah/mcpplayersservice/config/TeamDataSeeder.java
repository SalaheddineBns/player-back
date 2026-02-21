package com.salah.mcpplayersservice.config;

import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TeamDataSeeder implements CommandLineRunner {

	private final TeamRepository teamRepository;

	@Override
	public void run(String... args) {
		List<Team> seedTeams = List.of(buildTeam("FC Barcelona", "Xavi"), buildTeam("Real Madrid", "Carlo Ancelotti"),
				buildTeam("Manchester City", "Pep Guardiola"), buildTeam("Paris Saint-Germain", "Luis Enrique"));

		for (Team team : seedTeams) {
			if (!teamRepository.existsByTeamName(team.getTeamName())) {
				teamRepository.save(team);
			}
		}
	}

	private Team buildTeam(String teamName, String coach) {
		Team team = new Team();
		team.setTeamName(teamName);
		team.setCoach(coach);
		team.setDateCreated(new Date());
		return team;
	}

}
