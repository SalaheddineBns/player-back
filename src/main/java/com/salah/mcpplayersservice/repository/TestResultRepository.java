package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TestResultRepository extends JpaRepository<TestResult, UUID> {

	Optional<TestResult> findBySlotSlotId(UUID slotId);

}
