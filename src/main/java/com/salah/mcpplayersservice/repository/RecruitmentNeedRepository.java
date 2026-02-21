package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.RecruitmentNeed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecruitmentNeedRepository extends JpaRepository<RecruitmentNeed, UUID> {

	List<RecruitmentNeed> findByTeamTeamIdOrderByCreatedAtDesc(UUID teamId);

}
