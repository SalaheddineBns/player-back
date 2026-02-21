package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "media")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Media {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	private UUID mediaId;

	@NotBlank(message = "Title cannot be empty")
	private String title;

	private String description;

	@NotNull(message = "Media type cannot be null")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MediaType mediaType;

	@NotBlank(message = "File path cannot be empty")
	private String filePath;

	@NotBlank(message = "File name cannot be empty")
	private String fileName;

	private String contentType;

	private Long fileSize;

	@Column(nullable = false)
	private LocalDateTime uploadDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id", nullable = false)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Player player;

	@OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<MediaView> views = new ArrayList<>();

}
