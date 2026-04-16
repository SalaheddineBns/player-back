package com.salah.mcpplayersservice.notification;

public enum NotificationType {

	/** Manager receives this when a player applies for their trial */
	PLAYER_APPLIED,

	/** Player receives this when the manager updates their application status */
	STATUS_CHANGED,

	/** Manager receives this when a player withdraws their application */
	PLAYER_WITHDREW,

	// ── Test Invitation notifications ────────────────────────────────────────

	/** Player receives this when a manager sends them a private test invitation */
	TEST_INVITATION_RECEIVED,

	/** Manager receives this when a player accepts their invitation */
	TEST_INVITATION_ACCEPTED,

	/** Manager receives this when a player declines their invitation */
	TEST_INVITATION_DECLINED,

	/** Manager receives this when a player cancels a confirmed test slot */
	TEST_SLOT_CANCELLED_BY_PLAYER,

	/** Player receives this when the manager cancels a confirmed test slot */
	TEST_SLOT_CANCELLED_BY_MANAGER,

	/** Player receives this when the manager publishes the test result */
	TEST_RESULT_PUBLISHED

}
