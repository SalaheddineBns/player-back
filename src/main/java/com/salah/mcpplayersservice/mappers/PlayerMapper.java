package com.salah.mcpplayersservice.mappers;

import com.salah.mcpplayersservice.dto.response.PlayerResponseDto;
import com.salah.mcpplayersservice.models.Player;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

	PlayerResponseDto toPlayerResponseDto(Player player);

	Set<PlayerResponseDto> toPlayerResponseDtoSet(Set<Player> players);

}
