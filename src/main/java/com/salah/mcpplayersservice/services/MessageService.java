package com.salah.mcpplayersservice.services;

import com.salah.mcpplayersservice.dto.response.ConversationResponseDto;
import com.salah.mcpplayersservice.dto.response.MessageResponseDto;
import com.salah.mcpplayersservice.exceptions.RessourceNotFoundException;
import com.salah.mcpplayersservice.models.Conversation;
import com.salah.mcpplayersservice.models.Message;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.repository.ConversationRepository;
import com.salah.mcpplayersservice.repository.MessageRepository;
import com.salah.mcpplayersservice.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

	private final ConversationRepository conversationRepository;

	private final MessageRepository messageRepository;

	private final UserRepository userRepository;

	public MessageService(ConversationRepository conversationRepository, MessageRepository messageRepository,
			UserRepository userRepository) {
		this.conversationRepository = conversationRepository;
		this.messageRepository = messageRepository;
		this.userRepository = userRepository;
	}

	public Conversation getOrCreateConversation(User user1, User user2) {
		return conversationRepository.findByParticipants(user1, user2).orElseGet(() -> {
			Conversation conv = Conversation.builder()
				.participantOne(user1)
				.participantTwo(user2)
				.lastMessageAt(LocalDateTime.now())
				.build();
			return conversationRepository.save(conv);
		});
	}

	@Transactional
	public Message sendMessage(User sender, UUID receiverId, String content) {
		User receiver = userRepository.findByPlayerId(receiverId)
			.or(() -> userRepository.findById(receiverId))
			.orElseThrow(() -> new RessourceNotFoundException("User", "id", receiverId));

		Conversation conversation = getOrCreateConversation(sender, receiver);

		Message message = Message.builder()
			.conversation(conversation)
			.sender(sender)
			.receiver(receiver)
			.content(content)
			.sentAt(LocalDateTime.now())
			.read(false)
			.build();

		conversation.setLastMessageAt(message.getSentAt());
		conversationRepository.save(conversation);

		return messageRepository.save(message);
	}

	public List<ConversationResponseDto> getConversations(User user) {
		return conversationRepository.findByParticipant(user).stream().map(conv -> {
			User other = conv.getParticipantOne().getUserId().equals(user.getUserId()) ? conv.getParticipantTwo()
					: conv.getParticipantOne();
			long unread = messageRepository.countUnread(conv.getConversationId(), user.getUserId());
			Page<Message> lastPage = messageRepository
				.findByConversationConversationIdOrderBySentAtDesc(conv.getConversationId(), PageRequest.of(0, 1));
			String lastMsg = lastPage.hasContent() ? lastPage.getContent().get(0).getContent() : "";
			return new ConversationResponseDto(conv.getConversationId(), other.getUserId(), other.getUsername(),
					lastMsg, conv.getLastMessageAt(), unread);
		}).toList();
	}

	public Page<MessageResponseDto> getMessages(UUID conversationId, User user, int page, int size) {
		Conversation conv = conversationRepository.findById(conversationId)
			.orElseThrow(() -> new RessourceNotFoundException("Conversation", "id", conversationId));
		if (!conv.getParticipantOne().getUserId().equals(user.getUserId())
				&& !conv.getParticipantTwo().getUserId().equals(user.getUserId())) {
			throw new RessourceNotFoundException("Conversation", "id", conversationId);
		}
		return messageRepository
			.findByConversationConversationIdOrderBySentAtDesc(conversationId, PageRequest.of(page, size))
			.map(msg -> new MessageResponseDto(msg.getMessageId(), msg.getSender().getUserId(),
					msg.getSender().getUsername(), msg.getReceiver().getUserId(), msg.getReceiver().getUsername(),
					msg.getContent(), msg.getSentAt(), msg.isRead()));
	}

	@Transactional
	public void markAsRead(UUID conversationId, User user) {
		messageRepository.markAllAsRead(conversationId, user.getUserId());
	}

	public MessageResponseDto toMessageResponseDto(Message msg) {
		return new MessageResponseDto(msg.getMessageId(), msg.getSender().getUserId(), msg.getSender().getUsername(),
				msg.getReceiver().getUserId(), msg.getReceiver().getUsername(), msg.getContent(), msg.getSentAt(),
				msg.isRead());
	}

}
