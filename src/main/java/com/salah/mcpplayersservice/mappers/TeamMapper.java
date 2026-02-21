package com.salah.mcpplayersservice.mappers;

import com.salah.mcpplayersservice.dto.response.TeamOptionResponseDto;
import com.salah.mcpplayersservice.dto.response.TeamResponseDto;
import com.salah.mcpplayersservice.models.Team;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = PlayerMapper.class)
public interface TeamMapper {

	TeamResponseDto toTeamResponseDto(Team team);

	TeamOptionResponseDto toTeamOptionResponseDto(Team team);

	List<TeamOptionResponseDto> toTeamOptionResponseDtos(List<Team> teams);

}
