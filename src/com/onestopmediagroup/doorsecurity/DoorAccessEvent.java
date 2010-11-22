package com.onestopmediagroup.doorsecurity;

import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="door_event")
public class DoorAccessEvent {

	/**
	 * True if the access was allowed and the door was opened, false if it was denied
	 */
	private boolean allowed;
	
	/**
	 * True if the card was unknown, false if it was found in the database
	 */
	private boolean unknown;
	
	/**
	 * The name of the door that generated this access event
	 */
	private String doorName;

	/**
	 * The card id that was read for this event
	 */
	private String cardId;
	
	/**
	 * The full, real name of the card's owner, null if card was unknown
	 */
	private String realName;
	
	/**
	 * The nickname or username of the card's owner, null if card was unknown
	 */
	private String nickName;
	
	/**
	 * Whether or not this user has "magic" access, false if card was unknown
	 */
	private boolean magic;
	
	/**
	 * The date and time the card was read
	 */
	private Date timeRead;

	public DoorAccessEvent() {
		
	}

	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}

	public void setUnknown(boolean unknown) {
		this.unknown = unknown;
	}

	public void setDoorName(String doorName) {
		this.doorName = doorName;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public void setMagic(boolean magic) {
		this.magic = magic;
	}

	public void setTimeRead(Date timeRead) {
		this.timeRead = new Date(timeRead.getTime());
	}

	public Date getTimeRead() {
		// date is mutable, return defensive copy
		return new Date(timeRead.getTime());
	}
	
	public boolean isUnknown() {
		return unknown;
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
