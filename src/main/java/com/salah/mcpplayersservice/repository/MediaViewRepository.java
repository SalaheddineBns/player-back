package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.MediaView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MediaViewRepository extends JpaRepository<MediaView, UUID> {

	List<MediaView> findByMediaMediaId(UUID mediaId);

	long countByMediaMediaId(UUID mediaId);

}
