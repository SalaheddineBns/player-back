package com.salah.mcpplayersservice.controllers;

import com.salah.mcpplayersservice.dto.request.PublicationRequest;
import com.salah.mcpplayersservice.dto.response.PublicationResponseDto;
import com.salah.mcpplayersservice.mappers.PublicationMapper;
import com.salah.mcpplayersservice.models.Publication;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.services.PublicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/publications")
@Tag(name = "Publications", description = "Team publication endpoints")
public class PublicationController {

	private final PublicationService publicationService;

	private final PublicationMapper publicationMapper;

	public PublicationController(PublicationService publicationService, PublicationMapper publicationMapper) {
		this.publicationService = publicationService;
		this.publicationMapper = publicationMapper;
	}

	@Operation(summary = "Create a publication",
			description = "Creates a new publication for the team (TEAM_MANAGER only)")
	@PostMapping
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<PublicationResponseDto> createPublication(@Valid @RequestBody PublicationRequest request,
			@AuthenticationPrincipal User user) {
		Publication publication = publicationService.createPublication(request, user);
		return ResponseEntity.status(HttpStatus.CREATED).body(publicationMapper.toPublicationResponseDto(publication));
	}

	@Operation(summary = "Get all publications", description = "Returns all publications for the feed")
	@GetMapping
	public ResponseEntity<List<PublicationResponseDto>> getAllPublications() {
		List<Publication> publications = publicationService.getAllPublications();
		return ResponseEntity.ok(publicationMapper.toPublicationResponseDtoList(publications));
	}

	@Operation(summary = "Get my team's publications",
			description = "Returns publications for the current user's team (TEAM_MANAGER only)")
	@GetMapping("/me")
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<List<PublicationResponseDto>> getMyPublications(@AuthenticationPrincipal User user) {
		List<Publication> publications = publicationService.getMyPublications(user);
		return ResponseEntity.ok(publicationMapper.toPublicationResponseDtoList(publications));
	}

	@Operation(summary = "Delete a publication",
			description = "Deletes a publication owned by the user's team (TEAM_MANAGER only)")
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('TEAM_MANAGER')")
	public ResponseEntity<Void> deletePublication(@PathVariable UUID id, @AuthenticationPrincipal User user) {
		publicationService.deletePublication(id, user);
		return ResponseEntity.noContent().build();
	}

}
