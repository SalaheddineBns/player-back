package com.salah.mcpplayersservice.repository;

import com.salah.mcpplayersservice.models.Conversation;
import com.salah.mcpplayersservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

	@Query("SELECT c FROM Conversation c WHERE c.participantOne = :user OR c.participantTwo = :user ORDER BY c.lastMessageAt DESC")
	List<Conversation> findByParticipant(@Param("user") User user);

	@Query("SELECT c FROM Conversation c WHERE (c.participantOne = :u1 AND c.participantTwo = :u2) OR (c.participantOne = :u2 AND c.participantTwo = :u1)")
	Optional<Conversation> findByParticipants(@Param("u1") User u1, @Param("u2") User u2);

}
