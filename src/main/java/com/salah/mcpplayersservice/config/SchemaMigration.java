package com.salah.mcpplayersservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Configuration
public class SchemaMigration {

	private static final Logger log = LoggerFactory.getLogger(SchemaMigration.class);

	@Bean
	public ApplicationRunner dropTeamNullableConstraints(DataSource dataSource) {
		return args -> {
			try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
				stmt.execute("ALTER TABLE teams ALTER COLUMN city DROP NOT NULL");
				stmt.execute("ALTER TABLE teams ALTER COLUMN country DROP NOT NULL");
				log.info("Schema migration: city/country columns set to nullable");
			}
			catch (Exception e) {
				log.debug("Schema migration skipped (columns may already be nullable or table not yet created): {}",
						e.getMessage());
			}
		};
	}

}
