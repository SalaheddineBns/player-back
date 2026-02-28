package com.salah.mcpplayersservice.config;

import com.salah.mcpplayersservice.security.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

	private final JwtUtil jwtUtil;

	private final UserDetailsService userDetailsService;

	public WebSocketAuthInterceptor(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
			List<String> authHeaders = accessor.getNativeHeader("Authorization");
			if (authHeaders != null && !authHeaders.isEmpty()) {
				String token = authHeaders.get(0);
				if (token.startsWith("Bearer ")) {
					token = token.substring(7);
				}
				String username = jwtUtil.extractUsername(token);
				if (username != null) {
					UserDetails userDetails = userDetailsService.loadUserByUsername(username);
					if (jwtUtil.isTokenValid(token, userDetails)) {
						UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
								null, userDetails.getAuthorities());
						accessor.setUser(auth);
					}
				}
			}
		}
		return message;
	}

}
