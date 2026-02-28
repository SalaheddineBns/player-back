package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversations",
		uniqueConstraints = { @UniqueConstraint(columnNames = { "participant_one_id", "participant_two_id" }) })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	private UUID conversationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "participant_one_id", nullable = false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private User participantOne;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "participant_two_id", nullable = false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private User participantTwo;

	private LocalDateTime lastMessageAt;

}
