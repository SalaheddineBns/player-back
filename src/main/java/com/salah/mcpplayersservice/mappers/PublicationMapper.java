package com.salah.mcpplayersservice.mappers;

import com.salah.mcpplayersservice.dto.response.PublicationResponseDto;
import com.salah.mcpplayersservice.models.Publication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PublicationMapper {

	@Mapping(source = "team.teamId", target = "teamId")
	@Mapping(source = "team.teamName", target = "teamName")
	@Mapping(source = "team.logoUrl", target = "teamLogoUrl")
	PublicationResponseDto toPublicationResponseDto(Publication publication);

	List<PublicationResponseDto> toPublicationResponseDtoList(List<Publication> publications);

}
