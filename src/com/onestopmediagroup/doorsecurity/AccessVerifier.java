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

package com.onestopmediagroup.doorsecurity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This class handles communication with the database, an offline cache of the 
 * current access database, and verification of a specific credential against 
 * a specific door.
 * 
 * @author dfraser
 *
 */
public class AccessVerifier {

	private static Logger log = Logger.getLogger(AccessVerifier.class);
	private static Logger logFriendly = Logger.getLogger("Friendly");
	 
	
	/**
	 * The door identifier that we are checking access for.
	 */
	private final String doorName;
	
	/**
	 * Data storage for the access control database cache.
	 */
	private Map<String,UserCard> doorCache = new HashMap<String,UserCard>();
	
	/**
	 * "Double-buffer" so we can load a new cache without throwing out the old one.
	 */
	private Map<String,UserCard> newCache;
	
	/**
	 * Whether or not the current door is being forced unlocked by 
	 * database configuration.
	 */
	boolean forceUnlocked = false;
	
	/**
	 * "Double-buffer" so we can load state without throwing out the old one.
	 */
	boolean newForceUnlocked = false;


	private final Session session;
	
	/**
	 * Creates a new AccessVerifier object for a specific door, and loads its cache.
	 * 
	 * @param doorName the door identifier that we are controlling access for  
	 * @param dbUrl the JDBC user for our SQL database
	 * @param dbDriver the JDBC driver classname for our SQL database
	 */
	/** TODO: dbUrl and dbDriver are invalid above */
	public AccessVerifier(String doorName, Session session) {
		this.doorName = doorName;
		this.session = session;
		log.debug("currently after hours: "+isAfterHours(session.getAfterHoursStart(), session.getAfterHoursEnd()));
		try {
			Class.forName(session.getDbDriver()).newInstance();
		} catch (Exception e) {
			System.out.println("couldn't load class: "+e.getMessage());
		}
		updateCache();
		Thread crt = new CacheReloadThread();
		crt.start();
	}
	
	/**
	 * Sets whether or not this door should default to an unlocked state. 
	 * If this is set to true, the door will unlock and remain unlocked until 
	 * the state is changed.
	 * 
	 * @param state whether or not the door should default to an unlocked state.
	 */
	public void setDefaultUnlocked(boolean state) {
		// replace newcache with a fresh hashmap so we don't overwrite the old one
		newCache = new HashMap<String,UserCard>();
		Connection con = null;
		try {
			con = DriverManager.getConnection(session.getDbUrl());
			PreparedStatement pstmt = null;
			pstmt = con.prepareStatement("update door "
					+"set default_unlocked = ? "
					+"WHERE door.name = ? ");
			pstmt.setString(1, state ? "Y" : "N");
			pstmt.setString(2, doorName);
			pstmt.execute();
			forceUnlocked = state;
		} catch (SQLException e) {
			log.error("database error updating cache for door "+doorName+": "+e.getMessage(),e);
		} finally {
			try {
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				// nothing to do
			}
		}
		
	}
	
	/**
	 * Updates newCache with data from our SQL database.
	 */
	private void updateCache() {
		// replace newcache with a fresh hashmap so we don't overwrite the old one
		newCache = new HashMap<String,UserCard>();
		Connection con = null;
		try {
			log.trace("loading cache for door: "+doorName);
			con = DriverManager.getConnection(session.getDbUrl());
			PreparedStatement pstmt = null;
    		pstmt = con.prepareStatement("SELECT card_id, user, nick, after_hours, magic "
    				+"FROM card, door_access, door "
    				+"WHERE card.access_group_id = door_access.access_group_id "
    				+"AND door_access.door_id = door.id "
    				+"AND door.name = ? "
    				+"AND (card.expires is null or card.expires > now( )) "
    				+"AND (card.valid_from is null or card.valid_from < now()) "
    				+"AND card.disabled = 'N' ");
    		pstmt.setString(1, doorName);
    		ResultSet rs = pstmt.executeQuery();
    		while (rs.next()) {
    			newCache.put(rs.getString("card_id"),
    					new UserCard(rs.getString("user"), 
    							rs.getString("nick"), 
    							"Y".equals(rs.getString("after_hours")),
    							"Y".equals(rs.getString("magic")
    					)));
    		}
    		rs.close();
    		pstmt.close();
    		pstmt = con.prepareStatement("SELECT default_unlocked "
    				+"FROM door "
    				+"WHERE door.name = ? ");
    		pstmt.setString(1, doorName);
    		rs = pstmt.executeQuery();
    		while (rs.next()) {
    			newForceUnlocked = "Y".equals(rs.getString(1)) ? true : false;
    		}
    		swapCache();
    		
		} catch (SQLException e) {
			log.error("database error updating cache for door "+doorName+": "+e.getMessage(),e);
		} finally {
			try {
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				// nothing to do
			}
		}
	}
	
