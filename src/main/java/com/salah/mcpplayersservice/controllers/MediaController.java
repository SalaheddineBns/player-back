package com.salah.mcpplayersservice.controllers;

import com.salah.mcpplayersservice.dto.response.ErrorResponseDto;
import com.salah.mcpplayersservice.dto.response.MediaResponseDto;
import com.salah.mcpplayersservice.dto.response.MediaViewResponseDto;
import com.salah.mcpplayersservice.models.Media;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.repository.UserRepository;
import com.salah.mcpplayersservice.services.FileStorageService;
import com.salah.mcpplayersservice.services.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@Tag(name = "Media", description = "Player media gallery endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MediaController {

	private final MediaService mediaService;

	private final FileStorageService fileStorageService;

	private final UserRepository userRepository;

	public MediaController(MediaService mediaService, FileStorageService fileStorageService,
			UserRepository userRepository) {
		this.mediaService = mediaService;
		this.fileStorageService = fileStorageService;
		this.userRepository = userRepository;
	}

	@Operation(summary = "Upload media", description = "Upload a photo or video to the player's gallery")
	@ApiResponse(responseCode = "200", description = "Media uploaded successfully",
			content = @Content(schema = @Schema(implementation = MediaResponseDto.class)))
	@ApiResponse(responseCode = "403", description = "User is not a player",
			content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
	@PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<MediaResponseDto> uploadMedia(Authentication authentication,
			@RequestParam("file") MultipartFile file, @RequestParam("title") String title,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam("mediaType") String mediaType) {
		User user = resolveCurrentUser(authentication);
		if (user == null) {
			return ResponseEntity.status(401).build();
		}
		if (user.getPlayer() == null) {
			return ResponseEntity.status(403).build();
		}
		com.salah.mcpplayersservice.models.MediaType type = com.salah.mcpplayersservice.models.MediaType
			.valueOf(mediaType.toUpperCase());
		MediaResponseDto response = mediaService.uploadMedia(file, title, description, type, user.getPlayer());
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Get my media", description = "Returns all media items for the current player")
	@ApiResponse(responseCode = "200", description = "Media list returned successfully")
	@GetMapping("/me")
	public ResponseEntity<List<MediaResponseDto>> getMyMedia(Authentication authentication) {
		User user = resolveCurrentUser(authentication);
		if (user == null) {
			return ResponseEntity.status(401).build();
		}
		if (user.getPlayer() == null) {
			return ResponseEntity.status(403).build();
		}
		return ResponseEntity.ok(mediaService.getMyMedia(user.getPlayer().getPlayerId()));
	}

	@Operation(summary = "Get media by ID", description = "Returns a single media item")
	@ApiResponse(responseCode = "200", description = "Media item returned successfully")
	@GetMapping("/{id}")
	public ResponseEntity<MediaResponseDto> getMediaById(@PathVariable UUID id) {
		return ResponseEntity.ok(mediaService.getMediaById(id));
	}

	@Operation(summary = "Get media views", description = "Returns the list of team views for a media item")
	@ApiResponse(responseCode = "200", description = "Views returned successfully")
	@GetMapping("/{id}/views")
	public ResponseEntity<List<MediaViewResponseDto>> getMediaViews(@PathVariable UUID id) {
		return ResponseEntity.ok(mediaService.getViewsForMedia(id));
	}

	@Operation(summary = "Delete media", description = "Deletes a media item owned by the current player")
	@ApiResponse(responseCode = "204", description = "Media deleted successfully")
	@ApiResponse(responseCode = "403", description = "User is not a player or does not own this media",
			content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteMedia(Authentication authentication, @PathVariable UUID id) {
		User user = resolveCurrentUser(authentication);
		if (user == null) {
			return ResponseEntity.status(401).build();
		}
		if (user.getPlayer() == null) {
			return ResponseEntity.status(403).build();
		}
		mediaService.deleteMedia(id, user.getPlayer().getPlayerId());
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Serve media file", description = "Streams the actual media file")
	@ApiResponse(responseCode = "200", description = "File streamed successfully")
	@GetMapping("/{id}/file")
	public ResponseEntity<Resource> serveFile(@PathVariable UUID id) throws IOException {
		Media media = mediaService.getMediaEntity(id);
		Path filePath = fileStorageService.getFilePath(media.getFilePath());
		Resource resource = new UrlResource(filePath.toUri());

		if (!resource.exists()) {
			return ResponseEntity.notFound().build();
		}

		String contentType = media.getContentType() != null ? media.getContentType() : "application/octet-stream";

		return ResponseEntity.ok()
			.contentType(org.springframework.http.MediaType.parseMediaType(contentType))
			.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + media.getFileName() + "\"")
			.body(resource);
	}

	private User resolveCurrentUser(Authentication authentication) {
		if (authentication == null || authentication.getPrincipal() == null) {
			return null;
		}

		String userName;
		Object principal = authentication.getPrincipal();
		if (principal instanceof User userPrincipal) {
			return userPrincipal;
		}
		if (principal instanceof UserDetails userDetails) {
			userName = userDetails.getUsername();
		}
		else {
			userName = principal.toString();
		}

		return userRepository.findByUserNameWithPlayer(userName).orElse(null);
	}

}
