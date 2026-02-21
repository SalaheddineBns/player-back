package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PublicationRepository extends JpaRepository<Publication, UUID> {

	List<Publication> findAllByOrderByCreatedAtDesc();

	List<Publication> findByTeamTeamIdOrderByCreatedAtDesc(UUID teamId);

}
