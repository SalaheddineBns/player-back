package com.salah.mcpplayersservice.config;

import com.salah.mcpplayersservice.models.Manager;
import com.salah.mcpplayersservice.models.Role;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.repository.ManagerRepository;
import com.salah.mcpplayersservice.repository.TeamRepository;
import com.salah.mcpplayersservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class TeamDataSeeder implements CommandLineRunner {

	private final TeamRepository teamRepository;

	private final ManagerRepository managerRepository;

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) {
		List<String[]> seedData = List.of(new String[] { "FC Barcelona", "Xavi" },
				new String[] { "Real Madrid", "Carlo Ancelotti" }, new String[] { "Manchester City", "Pep Guardiola" },
				new String[] { "Paris Saint-Germain", "Luis Enrique" });

		for (String[] entry : seedData) {
			String teamName = entry[0];
			String coach = entry[1];

			java.util.Optional<Team> existing = teamRepository.findByTeamName(teamName);
			if (existing.isPresent()) {
				Team team = existing.get();
				if (team.getUser() == null) {
					linkManagerToExistingTeam(team, coach);
				}
			}
			else {
				createTeamWithManager(teamName, coach);
			}
		}
	}

	private Manager createManager(String teamName, String coach) {
		String[] coachParts = coach.split(" ", 2);
		String firstName = coachParts[0];
		String lastName = coachParts.length > 1 ? coachParts[1] : firstName;
		String username = teamName.replaceAll("\\s+", "").toLowerCase();

		// Skip if user already exists
		if (userRepository.existsByUserName(username)) {
			return null;
		}

		Manager manager = Manager.builder()
			.userName(username)
			.email(username + "@team.com")
			.password(passwordEncoder.encode("password123"))
			.role(Role.TEAM_MANAGER)
			.firstName(firstName)
			.lastName(lastName)
			.build();
		return managerRepository.save(manager);
	}

	private void createTeamWithManager(String teamName, String coach) {
		Manager manager = createManager(teamName, coach);
		if (manager == null) {
			return;
		}

		Team team = new Team();
		team.setTeamName(teamName);
		team.setCoach(coach);
		team.setDateCreated(new Date());
		team.setUser(manager);
		teamRepository.save(team);
	}

	private void linkManagerToExistingTeam(Team team, String coach) {
		Manager manager = createManager(team.getTeamName(), coach);
		if (manager == null) {
			return;
		}

		team.setUser(manager);
		teamRepository.save(team);
	}

}
