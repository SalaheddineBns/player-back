package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.Team;
import com.salah.mcpplayersservice.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {

	boolean existsByTeamName(String teamName);

	Optional<Team> findByTeamName(String teamName);

	Page<Team> findByTeamNameContainingIgnoreCase(String keyword, Pageable pageable);

	@Query("SELECT t FROM Team t WHERE "
			+ "(:keyword = '' OR LOWER(t.teamName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
			+ "(:city = '' OR LOWER(t.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND "
			+ "(:country = '' OR LOWER(t.country) LIKE LOWER(CONCAT('%', :country, '%')))")
	Page<Team> searchTeams(@Param("keyword") String keyword, @Param("city") String city,
			@Param("country") String country, Pageable pageable);

	Optional<Team> findByUser(User user);

	@Query("SELECT DISTINCT t.country FROM Team t WHERE t.country IS NOT NULL ORDER BY t.country")
	List<String> findDistinctCountries();

	@Query("SELECT DISTINCT t.city FROM Team t WHERE t.city IS NOT NULL AND (:country = '' OR LOWER(t.country) = LOWER(:country)) ORDER BY t.city")
	List<String> findDistinctCitiesByCountry(@Param("country") String country);

}
