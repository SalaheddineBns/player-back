package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "teams")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Team {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	private UUID teamId;

	@NotBlank(message = "Team name cannot be empty")
	@Column(unique = true, nullable = false)
	private String teamName;

	@NotNull(message = "Date cannot be null")
	@PastOrPresent(message = "Date cannot be in the future")
	private Date dateCreated;

	@NotBlank(message = "Coach cannot be empty")
	private String coach;

	private String logoUrl;

	private String division;

	@Column(columnDefinition = "TEXT")
	private String description;

	@OneToMany(mappedBy = "team")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<Player> players = new HashSet<>();

	@OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<Publication> publications = new ArrayList<>();

	@OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<RecruitmentNeed> recruitmentNeeds = new ArrayList<>();

	@NotNull(message = "Team must have an owner user")
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private User user;

}
