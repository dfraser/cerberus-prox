/*
 * Copyright 2008 Dan Fraser
 *
 * This file is part of Cerberus-Prox.
 *
 * Cerberus-Prox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus-Prox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus-Prox.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */


/**
 * This class represents the allowed access of a user.
 */
package com.onestopmediagroup.doorsecurity;

/**
 * Bean to represent data from Card table in the database.
 * 
 * @author Dan Fraser
 *
 */
public class UserCard {

	/**
	 * The full, real name of the card's owner.
	 */
	private final String realName;
	
	/**
	 * The nickname or username of the card's owner
	 */
	private final String nickName;
	
	/**
	 * Whether or not this user has after-hours access to the resource
	 */
	private final boolean afterHoursAllowed;
	
	/**
	 * Whether or not this user has "magic" access.
	 */
	private final boolean magic;

	/**
	 * Creates a new AccessInfo object. These objects are created by the AccessVerifier
	 * in response to an access request.  This is a test change!
	 * 
	 * @param realName the user's real name
	 * @param nickName the user's nickname
	 * @param afterHours whether or not the user is allowed in after hours
	 * @param magic whether or not the user has "magic" access
	 */
	protected UserCard(String realName, String nickName, boolean afterHours, boolean magic) {
		this.realName = realName;
		this.nickName = nickName;
		this.afterHoursAllowed = afterHours;
		this.magic = magic;
	}

	public String getRealName() {
		return realName;
	}

	public String getNickName() {
		return nickName;
	}

	public boolean isAfterHoursAllowed() {
		return afterHoursAllowed;
	}

	public boolean isMagic() {
		return magic;
	}

	

}
