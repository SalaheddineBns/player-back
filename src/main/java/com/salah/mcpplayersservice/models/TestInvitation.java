package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "test_invitations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestInvitation {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	private UUID invitationId;

	/** The TEAM_MANAGER user who sent the invitation */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "manager_id", nullable = false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private User manager;

	/** The PLAYER user who received the invitation */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_user_id", nullable = false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private User player;

	@NotNull
	@Column(nullable = false)
	private LocalDateTime proposedDate;

	/** Optional message from manager explaining the context */
	@Column(columnDefinition = "TEXT")
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private TestInvitationStatus status = TestInvitationStatus.PENDING;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private LocalDateTime respondedAt;

	/** The slot created when player accepts — null until accepted */
	@OneToOne(mappedBy = "invitation", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private TestSlot slot;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

}
