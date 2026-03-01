package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.TrialCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrialCandidateRepository extends JpaRepository<TrialCandidate, UUID> {

	Optional<TrialCandidate> findByTrialTrialIdAndPlayerPlayerId(UUID trialId, UUID playerId);

	List<TrialCandidate> findByTrialTrialId(UUID trialId);

	List<TrialCandidate> findByPlayerPlayerId(UUID playerId);

}
