package com.onestopmediagroup.doorsecurity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class AccessLogger implements DoorAccessListener {

	private static Logger log = Logger.getLogger(DoorController.class);
	private static Logger logFriendly = Logger.getLogger("Friendly");

	private final Session session;

	public AccessLogger(Session session) {
		this.session = session;
	}

	@Override
	public void doorActionEvent(DoorAccessEvent event) {
		Connection con = null;
		try {
			String doorName = event.getDoorName();
			boolean allowed = event.isAllowed();
			UserCard user = event.getUserCard();
			String cardId = event.getCardId();
			String detail = "";

			log.info(cardId+","+doorName+","+(allowed?"allowed":"denied")+","+detail);
			if (allowed && user != null) {
				String name;
				if (session.isFriendlyLogRealName()) {
					name = user.getRealName();
				} else {
					name = user.getNickName();
				}
				logFriendly.info(name+" has entered.");
			} else {
				logFriendly.info("Unauthorized card: "+cardId);
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

}
