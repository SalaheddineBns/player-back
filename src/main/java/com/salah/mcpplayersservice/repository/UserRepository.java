package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByUserName(String userName);

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByUserName(String userName);

}
