package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.TestInvitation;
import com.salah.mcpplayersservice.models.TestInvitationStatus;
import com.salah.mcpplayersservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestInvitationRepository extends JpaRepository<TestInvitation, UUID> {

	List<TestInvitation> findByManagerOrderByCreatedAtDesc(User manager);

	List<TestInvitation> findByPlayerOrderByCreatedAtDesc(User player);

	Optional<TestInvitation> findByManagerAndPlayerAndStatus(User manager, User player, TestInvitationStatus status);

}
