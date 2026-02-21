package com.salah.mcpplayersservice.services;

import com.salah.mcpplayersservice.dto.request.PublicationRequest;
import com.salah.mcpplayersservice.exceptions.RessourceNotFoundException;
import com.salah.mcpplayersservice.models.Publication;
import com.salah.mcpplayersservice.models.PublicationType;
import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.repository.PublicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PublicationService {

	private final PublicationRepository publicationRepository;

	public PublicationService(PublicationRepository publicationRepository) {
		this.publicationRepository = publicationRepository;
	}

	@Transactional
	public Publication createPublication(PublicationRequest request, User user) {
		Team team = user.getTeam();
		if (team == null) {
			throw new IllegalStateException("User is not associated with a team");
		}

		Publication publication = Publication.builder()
			.title(request.title())
			.content(request.content())
			.publicationType(request.publicationType())
			.team(team)
			.build();

		return publicationRepository.save(publication);
	}

	public List<Publication> getAllPublications() {
		return publicationRepository.findAllByOrderByCreatedAtDesc();
	}

	public List<Publication> getMyPublications(User user) {
		Team team = user.getTeam();
		if (team == null) {
			throw new IllegalStateException("User is not associated with a team");
		}
		return publicationRepository.findByTeamTeamIdOrderByCreatedAtDesc(team.getTeamId());
	}

	@Transactional
	public void deletePublication(UUID publicationId, User user) {
		Publication publication = publicationRepository.findById(publicationId)
			.orElseThrow(() -> new RessourceNotFoundException("Publication", "id", publicationId));

		Team team = user.getTeam();
		if (team == null || !team.getTeamId().equals(publication.getTeam().getTeamId())) {
			throw new IllegalStateException("You can only delete your own team's publications");
		}

		publicationRepository.delete(publication);
	}

}
