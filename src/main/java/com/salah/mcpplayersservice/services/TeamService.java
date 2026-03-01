package com.salah.mcpplayersservice.services;

import com.salah.mcpplayersservice.dto.request.TeamUpdateRequest;
import com.salah.mcpplayersservice.dto.response.PublicationResponseDto;
import com.salah.mcpplayersservice.dto.response.TeamOptionResponseDto;
import com.salah.mcpplayersservice.dto.response.TeamPageResponseDto;
import com.salah.mcpplayersservice.dto.response.RecruitmentNeedResponseDto;
import com.salah.mcpplayersservice.dto.response.SubscriberResponseDto;
import com.salah.mcpplayersservice.dto.response.TeamResponseDto;
import com.salah.mcpplayersservice.exceptions.RessourceNotFoundException;
import com.salah.mcpplayersservice.mappers.PublicationMapper;
import com.salah.mcpplayersservice.mappers.TeamMapper;
import com.salah.mcpplayersservice.models.Player;
import com.salah.mcpplayersservice.models.Publication;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.models.TeamSubscription;
import com.salah.mcpplayersservice.repository.PublicationRepository;
import com.salah.mcpplayersservice.repository.RecruitmentNeedRepository;
import com.salah.mcpplayersservice.repository.TeamRepository;
import com.salah.mcpplayersservice.repository.TeamSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {

	private final TeamRepository teamRepository;

	private final TeamSubscriptionRepository teamSubscriptionRepository;

	private final PublicationRepository publicationRepository;

	private final RecruitmentNeedRepository recruitmentNeedRepository;

	private final TeamMapper teamMapper;

	private final PublicationMapper publicationMapper;

	@Tool(name = "addTeam", description = "Add a football team")
	public TeamResponseDto addTeam(Team team) {
		if (team == null) {
			throw new IllegalArgumentException("Team cannot be null");
		}
		return teamMapper.toTeamResponseDto(teamRepository.save(team));
	}

	@Tool(name = "GetTeam", description = "Get one football team")
	public TeamResponseDto findTeam(UUID teamId) {
		Team team = teamRepository.findById(teamId)
			.orElseThrow(() -> new RessourceNotFoundException("Team", "id", teamId));
		return teamMapper.toTeamResponseDto(team);
	}

	@Tool(name = "UpdateTeam", description = "Update one football team")
	public TeamResponseDto updateTeam(Team team) {
		if (team == null) {
			throw new IllegalArgumentException("Team cannot be null");
		}
		Team existingTeam = teamRepository.findById(team.getTeamId())
			.orElseThrow(() -> new RessourceNotFoundException("Team", "id", team.getTeamId()));
		existingTeam.setTeamName(team.getTeamName());
		existingTeam.setDateCreated(team.getDateCreated());
		existingTeam.setCoach(team.getCoach());
		return teamMapper.toTeamResponseDto(teamRepository.save(existingTeam));
	}

	public List<TeamOptionResponseDto> getAllTeamOptions() {
		List<Team> teams = teamRepository.findAll(Sort.by(Sort.Direction.ASC, "teamName"));
		return teamMapper.toTeamOptionResponseDtos(teams);
	}

	public Page<TeamOptionResponseDto> searchTeams(String keyword, int page, int size) {
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "teamName"));
		Page<Team> teams = teamRepository.findByTeamNameContainingIgnoreCase(keyword, pageRequest);
		return teams.map(teamMapper::toTeamOptionResponseDto);
	}

	public void subscribe(Player player, UUID teamId) {
		Team team = teamRepository.findById(teamId)
			.orElseThrow(() -> new RessourceNotFoundException("Team", "id", teamId));
		if (teamSubscriptionRepository.existsByPlayerAndTeam(player, team)) {
			throw new IllegalStateException("Already subscribed to this team");
		}
		TeamSubscription subscription = new TeamSubscription();
		subscription.setPlayer(player);
		subscription.setTeam(team);
		teamSubscriptionRepository.save(subscription);
	}

	public void unsubscribe(Player player, UUID teamId) {
		Team team = teamRepository.findById(teamId)
			.orElseThrow(() -> new RessourceNotFoundException("Team", "id", teamId));
		TeamSubscription subscription = teamSubscriptionRepository.findByPlayerAndTeam(player, team)
			.orElseThrow(() -> new IllegalStateException("Not subscribed to this team"));
		teamSubscriptionRepository.delete(subscription);
	}

	public boolean isSubscribed(Player player, UUID teamId) {
		Team team = teamRepository.findById(teamId).orElse(null);
		if (team == null) {
			return false;
		}
		return teamSubscriptionRepository.existsByPlayerAndTeam(player, team);
	}

	public Set<UUID> getSubscribedTeamIds(Player player) {
		return teamSubscriptionRepository.findTeamIdsByPlayer(player);
	}

	public List<SubscriberResponseDto> getSubscribers(Team team) {
		return teamSubscriptionRepository.findByTeam(team)
			.stream()
			.map(sub -> {
				var p = sub.getPlayer();
				return new SubscriberResponseDto(p.getPlayerId(), p.getFirstName(), p.getLastName(), p.getPosition(),
						p.getNationality(), p.getCity(), p.getProfilePictureUrl(),
						p.getStatus() != null ? p.getStatus().name() : null, sub.getSubscribedAt());
			})
			.toList();
	}

	public TeamPageResponseDto updateTeamProfile(Team team, TeamUpdateRequest request) {
		if (request.teamName() != null && !request.teamName().isBlank()) {
			team.setTeamName(request.teamName());
		}
		if (request.description() != null) {
			team.setDescription(request.description());
		}
		if (request.division() != null) {
			team.setDivision(request.division());
		}
		team = teamRepository.save(team);
		return getTeamPage(team.getTeamId());
	}

	public TeamPageResponseDto getTeamPage(UUID teamId) {
		Team team = teamRepository.findById(teamId)
			.orElseThrow(() -> new RessourceNotFoundException("Team", "id", teamId));
		List<Publication> publications = publicationRepository.findByTeamTeamIdOrderByCreatedAtDesc(teamId);
		List<PublicationResponseDto> publicationDtos = publicationMapper.toPublicationResponseDtoList(publications);
		List<RecruitmentNeedResponseDto> recruitmentDtos = recruitmentNeedRepository
			.findByTeamTeamIdOrderByCreatedAtDesc(teamId)
			.stream()
			.map(r -> new RecruitmentNeedResponseDto(r.getId(), r.getPosition(), r.getDescription(), r.getCreatedAt()))
			.toList();
		int playerCount = team.getPlayers() != null ? team.getPlayers().size() : 0;
		return new TeamPageResponseDto(team.getTeamId(), team.getTeamName(), team.getDescription(), team.getDivision(),
				team.getCoach(), team.getDateCreated(), team.getLogoUrl(), playerCount, publicationDtos,
				recruitmentDtos);
	}

}
