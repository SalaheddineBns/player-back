package com.salah.mcpplayersservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
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

	/**
	 * Migrates the schema from the old User→Player OneToOne design to the new JOINED
	 * inheritance (User ← Player, User ← Manager). Detects old schema by the presence of
	 * users.player_id and runs the full migration in a single transaction.
	 */
	@Bean
	public ApplicationRunner migrateToInheritanceSchema(DataSource dataSource) {
		return args -> {
			try (Connection conn = dataSource.getConnection()) {
				if (!columnExists(conn, "users", "player_id")) {
					log.debug("Inheritance schema migration: already applied, skipping.");
					return;
				}
				log.info("Inheritance schema migration: detected old schema, starting migration...");
				conn.setAutoCommit(false);
				try (Statement stmt = conn.createStatement()) {

					// 1. Add new columns to users
					stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS dtype varchar(31)");
					stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name varchar(255)");
					stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name varchar(255)");

					// 2. Populate from linked player rows
					stmt.execute("""
							UPDATE users u
							SET dtype      = CASE WHEN u.role = 'TEAM_MANAGER' THEN 'TEAM_MANAGER' ELSE 'PLAYER' END,
							    first_name = p.first_name,
							    last_name  = p.last_name
							FROM players p
							WHERE u.player_id = p.player_id
							  AND u.dtype IS NULL
							""");

					// 3. Enforce NOT NULL
					stmt.execute("ALTER TABLE users ALTER COLUMN dtype SET NOT NULL");
					stmt.execute("ALTER TABLE users ALTER COLUMN first_name SET NOT NULL");
					stmt.execute("ALTER TABLE users ALTER COLUMN last_name SET NOT NULL");

					// 4. Add user_id column to players (new JOINED-inheritance PK)
					stmt.execute("ALTER TABLE players ADD COLUMN IF NOT EXISTS user_id uuid");

					// 5. Populate players.user_id for PLAYER-role users
					stmt.execute("""
							UPDATE players p
							SET user_id = u.user_id
							FROM users u
							WHERE u.player_id = p.player_id
							  AND u.role = 'PLAYER'
							  AND p.user_id IS NULL
							""");

					// 6. Create manager rows for TEAM_MANAGER users
					stmt.execute("""
							INSERT INTO managers (user_id, profile_picture_url)
							SELECT u.user_id, NULL
							FROM users u
							WHERE u.role = 'TEAM_MANAGER'
							  AND NOT EXISTS (SELECT 1 FROM managers m WHERE m.user_id = u.user_id)
							""");

					// 7. Remap media.player_id (old player_id → new user_id)
					stmt.execute("ALTER TABLE media DROP CONSTRAINT IF EXISTS fkmd3c08rt8oxwu7qtxqmmj270r");
					stmt.execute("""
							UPDATE media m
							SET player_id = u.user_id
							FROM users u
							WHERE u.player_id = m.player_id
							""");

					// 8. Remap team_subscriptions.player_id
					stmt.execute(
							"ALTER TABLE team_subscriptions DROP CONSTRAINT IF EXISTS fkm2ev47gib66kw1wssuf0ih0ri");
					stmt.execute(
							"ALTER TABLE team_subscriptions DROP CONSTRAINT IF EXISTS ukni33e9oa32pcei0m8uvwnnrt8");
					stmt.execute("""
							UPDATE team_subscriptions ts
							SET player_id = u.user_id
							FROM users u
							WHERE u.player_id = ts.player_id
							""");

					// 9. Remap trial_candidates.player_id
					stmt.execute("ALTER TABLE trial_candidates DROP CONSTRAINT IF EXISTS fkntxdi8mlkjkvogwbe3qpo8gnt");
					stmt.execute("ALTER TABLE trial_candidates DROP CONSTRAINT IF EXISTS uk9hruq4712idsd9og63kahkf0j");
					stmt.execute("""
							UPDATE trial_candidates tc
							SET player_id = u.user_id
							FROM users u
							WHERE u.player_id = tc.player_id
							""");

					// 10. Drop users.player_id FK before deleting referenced player rows
					stmt.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS fk3a2a4ak4uk7fk6clrml2pgo5c");

					// 11. Delete player rows that belong to TEAM_MANAGER users
					stmt.execute("""
							DELETE FROM players p
							USING users u
							WHERE u.player_id = p.player_id
							  AND u.role = 'TEAM_MANAGER'
							""");

					stmt.execute("ALTER TABLE players DROP CONSTRAINT IF EXISTS players_pkey");

					// 12. Set new PK on players
					stmt.execute("ALTER TABLE players ALTER COLUMN user_id SET NOT NULL");
					stmt.execute("ALTER TABLE players ADD PRIMARY KEY (user_id)");
					stmt.execute(
							"ALTER TABLE players ADD CONSTRAINT fk_players_users FOREIGN KEY (user_id) REFERENCES users(user_id)");

					// 13. Drop old player_id, first_name, last_name from players
					stmt.execute("ALTER TABLE players DROP COLUMN IF EXISTS player_id");
					stmt.execute("ALTER TABLE players DROP COLUMN IF EXISTS first_name");
					stmt.execute("ALTER TABLE players DROP COLUMN IF EXISTS last_name");

					// 14. Drop player_id from users
					stmt.execute("ALTER TABLE users DROP COLUMN IF EXISTS player_id");

					// 15. Restore FK constraints pointing to new players.user_id
					stmt.execute(
							"ALTER TABLE media ADD CONSTRAINT fk_media_player FOREIGN KEY (player_id) REFERENCES players(user_id)");
					stmt.execute(
							"ALTER TABLE team_subscriptions ADD CONSTRAINT fk_sub_player FOREIGN KEY (player_id) REFERENCES players(user_id)");
					stmt.execute(
							"ALTER TABLE team_subscriptions ADD CONSTRAINT uk_sub_player_team UNIQUE (player_id, team_id)");
					stmt.execute(
							"ALTER TABLE trial_candidates ADD CONSTRAINT fk_trial_player FOREIGN KEY (player_id) REFERENCES players(user_id)");
					stmt.execute(
							"ALTER TABLE trial_candidates ADD CONSTRAINT uk_trial_player UNIQUE (trial_id, player_id)");

					conn.commit();
					log.info("Inheritance schema migration: completed successfully.");
				}
				catch (Exception ex) {
					conn.rollback();
					log.error("Inheritance schema migration: failed, rolled back. {}", ex.getMessage(), ex);
				}
				finally {
					conn.setAutoCommit(true);
				}
			}
			catch (Exception ex) {
				log.error("Inheritance schema migration: could not obtain connection. {}", ex.getMessage(), ex);
			}
		};
	}

	private boolean columnExists(Connection conn, String table, String column) throws Exception {
		try (ResultSet rs = conn.getMetaData().getColumns(null, null, table, column)) {
			return rs.next();
		}
	}

}
