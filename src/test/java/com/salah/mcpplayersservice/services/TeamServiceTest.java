package com.salah.mcpplayersservice.services;

import com.salah.mcpplayersservice.dto.response.TeamOptionResponseDto;
import com.salah.mcpplayersservice.mappers.TeamMapper;
import com.salah.mcpplayersservice.models.Player;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.models.TeamSubscription;
import com.salah.mcpplayersservice.repository.TeamRepository;
import com.salah.mcpplayersservice.repository.TeamSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

	@Mock
	private TeamRepository teamRepository;

	@Mock
	private TeamSubscriptionRepository teamSubscriptionRepository;

	@Mock
	private TeamMapper teamMapper;

	@InjectMocks
	private TeamService teamService;

	@Test
	void searchTeams_withMatchingKeyword_returnsResults() {
		Team team = new Team();
		team.setTeamId(UUID.randomUUID());
		team.setTeamName("Barcelona FC");
		team.setCoach("Coach");
		team.setDateCreated(new Date());

		TeamOptionResponseDto dto = new TeamOptionResponseDto(team.getTeamId(), "Barcelona FC", null);

		Page<Team> teamPage = new PageImpl<>(List.of(team), PageRequest.of(0, 10), 1);
		when(teamRepository.findByTeamNameContainingIgnoreCase(eq("bar"), any(Pageable.class))).thenReturn(teamPage);
		when(teamMapper.toTeamOptionResponseDto(team)).thenReturn(dto);

		Page<TeamOptionResponseDto> result = teamService.searchTeams("bar", 0, 10);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).teamName()).isEqualTo("Barcelona FC");
		assertThat(result.getTotalElements()).isEqualTo(1);
	}

	@Test
	void searchTeams_withNoMatch_returnsEmptyPage() {
		Page<Team> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
		when(teamRepository.findByTeamNameContainingIgnoreCase(eq("xyz"), any(Pageable.class))).thenReturn(emptyPage);

		Page<TeamOptionResponseDto> result = teamService.searchTeams("xyz", 0, 10);

		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isEqualTo(0);
	}

	@Test
	void searchTeams_pagination_returnsCorrectPage() {
		Team team = new Team();
		team.setTeamId(UUID.randomUUID());
		team.setTeamName("Team A");
		team.setCoach("Coach A");
		team.setDateCreated(new Date());

		TeamOptionResponseDto dto = new TeamOptionResponseDto(team.getTeamId(), "Team A", null);

		Page<Team> teamPage = new PageImpl<>(List.of(team), PageRequest.of(1, 5), 6);
		when(teamRepository.findByTeamNameContainingIgnoreCase(eq("team"), any(Pageable.class))).thenReturn(teamPage);
		when(teamMapper.toTeamOptionResponseDto(team)).thenReturn(dto);

		Page<TeamOptionResponseDto> result = teamService.searchTeams("team", 1, 5);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getNumber()).isEqualTo(1);
		assertThat(result.getTotalElements()).isEqualTo(6);
	}

	@Test
	void subscribe_successfully() {
		Player player = Player.builder().playerId(UUID.randomUUID()).firstName("John").lastName("Doe").build();
		Team team = new Team();
		UUID teamId = UUID.randomUUID();
		team.setTeamId(teamId);

		when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
		when(teamSubscriptionRepository.existsByPlayerAndTeam(player, team)).thenReturn(false);

		teamService.subscribe(player, teamId);

		verify(teamSubscriptionRepository).save(any(TeamSubscription.class));
	}

	@Test
	void subscribe_alreadySubscribed_throwsException() {
		Player player = Player.builder().playerId(UUID.randomUUID()).firstName("John").lastName("Doe").build();
		Team team = new Team();
		UUID teamId = UUID.randomUUID();
		team.setTeamId(teamId);

		when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
		when(teamSubscriptionRepository.existsByPlayerAndTeam(player, team)).thenReturn(true);

		assertThatThrownBy(() -> teamService.subscribe(player, teamId)).isInstanceOf(IllegalStateException.class)
			.hasMessage("Already subscribed to this team");
	}

	@Test
	void unsubscribe_successfully() {
		Player player = Player.builder().playerId(UUID.randomUUID()).firstName("John").lastName("Doe").build();
		Team team = new Team();
		UUID teamId = UUID.randomUUID();
		team.setTeamId(teamId);
		TeamSubscription subscription = new TeamSubscription();

		when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
		when(teamSubscriptionRepository.findByPlayerAndTeam(player, team)).thenReturn(Optional.of(subscription));

		teamService.unsubscribe(player, teamId);

		verify(teamSubscriptionRepository).delete(subscription);
	}

	@Test
	void unsubscribe_notSubscribed_throwsException() {
		Player player = Player.builder().playerId(UUID.randomUUID()).firstName("John").lastName("Doe").build();
		Team team = new Team();
		UUID teamId = UUID.randomUUID();
		team.setTeamId(teamId);

		when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
		when(teamSubscriptionRepository.findByPlayerAndTeam(player, team)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> teamService.unsubscribe(player, teamId)).isInstanceOf(IllegalStateException.class)
			.hasMessage("Not subscribed to this team");
	}

}
