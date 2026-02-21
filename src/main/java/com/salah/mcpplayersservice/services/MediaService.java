package com.salah.mcpplayersservice.services;

import com.salah.mcpplayersservice.dto.response.MediaResponseDto;
import com.salah.mcpplayersservice.dto.response.MediaViewResponseDto;
import com.salah.mcpplayersservice.exceptions.RessourceNotFoundException;
import com.salah.mcpplayersservice.mappers.MediaMapper;
import com.salah.mcpplayersservice.models.Media;
import com.salah.mcpplayersservice.models.MediaType;
import com.salah.mcpplayersservice.models.Player;
import com.salah.mcpplayersservice.repository.MediaRepository;
import com.salah.mcpplayersservice.repository.MediaViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

	private final MediaRepository mediaRepository;

	private final MediaViewRepository mediaViewRepository;

	private final MediaMapper mediaMapper;

	private final FileStorageService fileStorageService;

	@Transactional
	public MediaResponseDto uploadMedia(MultipartFile file, String title, String description, MediaType mediaType,
			Player player) {
		String filePath = fileStorageService.storeFile(file, player.getPlayerId());

		Media media = Media.builder()
			.title(title)
			.description(description)
			.mediaType(mediaType)
			.filePath(filePath)
			.fileName(file.getOriginalFilename())
			.contentType(file.getContentType())
			.fileSize(file.getSize())
			.uploadDate(LocalDateTime.now())
			.player(player)
			.build();

		media = mediaRepository.save(media);
		return mediaMapper.toMediaResponseDto(media, 0L);
	}

	public List<MediaResponseDto> getMyMedia(UUID playerId) {
		List<Media> mediaList = mediaRepository.findByPlayerPlayerIdOrderByUploadDateDesc(playerId);
		return mediaList.stream()
			.map(media -> mediaMapper.toMediaResponseDto(media,
					mediaViewRepository.countByMediaMediaId(media.getMediaId())))
			.toList();
	}

	public MediaResponseDto getMediaById(UUID mediaId) {
		Media media = mediaRepository.findById(mediaId)
			.orElseThrow(() -> new RessourceNotFoundException("Media", "id", mediaId));
		long viewCount = mediaViewRepository.countByMediaMediaId(mediaId);
		return mediaMapper.toMediaResponseDto(media, viewCount);
	}

	public List<MediaViewResponseDto> getViewsForMedia(UUID mediaId) {
		if (!mediaRepository.existsById(mediaId)) {
			throw new RessourceNotFoundException("Media", "id", mediaId);
		}
		return mediaMapper.toMediaViewResponseDtos(mediaViewRepository.findByMediaMediaId(mediaId));
	}

	@Transactional
	public void deleteMedia(UUID mediaId, UUID playerId) {
		Media media = mediaRepository.findById(mediaId)
			.orElseThrow(() -> new RessourceNotFoundException("Media", "id", mediaId));
		if (!media.getPlayer().getPlayerId().equals(playerId)) {
			throw new RuntimeException("You can only delete your own media");
		}
		fileStorageService.deleteFile(media.getFilePath());
		mediaRepository.delete(media);
	}

	public Media getMediaEntity(UUID mediaId) {
		return mediaRepository.findById(mediaId)
			.orElseThrow(() -> new RessourceNotFoundException("Media", "id", mediaId));
	}

}
