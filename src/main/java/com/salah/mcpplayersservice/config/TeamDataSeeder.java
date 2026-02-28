package com.salah.mcpplayersservice.config;

import com.salah.mcpplayersservice.models.Player;
import com.salah.mcpplayersservice.models.Role;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.repository.PlayerRepository;
import com.salah.mcpplayersservice.repository.TeamRepository;
import com.salah.mcpplayersservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TeamDataSeeder implements CommandLineRunner {

	private final TeamRepository teamRepository;

	private final PlayerRepository playerRepository;

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
					linkUserToExistingTeam(team, coach);
				}
			}
			else {
				createTeamWithUser(teamName, coach);
			}
		}
	}

	private User createUserAndPlayer(String teamName, String coach) {
		String[] coachParts = coach.split(" ", 2);
		String firstName = coachParts[0];
		String lastName = coachParts.length > 1 ? coachParts[1] : firstName;

		Player player = Player.builder().firstName(firstName).lastName(lastName).build();
		player = playerRepository.save(player);

		String username = teamName.replaceAll("\\s+", "").toLowerCase();

		User user = User.builder()
			.userName(username)
			.email(username + "@team.com")
			.password(passwordEncoder.encode("password123"))
			.role(Role.TEAM_MANAGER)
			.player(player)
			.build();
		user = userRepository.save(user);

		player.setUser(user);
		return user;
	}

	private void createTeamWithUser(String teamName, String coach) {
		User user = createUserAndPlayer(teamName, coach);

		Team team = new Team();
		team.setTeamName(teamName);
		team.setCoach(coach);
		team.setDateCreated(new Date());
		team.setUser(user);
		team = teamRepository.save(team);

		user.getPlayer().setTeam(team);
		playerRepository.save(user.getPlayer());
	}

	private void linkUserToExistingTeam(Team team, String coach) {
		User user = createUserAndPlayer(team.getTeamName(), coach);

		team.setUser(user);
		teamRepository.save(team);

		user.getPlayer().setTeam(team);
		playerRepository.save(user.getPlayer());
	}

}
