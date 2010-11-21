package com.onestopmediagroup.doorsecurity;

import java.util.Date;
import java.util.EventObject;

@SuppressWarnings("serial")
public class DoorAccessEvent extends EventObject {

	/**
	 * True if the access was allowed and the door was opened, false if it was denied
	 */
	private final boolean allowed;
	/**
	 * True if the card was unknown, false if it was found in the database
	 */
	private final boolean unknown;
	
	/**
	 * The name of the door that generated this access event
	 */
	private final String doorName;

	/**
	 * The card id that was read for this event
	 */
	private final String cardId;
	
	/**
	 * The full, real name of the card's owner, null if card was unknown
	 */
	private final String realName;
	
	/**
	 * The nickname or username of the card's owner, null if card was unknown
	 */
	private final String nickName;
	
	/**
	 * Whether or not this user has after-hours access to the resource, false if card was unknown
	 */
	private final boolean afterHoursAllowed;
	
	/**
	 * Whether or not this user has "magic" access, false if card was unknown
	 */
	private final boolean magic;
	
	/**
	 * The date and time the card was read
	 */
	private final Date timeRead;

	public DoorAccessEvent(Object source, Date timeRead, String cardId, boolean allowed, boolean unknown, String doorName, String realName, String nickName, boolean afterHoursAllowed, boolean magic) {
		super(source);
		this.cardId = cardId;
		this.allowed = allowed;
		this.doorName = doorName;
		this.afterHoursAllowed = afterHoursAllowed;
		this.magic = magic;
		this.nickName = nickName;
		this.realName = realName;
		this.unknown = unknown;
		this.timeRead = timeRead;
	}

	@Override
	public Object getSource() {
		// TODO Auto-generated method stub
		return super.getSource();
	}
	
	public Date getTimeRead() {
		// date is mutable, return defensive copy
		return new Date(timeRead.getTime());
	}
	
	public boolean isUnknown() {
		return unknown;
	}
	
	public boolean isAfterHoursAllowed() {
		return afterHoursAllowed;
	}
	
	public boolean isMagic() {
		return magic;
	}
	
	public String getNickName() {
		return nickName;
	}
	
	public String getRealName() {
		return realName;
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
