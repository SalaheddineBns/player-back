package com.salah.mcpplayersservice.models;

public enum TestSlotStatus {

	/** Slot confirmed, test not yet done */
	SCHEDULED,

	/** Manager cancelled the appointment */
	CANCELLED_BY_MANAGER,

	/** Player cancelled the appointment */
	CANCELLED_BY_PLAYER,

	/** Test took place — result may be published */
	COMPLETED

}
