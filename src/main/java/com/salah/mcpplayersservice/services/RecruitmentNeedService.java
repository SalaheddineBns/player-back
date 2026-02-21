package com.salah.mcpplayersservice.services;

import com.salah.mcpplayersservice.dto.request.RecruitmentNeedRequest;
import com.salah.mcpplayersservice.dto.response.RecruitmentNeedResponseDto;
import com.salah.mcpplayersservice.exceptions.RessourceNotFoundException;
import com.salah.mcpplayersservice.models.RecruitmentNeed;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.repository.RecruitmentNeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecruitmentNeedService {

	private final RecruitmentNeedRepository recruitmentNeedRepository;

	public RecruitmentNeedResponseDto create(Team team, RecruitmentNeedRequest request) {
		RecruitmentNeed need = RecruitmentNeed.builder()
			.position(request.position())
			.description(request.description())
			.team(team)
			.build();
		need = recruitmentNeedRepository.save(need);
		return toDto(need);
	}

	public List<RecruitmentNeedResponseDto> getByTeam(UUID teamId) {
		return recruitmentNeedRepository.findByTeamTeamIdOrderByCreatedAtDesc(teamId)
			.stream()
			.map(this::toDto)
			.toList();
	}

	public RecruitmentNeedResponseDto update(UUID id, Team team, RecruitmentNeedRequest request) {
		RecruitmentNeed need = recruitmentNeedRepository.findById(id)
			.orElseThrow(() -> new RessourceNotFoundException("RecruitmentNeed", "id", id));
		if (!need.getTeam().getTeamId().equals(team.getTeamId())) {
			throw new IllegalStateException("You can only edit your own team's recruitment needs");
		}
		need.setPosition(request.position());
		need.setDescription(request.description());
		need = recruitmentNeedRepository.save(need);
		return toDto(need);
	}

	public void delete(UUID id, Team team) {
		RecruitmentNeed need = recruitmentNeedRepository.findById(id)
			.orElseThrow(() -> new RessourceNotFoundException("RecruitmentNeed", "id", id));
		if (!need.getTeam().getTeamId().equals(team.getTeamId())) {
			throw new IllegalStateException("You can only delete your own team's recruitment needs");
		}
		recruitmentNeedRepository.delete(need);
	}

	private RecruitmentNeedResponseDto toDto(RecruitmentNeed need) {
		return new RecruitmentNeedResponseDto(need.getId(), need.getPosition(), need.getDescription(),
				need.getCreatedAt());
	}

}
