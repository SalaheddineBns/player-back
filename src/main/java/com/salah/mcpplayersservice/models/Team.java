package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;
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
	@Column(nullable = false)
	private String teamName;

	@NotNull(message = "Date cannot be null")
	@PastOrPresent(message = "Date cannot be in the future")
	private Date dateCreated;

	@NotBlank(message = "Coach cannot be empty")
	private String coach;// to create a class for coach

	@OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
	private Set<Player> players;

}
