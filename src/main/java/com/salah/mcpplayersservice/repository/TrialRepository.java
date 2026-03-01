package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.Trial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TrialRepository extends JpaRepository<Trial, UUID> {

	List<Trial> findAllByOrderByTrialDateAsc();

	List<Trial> findByTeamTeamIdOrderByTrialDateAsc(UUID teamId);

}
