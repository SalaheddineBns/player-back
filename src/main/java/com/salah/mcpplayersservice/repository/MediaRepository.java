package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {

	List<Media> findByPlayerPlayerIdOrderByUploadDateDesc(UUID playerId);

}
