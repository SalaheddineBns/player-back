package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "players")
@DiscriminatorValue("PLAYER")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Player extends User {

	private String position;

	private String nationality;

	private String city;

	private String gender;

	private String preferredLeg;

	private Integer preferredNumber;

	private String level;

	@Enumerated(EnumType.STRING)
	@Column(columnDefinition = "varchar(255) default 'AVAILABLE'")
	@Builder.Default
	private PlayerStatus status = PlayerStatus.AVAILABLE;

	private String profilePictureUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Team team;

	@OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Builder.Default
	private List<Media> mediaItems = new ArrayList<>();

	/** Alias so existing code using playerId still compiles. */
	public UUID getPlayerId() {
		return getUserId();
	}

	@Override
	public Team getTeam() {
		return team;
	}

}
