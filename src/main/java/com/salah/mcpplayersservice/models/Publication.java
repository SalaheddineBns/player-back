package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "publications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Publication {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	private UUID publicationId;

	@NotBlank(message = "Title cannot be empty")
	private String title;

	@NotBlank(message = "Content cannot be empty")
	@Column(columnDefinition = "TEXT")
	private String content;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PublicationType publicationType;

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
