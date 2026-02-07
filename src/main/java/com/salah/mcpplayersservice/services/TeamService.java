package com.salah.mcpplayersservice.services;

import com.salah.mcpplayersservice.dto.response.TeamResponseDto;
import com.salah.mcpplayersservice.exceptions.RessourceNotFoundException;
import com.salah.mcpplayersservice.mappers.TeamMapper;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {

	private final TeamRepository teamRepository;

	private final TeamMapper teamMapper;

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

}
