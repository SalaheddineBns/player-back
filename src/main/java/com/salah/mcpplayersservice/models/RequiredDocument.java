package com.salah.mcpplayersservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "required_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequiredDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@UuidGenerator
	private UUID documentId;

	@NotBlank
	@Column(nullable = false, unique = true)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

}
