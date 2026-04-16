package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "test_slots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSlot {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	private UUID slotId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "invitation_id", nullable = false, unique = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private TestInvitation invitation;

	@NotNull
	@Column(nullable = false)
	private LocalDateTime scheduledDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private TestSlotStatus status = TestSlotStatus.SCHEDULED;

	@Column(columnDefinition = "TEXT")
	private String cancellationReason;

	/** The user (manager or player) who cancelled, null if not cancelled */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cancelled_by_id")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private User cancelledBy;

	private LocalDateTime cancelledAt;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/** Result published after test — null until manager publishes */
	@OneToOne(mappedBy = "slot", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private TestResult result;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

}
