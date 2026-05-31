package com.salah.mcpplayersservice.mappers;

import com.salah.mcpplayersservice.dto.response.MediaResponseDto;
import com.salah.mcpplayersservice.dto.response.PlayerProfileResponseDto;
import com.salah.mcpplayersservice.dto.response.PlayerResponseDto;
import com.salah.mcpplayersservice.dto.response.PlayerTeamResponseDto;
import com.salah.mcpplayersservice.models.Player;
import com.salah.mcpplayersservice.models.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

	@Mapping(source = "userId", target = "playerId")
	@Mapping(source = "username", target = "userName")
	@Mapping(source = "email", target = "email")
	@Mapping(source = "firstName", target = "firstName")
	@Mapping(source = "lastName", target = "lastName")
	@Mapping(source = "position", target = "position")
	@Mapping(source = "nationality", target = "nationality")
	@Mapping(source = "city", target = "city")
	@Mapping(source = "gender", target = "gender")
	@Mapping(source = "preferredLeg", target = "preferredLeg")
	@Mapping(source = "preferredNumber", target = "preferredNumber")
	@Mapping(source = "team", target = "team")
	@Mapping(source = "profilePictureUrl", target = "profilePictureUrl")
	@Mapping(target = "status", expression = "java(player.getStatus() != null ? player.getStatus().name() : null)")
	@Mapping(source = "level", target = "level")
	PlayerResponseDto toPlayerResponseDto(Player player);

	@Mapping(source = "player.userId", target = "playerId")
	@Mapping(source = "player.userId", target = "userId")
	@Mapping(source = "player.username", target = "userName")
	@Mapping(source = "player.email", target = "email")
	@Mapping(source = "player.firstName", target = "firstName")
	@Mapping(source = "player.lastName", target = "lastName")
	@Mapping(source = "player.position", target = "position")
	@Mapping(source = "player.nationality", target = "nationality")
	@Mapping(source = "player.city", target = "city")
	@Mapping(source = "player.gender", target = "gender")
	@Mapping(source = "player.preferredLeg", target = "preferredLeg")
	@Mapping(source = "player.preferredNumber", target = "preferredNumber")
	@Mapping(source = "player.team", target = "team")
	@Mapping(source = "player.profilePictureUrl", target = "profilePictureUrl")
	@Mapping(target = "status", expression = "java(player.getStatus() != null ? player.getStatus().name() : null)")
	@Mapping(source = "player.level", target = "level")
	@Mapping(source = "mediaItems", target = "mediaItems")
	PlayerProfileResponseDto toPlayerProfileResponseDto(Player player, List<MediaResponseDto> mediaItems);

	PlayerTeamResponseDto toPlayerTeamResponseDto(Team team);

	Set<PlayerResponseDto> toPlayerResponseDtoSet(Set<Player> players);

}
