package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "team_subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = { "player_id", "team_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamSubscription {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	private LocalDateTime subscribedAt;

	@PrePersist
	public void prePersist() {
		this.subscribedAt = LocalDateTime.now();
	}

}
