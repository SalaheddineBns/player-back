package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trial_candidates", uniqueConstraints = @UniqueConstraint(columnNames = { "trial_id", "player_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrialCandidate {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	private UUID candidateId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trial_id", nullable = false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Trial trial;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "VARCHAR(50)")
	private TrialApplicationStatus status = TrialApplicationStatus.PENDING;

	private LocalDateTime appliedAt;

	private LocalDateTime statusUpdatedAt;

	@PrePersist
	protected void onCreate() {
		this.appliedAt = LocalDateTime.now();
		this.statusUpdatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.statusUpdatedAt = LocalDateTime.now();
	}

}
