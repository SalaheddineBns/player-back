package com.salah.mcpplayersservice.controllers;

import com.salah.mcpplayersservice.dto.request.ManagerUpdateRequest;
import com.salah.mcpplayersservice.dto.response.ManagerResponseDto;
import com.salah.mcpplayersservice.dto.response.PlayerTeamResponseDto;
import com.salah.mcpplayersservice.mappers.PlayerMapper;
import com.salah.mcpplayersservice.models.Manager;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.repository.ManagerRepository;
import com.salah.mcpplayersservice.repository.TeamRepository;
import com.salah.mcpplayersservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/managers")
@Tag(name = "Managers", description = "Manager profile endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ManagerController {

	private final ManagerRepository managerRepository;

	private final TeamRepository teamRepository;

	private final UserRepository userRepository;

	private final PlayerMapper playerMapper;

	@Value("${media.upload-dir}")
	private String uploadDir;

	public ManagerController(ManagerRepository managerRepository, TeamRepository teamRepository,
			UserRepository userRepository, PlayerMapper playerMapper) {
		this.managerRepository = managerRepository;
		this.teamRepository = teamRepository;
		this.userRepository = userRepository;
		this.playerMapper = playerMapper;
	}

	@Operation(summary = "Get current manager profile")
	@GetMapping("/me")
	public ResponseEntity<ManagerResponseDto> getMyProfile(Authentication authentication) {
		User user = resolveUser(authentication);
		if (user == null)
			return ResponseEntity.status(401).build();
		if (!(user instanceof Manager manager))
			return ResponseEntity.status(403).build();
		Team team = teamRepository.findByUser(user).orElse(null);
		return ResponseEntity.ok(toDto(manager, team));
	}

	@Operation(summary = "Update current manager profile")
	@PutMapping("/me")
	public ResponseEntity<ManagerResponseDto> updateMyProfile(Authentication authentication,
			@RequestBody ManagerUpdateRequest request) {
		User user = resolveUser(authentication);
		if (user == null)
			return ResponseEntity.status(401).build();
		if (!(user instanceof Manager manager))
			return ResponseEntity.status(403).build();
		if (request.firstName() != null)
			manager.setFirstName(request.firstName());
		if (request.lastName() != null)
			manager.setLastName(request.lastName());
		manager = managerRepository.save(manager);
		Team team = teamRepository.findByUser(manager).orElse(null);
		return ResponseEntity.ok(toDto(manager, team));
	}

	@Operation(summary = "Upload manager profile picture")
	@PutMapping("/me/picture")
	public ResponseEntity<?> uploadProfilePicture(Authentication authentication,
			@RequestParam("file") MultipartFile file) {
		User user = resolveUser(authentication);
		if (!(user instanceof Manager manager))
			return ResponseEntity.status(403).build();
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/"))
			return ResponseEntity.badRequest().body(Map.of("message", "Only image files are allowed"));
		try {
			Path uploadPath = Paths.get(uploadDir, "profiles");
			Files.createDirectories(uploadPath);
			String extension = getExtension(file.getOriginalFilename());
			String fileName = "manager-" + manager.getManagerId() + extension;
			Path filePath = uploadPath.resolve(fileName);
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
			String pictureUrl = "/api/managers/" + manager.getManagerId() + "/picture";
			manager.setProfilePictureUrl(pictureUrl);
			managerRepository.save(manager);
			return ResponseEntity.ok(Map.of("profilePictureUrl", pictureUrl));
		}
		catch (IOException ex) {
			return ResponseEntity.internalServerError().body(Map.of("message", "Failed to upload picture"));
		}
	}

	@GetMapping("/{managerId}/picture")
	public ResponseEntity<byte[]> getProfilePicture(@PathVariable UUID managerId) {
		Manager manager = managerRepository.findById(managerId).orElse(null);
		if (manager == null || manager.getProfilePictureUrl() == null)
			return ResponseEntity.notFound().build();
		try {
			Path uploadPath = Paths.get(uploadDir, "profiles");
			Path pictureFile = Files.list(uploadPath)
				.filter(p -> p.getFileName().toString().startsWith("manager-" + managerId))
				.findFirst()
				.orElse(null);
			if (pictureFile == null || !Files.exists(pictureFile))
				return ResponseEntity.notFound().build();
			byte[] bytes = Files.readAllBytes(pictureFile);
			String type = Files.probeContentType(pictureFile);
			if (type == null)
				type = "image/png";
			return ResponseEntity.ok()
				.header("Content-Type", type)
				.header("Cache-Control", "max-age=86400")
				.body(bytes);
		}
		catch (IOException ex) {
			return ResponseEntity.internalServerError().build();
		}
	}

	private ManagerResponseDto toDto(Manager manager, Team team) {
		PlayerTeamResponseDto teamDto = team != null ? playerMapper.toPlayerTeamResponseDto(team) : null;
		return new ManagerResponseDto(manager.getManagerId(), manager.getFirstName(), manager.getLastName(),
				manager.getProfilePictureUrl(), manager.getUsername(), manager.getEmail(), teamDto);
	}

	private String getExtension(String filename) {
		if (filename == null)
			return ".png";
		int dot = filename.lastIndexOf('.');
		return dot >= 0 ? filename.substring(dot) : ".png";
	}

	private User resolveUser(Authentication authentication) {
		if (authentication == null)
			return null;
		Object principal = authentication.getPrincipal();
		if (principal instanceof User u)
			return u;
		String userName = principal instanceof UserDetails ud ? ud.getUsername() : principal.toString();
		return userRepository.findByUserName(userName).orElse(null);
	}

}
