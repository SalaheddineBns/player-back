package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.TestSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TestSlotRepository extends JpaRepository<TestSlot, UUID> {

	Optional<TestSlot> findByInvitationInvitationId(UUID invitationId);

}
