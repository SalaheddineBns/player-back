package com.salah.mcpplayersservice.controllers;

import com.salah.mcpplayersservice.dto.request.SubscriptionRequest;
import com.salah.mcpplayersservice.dto.response.PlanInfoDto;
import com.salah.mcpplayersservice.dto.response.SubscriptionResponseDto;
import com.salah.mcpplayersservice.models.User;
import com.salah.mcpplayersservice.services.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscriptions", description = "Player subscription plan endpoints")
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	public SubscriptionController(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	@Operation(summary = "Get all available plans", description = "Public — no auth required")
	@GetMapping("/plans")
	public ResponseEntity<List<PlanInfoDto>> getPlans() {
		return ResponseEntity.ok(subscriptionService.getPublicPlans());
	}

	@Operation(summary = "Subscribe to a plan", description = "Player subscribes or switches plans")
	@PostMapping("/subscribe")
	@PreAuthorize("hasRole('PLAYER')")
	public ResponseEntity<SubscriptionResponseDto> subscribe(@Valid @RequestBody SubscriptionRequest request,
			@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(subscriptionService.subscribe(request, user));
	}

	@Operation(summary = "Cancel active subscription")
	@DeleteMapping("/cancel")
	@PreAuthorize("hasRole('PLAYER')")
	public ResponseEntity<Void> cancel(@AuthenticationPrincipal User user) {
		subscriptionService.cancel(user);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Get current player's active subscription")
	@GetMapping("/me")
	@PreAuthorize("hasRole('PLAYER')")
	public ResponseEntity<SubscriptionResponseDto> getMySubscription(@AuthenticationPrincipal User user) {
		return subscriptionService.getMySubscription(user)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.noContent().build());
	}

}