	/**
	 * Swaps the buffers in our double-buffered cache.  This is intended to be instanenous, 
	 * so we don't have to block for database access at any time.
	 */
	private synchronized void swapCache() {
		doorCache = newCache;
		forceUnlocked = newForceUnlocked;
	}
	
	
	/**
	 * Checks the access to this door for a given card id.
	 * 
	 * @param cardId the card id to check
	 * @return a UserCard object representing the user, or null if access was denied.
	 */
	public synchronized UserCard checkAccess(String cardId) {
		UserCard user = doorCache.get(cardId);
		if (user != null) {
			boolean currentlyAfterHours = isAfterHours(session.getAfterHoursStart(), session.getAfterHoursEnd());
			if (currentlyAfterHours == true && user.isAfterHoursAllowed() == false) { 
				return null;
			}
		}
		return user;
	}
	
	/**
	 * Class to handle automatic reloading of the door access cache.
	 * 
	 * @author dfraser
	 *
	 */
	public class CacheReloadThread extends Thread {
		
		@Override
		public void run() {
			while (!this.isInterrupted()) {
				try { 
					Thread.sleep(session.getCacheReloadSeconds()*1000); // every 5 minutes
					updateCache();
				} catch (InterruptedException e) {
					return;
				}		
			}
		}
	}
	
	/**
	 * Provides a simple access to logging back to the SQL database.
	 * 
	 * @param cardId the card id which is the subject of this log entry
	 * @param allowed whether the action was allowed or denied
	 * @param detail a detail message regarding the log entry
	 */
	public void logAccess(String cardId, boolean allowed, UserCard user, String detail) {
		Connection con = null;
		try {
			log.info(cardId+","+doorName+","+(allowed?"allowed":"denied")+","+detail);
			if (allowed && user != null) {
				String name;
				if (session.isFriendlyLogRealName()) {
					name = user.getRealName();
				} else {
					name = user.getNickName();
				}
				logFriendly.info(name+" has entered.");
			}
			con = DriverManager.getConnection(session.getDbUrl());
			PreparedStatement pstmt = null;
    		pstmt = con.prepareStatement("insert into access_log (logged, card_id, action, door, detail) values (now(),?,?,?,?)");
    		pstmt.setString(1, cardId);
    		pstmt.setString(2, allowed ? "ALLOW" : "DENY");
    		pstmt.setString(3, doorName);
    		pstmt.setString(4, detail);
    		pstmt.execute();
		} catch (SQLException e) {
			log.error("database error adding log: "+e.getMessage(),e);
		} finally {
			try {
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				// nothing to do
			}
		}	
	}
	
	/** 
	 * Checks if it's after hours or not.  Assumes Saturday and Sunday are after hours.
	 * If "after hours" has not been configured or is disabled, this will always return false.
 	 *
	 * @param startTime the time at which after hours starts.  Times are an integer representing 24-hour time as HHmm.
	 * @param endTime the time at which after hours ends.
	 * @return true if current time falls within any TimePairs.  false otherwise.
	 */
	private boolean isAfterHours(int startTime, int endTime) {
		if (!session.isAfterHoursEnabled()) {
			log.debug("after hours is disabled, returning false");
			return false;
		}
		
		Calendar cal = Calendar.getInstance();
		
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			log.debug("weekend, after hours is true");
			return true;
		}
		
		int currentTime = cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE);
		log.debug("after hours test: startime is "+startTime+" endtime is: "+endTime+" current time is: "+currentTime);
							
	  	if (startTime > endTime) {
			// crossing midnight, special test
			if ( (startTime <= currentTime && currentTime <= 2359) ||
				 (0 <= currentTime && currentTime < endTime)) {
				 	return true;
			}
		} else {
			// simple case, not crossing midnight
			if (currentTime >= startTime && currentTime < endTime) {
				return true;
			}
		}
	  	
	  	return false;
	}
	
	
}


