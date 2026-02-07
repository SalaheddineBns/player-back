package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Entity
@Table(name = "players")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	UUID playerId;

	@NotBlank(message = "First name cannot be empty")
	String firstName;

	@NotBlank(message = "Last name cannot be empty")
	String lastName;

	@NotBlank(message = "Username cannot be empty")
	@Column(unique = true)
	String userName;

	@NotBlank(message = "Email cannot be empty")
	@Email(message = "Email must be valid")
	@Column(unique = true)
	String email;

	@NotBlank(message = "Password cannot be empty")
	String password;

	String position;

	String nationality;

	@ManyToOne(fetch = FetchType.LAZY)
	Team team;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(new SimpleGrantedAuthority("PLAYER"));
	}

	@Override
	public String getUsername() {
		return this.userName;
	}

}
