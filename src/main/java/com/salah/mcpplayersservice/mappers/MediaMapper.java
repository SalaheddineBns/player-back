package com.salah.mcpplayersservice.mappers;

import com.salah.mcpplayersservice.dto.response.MediaResponseDto;
import com.salah.mcpplayersservice.dto.response.MediaViewResponseDto;
import com.salah.mcpplayersservice.models.Media;
import com.salah.mcpplayersservice.models.MediaView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MediaMapper {

	@Mapping(target = "mediaType", expression = "java(media.getMediaType().name())")
	@Mapping(target = "viewCount", expression = "java(viewCount)")
	@Mapping(target = "mediaUrl", expression = "java(\"/api/media/\" + media.getMediaId() + \"/file\")")
	MediaResponseDto toMediaResponseDto(Media media, long viewCount);

	@Mapping(source = "team.teamId", target = "teamId")
	@Mapping(source = "team.teamName", target = "teamName")
	MediaViewResponseDto toMediaViewResponseDto(MediaView mediaView);

	List<MediaViewResponseDto> toMediaViewResponseDtos(List<MediaView> mediaViews);

}
