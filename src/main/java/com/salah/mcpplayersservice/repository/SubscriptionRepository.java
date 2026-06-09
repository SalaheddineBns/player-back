package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.Subscription;
import com.salah.mcpplayersservice.models.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

	Optional<Subscription> findByPlayerUserIdAndStatus(UUID playerId, SubscriptionStatus status);

	Optional<Subscription> findByPlayerUserId(UUID playerId);

	List<Subscription> findByStatusAndEndDateBefore(SubscriptionStatus status, LocalDate date);

}
