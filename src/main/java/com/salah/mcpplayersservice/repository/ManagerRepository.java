package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ManagerRepository extends JpaRepository<Manager, UUID> {

}
