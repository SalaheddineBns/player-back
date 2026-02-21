package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByUserName(String userName);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.player p LEFT JOIN FETCH p.team WHERE u.userName = :userName")
	Optional<User> findByUserNameWithPlayer(String userName);

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByUserName(String userName);

}