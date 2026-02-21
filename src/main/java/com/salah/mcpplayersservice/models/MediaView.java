package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "media_views", uniqueConstraints = @UniqueConstraint(columnNames = { "media_id", "team_id" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaView {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	private UUID viewId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "media_id", nullable = false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@NotNull
	private Media media;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@NotNull
	private Team team;

	@Column(nullable = false)
	private LocalDateTime viewedAt;

}
