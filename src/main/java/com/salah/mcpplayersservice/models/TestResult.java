package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "test_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	private UUID resultId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "slot_id", nullable = false, unique = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private TestSlot slot;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TestResultStatus status;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@Column(nullable = false, updatable = false)
	private LocalDateTime publishedAt;

	@PrePersist
	protected void onCreate() {
		this.publishedAt = LocalDateTime.now();
	}

}
