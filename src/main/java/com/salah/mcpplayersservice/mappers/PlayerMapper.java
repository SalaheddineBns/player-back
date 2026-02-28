package com.salah.mcpplayersservice.mappers;

import com.salah.mcpplayersservice.dto.response.MediaResponseDto;
import com.salah.mcpplayersservice.dto.response.PlayerProfileResponseDto;
import com.salah.mcpplayersservice.dto.response.PlayerResponseDto;
import com.salah.mcpplayersservice.dto.response.PlayerTeamResponseDto;
import com.salah.mcpplayersservice.models.Player;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

	@Mapping(source = "player.playerId", target = "playerId")
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
	@Mapping(source = "user.username", target = "userName")
	@Mapping(source = "user.email", target = "email")
	PlayerResponseDto toPlayerResponseDto(Player player, User user);

	@Mapping(source = "player.playerId", target = "playerId")
	@Mapping(source = "user.userId", target = "userId")
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
	@Mapping(source = "user.username", target = "userName")
	@Mapping(source = "user.email", target = "email")
	@Mapping(source = "mediaItems", target = "mediaItems")
	PlayerProfileResponseDto toPlayerProfileResponseDto(Player player, User user, List<MediaResponseDto> mediaItems);

	PlayerTeamResponseDto toPlayerTeamResponseDto(Team team);

	Set<PlayerResponseDto> toPlayerResponseDtoSet(Set<Player> players);

}
