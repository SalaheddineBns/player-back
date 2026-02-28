package com.salah.mcpplayersservice.controllers;

import com.salah.mcpplayersservice.dto.request.SendMessageRequest;
import com.salah.mcpplayersservice.dto.response.ConversationResponseDto;
import com.salah.mcpplayersservice.dto.response.MessageResponseDto;
import com.salah.mcpplayersservice.models.Message;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.repository.UserRepository;
import com.salah.mcpplayersservice.services.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messages", description = "Messaging endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

	private final MessageService messageService;

	private final UserRepository userRepository;

	private final SimpMessagingTemplate messagingTemplate;

	public MessageController(MessageService messageService, UserRepository userRepository,
			SimpMessagingTemplate messagingTemplate) {
		this.messageService = messageService;
		this.userRepository = userRepository;
		this.messagingTemplate = messagingTemplate;
	}

	@Operation(summary = "Get conversations", description = "Returns all conversations for the current user")
	@GetMapping("/conversations")
	public ResponseEntity<List<ConversationResponseDto>> getConversations(Authentication authentication) {
		User user = resolveUser(authentication);
		if (user == null) {
			return ResponseEntity.status(401).build();
		}
		return ResponseEntity.ok(messageService.getConversations(user));
	}

	@Operation(summary = "Get messages", description = "Returns messages for a conversation")
	@GetMapping("/conversations/{conversationId}")
	public ResponseEntity<Page<MessageResponseDto>> getMessages(@PathVariable UUID conversationId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			Authentication authentication) {
		User user = resolveUser(authentication);
		if (user == null) {
			return ResponseEntity.status(401).build();
		}
		return ResponseEntity.ok(messageService.getMessages(conversationId, user, page, size));
	}

	@Operation(summary = "Send message", description = "Sends a message to another user")
	@PostMapping("/send")
	public ResponseEntity<MessageResponseDto> sendMessage(@RequestBody SendMessageRequest request,
			Authentication authentication) {
		User user = resolveUser(authentication);
		if (user == null) {
			return ResponseEntity.status(401).build();
		}
		Message msg = messageService.sendMessage(user, request.receiverId(), request.content());
		MessageResponseDto dto = messageService.toMessageResponseDto(msg);

		messagingTemplate.convertAndSendToUser(msg.getReceiver().getUsername(), "/queue/messages", dto);

		return ResponseEntity.ok(dto);
	}

	@Operation(summary = "Mark as read", description = "Marks all messages in a conversation as read")
	@PutMapping("/conversations/{conversationId}/read")
	public ResponseEntity<Void> markAsRead(@PathVariable UUID conversationId, Authentication authentication) {
		User user = resolveUser(authentication);
		if (user == null) {
			return ResponseEntity.status(401).build();
		}
		messageService.markAsRead(conversationId, user);
		return ResponseEntity.ok().build();
	}

	private User resolveUser(Authentication authentication) {
		if (authentication == null || authentication.getPrincipal() == null) {
			return null;
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof User userPrincipal) {
			return userPrincipal;
		}
		String userName;
		if (principal instanceof UserDetails userDetails) {
			userName = userDetails.getUsername();
		}
		else {
			userName = principal.toString();
		}
		return userRepository.findByUserName(userName).orElse(null);
	}

}
