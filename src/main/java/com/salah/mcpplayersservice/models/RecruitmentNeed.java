package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recruitment_needs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentNeed {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	private UUID id;

	@NotBlank(message = "Position cannot be empty")
	private String position;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

}
