package com.onestopmediagroup.doorsecurity;

import java.util.EventObject;

@SuppressWarnings("serial")
public class DoorAccessEvent extends EventObject {

	private final UserCard userCard;
	private final boolean allowed;
	private final String doorName;
	private final String cardId;
			
	public DoorAccessEvent(Object source, String cardId, UserCard userCard, boolean allowed, String doorName) {
		super(source);
		this.cardId = cardId;
		this.userCard = userCard;
		this.allowed = allowed;
		this.doorName = doorName;
	}

	public UserCard getUserCard() {
		return userCard;
	}
	
	public boolean isAllowed() {
		return allowed;
	}
	
	public String getDoorName() {
		return doorName;
	}
	
	public String getCardId() {
		return cardId;
	}
}
