package com.salah.mcpplayersservice.config;

import com.salah.mcpplayersservice.models.RequiredDocument;
import com.salah.mcpplayersservice.repository.RequiredDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the standard documents a player must provide when accepted after a test (MA-R-1).
 * Runs on startup; skips any document that already exists.
 */
@Component
public class DocumentDataSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DocumentDataSeeder.class);

	private final RequiredDocumentRepository documentRepository;

	public DocumentDataSeeder(RequiredDocumentRepository documentRepository) {
		this.documentRepository = documentRepository;
	}

	@Override
	public void run(String... args) {
		List<RequiredDocument> documents = List.of(
				RequiredDocument.builder()
					.title("Pièce d'identité")
					.description("Carte nationale d'identité ou passeport en cours de validité.")
					.build(),
				RequiredDocument.builder()
					.title("Ancienne licence")
					.description("Copie de la dernière licence sportive valide.")
					.build(),
				RequiredDocument.builder()
					.title("CV sportif")
					.description("Curriculum vitae détaillant le parcours et les expériences sportives du joueur.")
					.build(),
				RequiredDocument.builder()
					.title("Certificat médical")
					.description("Certificat médical d'aptitude à la pratique du football de moins de 3 mois.")
					.build(),
				RequiredDocument.builder()
					.title("Photo d'identité")
					.description("2 photos d'identité récentes au format réglementaire.")
					.build());

		for (RequiredDocument doc : documents) {
			if (!documentRepository.existsByTitle(doc.getTitle())) {
				documentRepository.save(doc);
				log.info("[DocumentDataSeeder] seeded document: {}", doc.getTitle());
			}
		}
	}

}
