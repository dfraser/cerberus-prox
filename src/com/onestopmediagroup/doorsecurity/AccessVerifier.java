package com.onestopmediagroup.doorsecurity;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;


public class AccessVerifier {

	private static Logger log = Logger.getLogger(AccessVerifier.class);
	  
	private final String doorName;
	Map<String,Boolean> doorCache = new HashMap<String,Boolean>();
	Map<String,Boolean> newCache;
	boolean forceUnlocked = false;
	boolean newForceUnlocked = false;

	private final String dbUrl;
	
	public AccessVerifier(String doorName, String dbUrl, String dbDriver) {
		this.doorName = doorName;
		this.dbUrl = dbUrl;
		try {
			Class.forName(dbDriver).newInstance();
		} catch (Exception e) {
			System.out.println("couldn't load class: "+e.getMessage());
		}
		updateCache();
		Thread crt = new CacheReloadThread();
		crt.start();
	}
	
	
	public void setDefaultUnlocked(boolean state) {
		// replace newcache with a fresh hashmap so we don't overwrite the old one
		newCache = new HashMap<String,Boolean>();
		Connection con = null;
		try {
			con = DriverManager.getConnection(dbUrl);
			PreparedStatement pstmt = null;
			pstmt = con.prepareStatement("update door "
					+"set default_unlocked = ? "
					+"WHERE door.name = ? ");
			pstmt.setString(1, state ? "Y" : "N");
			pstmt.setString(2, doorName);
			pstmt.execute();
			setForceUnlocked(state);
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
	
	private void updateCache() {
		// replace newcache with a fresh hashmap so we don't overwrite the old one
		newCache = new HashMap<String,Boolean>();
		Connection con = null;
		try {
			log.trace("loading cache for door: "+doorName);
			con = DriverManager.getConnection(dbUrl);
			PreparedStatement pstmt = null;
    		pstmt = con.prepareStatement("SELECT card_id, magic "
    				+"FROM card, door_access, door "
    				+"WHERE card.access_group_id = door_access.access_group_id "
    				+"AND door_access.door_id = door.id "
    				+"AND door.name = ? "
    				+"AND card.expires > now( ) "
    				+"AND card.valid_from < now() "
    				+"AND card.disabled = 'N' ");
    		pstmt.setString(1, doorName);
    		ResultSet rs = pstmt.executeQuery();
    		while (rs.next()) {
    			boolean magic = "Y".equals(rs.getString(2)) ? true : false;
    			newCache.put(rs.getString(1), magic);
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
	
	private synchronized void swapCache() {
		doorCache = newCache;
		forceUnlocked = newForceUnlocked;
	}
	
	private synchronized void setForceUnlocked(boolean state) {
		forceUnlocked = state;
	}
	
	public synchronized AccessInfo checkAccess(String cardId) {
		boolean retVal = doorCache.containsKey(cardId);
		AccessInfo ai;
		if (retVal) {
			if (doorCache.get(cardId)) {
				ai = new AccessInfo(true, true);

			} else {
				ai = new AccessInfo(true, false);
			}
		} else {
			ai = new AccessInfo(false, false);
		}
		
		return ai;
	}
	
	public class CacheReloadThread extends Thread {
		public void run() {
			while (!this.isInterrupted()) {
				try { 
					Thread.sleep(60*1000); // every 5 minutes
					updateCache();
				} catch (InterruptedException e) {
					return;
				}		
			}
		}
	}
	
	public synchronized boolean isForceUnlocked() {
		return forceUnlocked;
	}
	
	public void logAccess(String cardId, boolean allowed, String detail) {
		Connection con = null;
		try {
			log.info(cardId+","+doorName+","+(allowed?"allowed":"denied")+","+detail);
			con = DriverManager.getConnection(dbUrl);
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
	
}


