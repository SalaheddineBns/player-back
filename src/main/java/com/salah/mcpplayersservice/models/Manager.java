package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "managers")
@DiscriminatorValue("TEAM_MANAGER")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Manager extends User {

	private String profilePictureUrl;

	/** Alias for API compatibility. */
	public UUID getManagerId() {
		return getUserId();
	}

}
