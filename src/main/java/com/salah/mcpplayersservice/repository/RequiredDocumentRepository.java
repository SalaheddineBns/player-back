package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.RequiredDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequiredDocumentRepository extends JpaRepository<RequiredDocument, UUID> {

	boolean existsByTitle(String title);

}
