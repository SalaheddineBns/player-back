package com.salah.mcpplayersservice.mappers;

import com.salah.mcpplayersservice.dto.response.PlayerResponseDto;
import com.salah.mcpplayersservice.dto.response.PlayerTeamResponseDto;
import com.salah.mcpplayersservice.models.Player;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

	@Mapping(source = "player.playerId", target = "playerId")
	@Mapping(source = "player.firstName", target = "firstName")
	@Mapping(source = "player.lastName", target = "lastName")
	@Mapping(source = "player.position", target = "position")
	@Mapping(source = "player.nationality", target = "nationality")
	@Mapping(source = "player.gender", target = "gender")
	@Mapping(source = "player.preferredLeg", target = "preferredLeg")
	@Mapping(source = "player.preferredNumber", target = "preferredNumber")
	@Mapping(source = "player.team", target = "team")
	@Mapping(source = "player.profilePictureUrl", target = "profilePictureUrl")
	@Mapping(source = "user.username", target = "userName")
	@Mapping(source = "user.email", target = "email")
	PlayerResponseDto toPlayerResponseDto(Player player, User user);

	PlayerTeamResponseDto toPlayerTeamResponseDto(Team team);

	Set<PlayerResponseDto> toPlayerResponseDtoSet(Set<Player> players);

}
