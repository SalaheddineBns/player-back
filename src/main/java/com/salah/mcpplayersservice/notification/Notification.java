package com.salah.mcpplayersservice.notification;

import com.salah.mcpplayersservice.models.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String notificationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recipient_id", nullable = false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private User recipient;

	private String playerName;

	private String trialLocation;

	@Column(nullable = false)
	private LocalDateTime trialDate;

	private String trialId;

	@Column(nullable = false)
	@Builder.Default
	private boolean isRead = false;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

}
