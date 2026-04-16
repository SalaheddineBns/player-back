package com.salah.mcpplayersservice.models;

public enum TestInvitationStatus {

	/** Invitation sent, awaiting player response */
	PENDING,

	/** Player accepted the invitation — a TestSlot was created */
	ACCEPTED,

	/** Player declined the invitation */
	DECLINED

}
