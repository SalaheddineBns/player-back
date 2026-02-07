package com.salah.mcpplayersservice.mappers;

import com.salah.mcpplayersservice.dto.response.TeamResponseDto;
import com.salah.mcpplayersservice.models.Team;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = PlayerMapper.class)
public interface TeamMapper {

	TeamResponseDto toTeamResponseDto(Team team);

}
