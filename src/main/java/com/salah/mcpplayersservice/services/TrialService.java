package com.salah.mcpplayersservice.services;

import com.salah.mcpplayersservice.dto.request.TrialRequest;
import com.salah.mcpplayersservice.exceptions.RessourceNotFoundException;
import com.salah.mcpplayersservice.models.*;
import com.salah.mcpplayersservice.notification.NotificationService;
import com.salah.mcpplayersservice.repository.TrialCandidateRepository;
import com.salah.mcpplayersservice.repository.TrialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TrialService {

	private final TrialRepository trialRepository;

	private final TrialCandidateRepository trialCandidateRepository;

	private final NotificationService notificationService;

	public TrialService(TrialRepository trialRepository, TrialCandidateRepository trialCandidateRepository,
			NotificationService notificationService) {
		this.trialRepository = trialRepository;
		this.trialCandidateRepository = trialCandidateRepository;
		this.notificationService = notificationService;
	}

	@Transactional
	public Trial createTrial(TrialRequest request, User user) {
		Team team = user.getTeam();
		if (team == null) {
			throw new IllegalStateException("User is not associated with a team");
		}
		Trial trial = Trial.builder()
			.location(request.location())
			.trialDate(request.trialDate())
			.position(request.position())
			.description(request.description())
			.team(team)
			.build();
		return trialRepository.save(trial);
	}

	public List<Trial> getAllTrials() {
		return trialRepository.findAllByOrderByTrialDateAsc();
	}

	public List<Trial> getMyTrials(User user) {
		Team team = user.getTeam();
		if (team == null) {
			throw new IllegalStateException("User is not associated with a team");
		}
		return trialRepository.findByTeamTeamIdOrderByTrialDateAsc(team.getTeamId());
	}

	@Transactional
	public void deleteTrial(UUID trialId, User user) {
		Trial trial = trialRepository.findById(trialId)
			.orElseThrow(() -> new RessourceNotFoundException("Trial", "id", trialId));
		Team team = user.getTeam();
		if (team == null || !team.getTeamId().equals(trial.getTeam().getTeamId())) {
			throw new IllegalStateException("You can only delete your own team's trials");
		}
		trialRepository.delete(trial);
	}

	@Transactional
	public TrialCandidate applyForTrial(UUID trialId, User user) {
		Trial trial = trialRepository.findById(trialId)
			.orElseThrow(() -> new RessourceNotFoundException("Trial", "id", trialId));
		Player player = user.getPlayer();
		if (player == null) {
			throw new IllegalStateException("User is not a player");
		}
		if (trialCandidateRepository.findByTrialTrialIdAndPlayerPlayerId(trialId, player.getPlayerId()).isPresent()) {
			throw new IllegalStateException("Already applied to this trial");
		}
		TrialCandidate candidate = new TrialCandidate();
		candidate.setTrial(trial);
		candidate.setPlayer(player);
		candidate.setStatus(TrialApplicationStatus.PENDING);
		TrialCandidate saved = trialCandidateRepository.save(candidate);

		User manager = trial.getTeam().getUser();
		if (manager != null) {
			String playerName = player.getFirstName() + " " + player.getLastName();
			notificationService.createNotification(manager, playerName, trial.getLocation(), trial.getTrialDate(),
					trial.getTrialId().toString());
		}

		return saved;
	}

	@Transactional
	public void withdrawApplication(UUID trialId, User user) {
		Player player = user.getPlayer();
		if (player == null) {
			throw new IllegalStateException("User is not a player");
		}
		TrialCandidate candidate = trialCandidateRepository
			.findByTrialTrialIdAndPlayerPlayerId(trialId, player.getPlayerId())
			.orElseThrow(() -> new RessourceNotFoundException("Application", "trial", trialId));
		trialCandidateRepository.delete(candidate);
	}

	public List<TrialCandidate> getCandidates(UUID trialId, User user) {
		Trial trial = trialRepository.findById(trialId)
			.orElseThrow(() -> new RessourceNotFoundException("Trial", "id", trialId));
		Team team = user.getTeam();
		if (team == null || !team.getTeamId().equals(trial.getTeam().getTeamId())) {
			throw new IllegalStateException("You can only view candidates for your own trials");
		}
		return trialCandidateRepository.findByTrialTrialId(trialId);
	}

	public List<UUID> getAppliedTrialIds(User user) {
		Player player = user.getPlayer();
		if (player == null) {
			throw new IllegalStateException("User is not a player");
		}
		return trialCandidateRepository.findByPlayerPlayerId(player.getPlayerId())
			.stream()
			.map(c -> c.getTrial().getTrialId())
			.toList();
	}

}
