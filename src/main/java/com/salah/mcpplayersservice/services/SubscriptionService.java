package com.salah.mcpplayersservice.services;

import com.salah.mcpplayersservice.dto.request.SubscriptionRequest;
import com.salah.mcpplayersservice.dto.response.PlanInfoDto;
import com.salah.mcpplayersservice.dto.response.SubscriptionResponseDto;
import com.salah.mcpplayersservice.exceptions.RessourceNotFoundException;
import com.salah.mcpplayersservice.models.*;
import com.salah.mcpplayersservice.repository.SubscriptionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

	private final SubscriptionRepository subscriptionRepository;

	public SubscriptionService(SubscriptionRepository subscriptionRepository) {
		this.subscriptionRepository = subscriptionRepository;
	}

	public List<PlanInfoDto> getPublicPlans() {
		return List.of(buildPlanInfo(SubscriptionPlan.STARTER, false), buildPlanInfo(SubscriptionPlan.PRO, true),
				buildPlanInfo(SubscriptionPlan.ELITE, false));
	}

	@Transactional
	public SubscriptionResponseDto subscribe(SubscriptionRequest request, User user) {
		if (!(user instanceof Player player)) {
			throw new IllegalStateException("Only players can subscribe to a plan");
		}
		Optional<Subscription> existing = subscriptionRepository.findByPlayerUserIdAndStatus(player.getPlayerId(),
				SubscriptionStatus.ACTIVE);
		if (existing.isPresent()) {
			Subscription current = existing.get();
			current.setStatus(SubscriptionStatus.CANCELLED);
			subscriptionRepository.save(current);
		}
		Subscription subscription = Subscription.builder()
			.plan(request.plan())
			.status(SubscriptionStatus.ACTIVE)
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusMonths(1))
			.player(player)
			.build();
		return toDto(subscriptionRepository.save(subscription));
	}

	@Transactional
	public void cancel(User user) {
		if (!(user instanceof Player player)) {
			throw new IllegalStateException("Only players can cancel a subscription");
		}
		Subscription subscription = subscriptionRepository
			.findByPlayerUserIdAndStatus(player.getPlayerId(), SubscriptionStatus.ACTIVE)
			.orElseThrow(() -> new RessourceNotFoundException("Subscription", "player", player.getPlayerId()));
		subscription.setStatus(SubscriptionStatus.CANCELLED);
		subscriptionRepository.save(subscription);
	}

	public Optional<SubscriptionResponseDto> getMySubscription(User user) {
		if (!(user instanceof Player player)) {
			return Optional.empty();
		}
		return subscriptionRepository.findByPlayerUserIdAndStatus(player.getPlayerId(), SubscriptionStatus.ACTIVE)
			.map(this::toDto);
	}

	@Scheduled(cron = "0 0 1 * * *")
	@Transactional
	public void expireSubscriptions() {
		List<Subscription> expired = subscriptionRepository.findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE,
				LocalDate.now());
		expired.forEach(s -> s.setStatus(SubscriptionStatus.EXPIRED));
		subscriptionRepository.saveAll(expired);
	}

	private SubscriptionResponseDto toDto(Subscription s) {
		PlanInfoDto info = buildPlanInfo(s.getPlan(), false);
		return new SubscriptionResponseDto(s.getId(), s.getPlan(), s.getStatus(), s.getStartDate(), s.getEndDate(),
				info.pricePerMonth(), info.maxVideos(), info.maxTrialApplicationsPerMonth(), info.canContactManager(),
				info.unlimitedVideos(), info.unlimitedTrials());
	}

	private PlanInfoDto buildPlanInfo(SubscriptionPlan plan, boolean popular) {
		return switch (plan) {
			case STARTER -> new PlanInfoDto(SubscriptionPlan.STARTER, "Starter", 10.0,
					List.of("Access to all team information", "Apply to up to 3 trials per month",
							"Share up to 5 videos", "Public player profile", "Browse team pages"),
					5, 3, false, false, false, popular);
			case PRO -> new PlanInfoDto(SubscriptionPlan.PRO, "Pro", 30.0,
					List.of("Everything in Starter", "Contact team managers directly", "Unlimited trial applications",
							"Share up to 15 videos", "Priority profile visibility", "Application status tracking"),
					15, Integer.MAX_VALUE, true, false, true, popular);
			case ELITE -> new PlanInfoDto(SubscriptionPlan.ELITE, "Elite", 50.0,
					List.of("Everything in Pro", "Unlimited video uploads", "Elite profile badge",
							"Featured in manager searches", "Advanced profile analytics", "Dedicated support channel"),
					Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, true, popular);
		};
	}

}
