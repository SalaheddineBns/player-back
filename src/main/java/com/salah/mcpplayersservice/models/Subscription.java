package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SubscriptionPlan plan;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = false, unique = true)
	private Player player;

}
