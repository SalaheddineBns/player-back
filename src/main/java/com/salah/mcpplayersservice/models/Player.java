package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "players")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	private UUID playerId;

	@NotBlank(message = "First name cannot be empty")
	private String firstName;

	@NotBlank(message = "Last name cannot be empty")
	private String lastName;

	private String position;

	private String nationality;

	private String city;

	private String gender;

	private String preferredLeg;

	private Integer preferredNumber;

	@Enumerated(EnumType.STRING)
	@Column(columnDefinition = "varchar(255) default 'AVAILABLE'")
	@Builder.Default
	private PlayerStatus status = PlayerStatus.AVAILABLE;

	private String profilePictureUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id")
	private Team team;

	@OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<Media> mediaItems = new ArrayList<>();

	@OneToOne(mappedBy = "player", fetch = FetchType.LAZY)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private User user;

}
