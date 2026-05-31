package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public abstract class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	@EqualsAndHashCode.Include
	private UUID userId;

	@NotBlank(message = "Username cannot be empty")
	@Column(unique = true, nullable = false)
	private String userName;

	@NotBlank(message = "Email cannot be empty")
	@Email(message = "Email must be valid")
	@Column(unique = true, nullable = false)
	private String email;

	@NotBlank(message = "Password cannot be empty")
	private String password;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@NotBlank(message = "First name cannot be empty")
	private String firstName;

	@NotBlank(message = "Last name cannot be empty")
	private String lastName;

	@OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
	@ToString.Exclude
	private Team managedTeam;

	public Team getTeam() {
		return managedTeam;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getUsername() {
		return this.userName;
	}

}
