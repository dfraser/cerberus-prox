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

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class Main {

	private static Logger log = Logger.getLogger(Main.class);
	private Session session;
	
	/**
	 * Main entry point to the application.
	 * 
	 * @param args there are no command line arguments yet....
	 */ 
	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.start();
	}
	
	/**
	 * Startup hook for the Commons-Daemon system.  Loads the configuration 
	 * and kicks off a {@link DoorController} for each door. 
	 */
	public void start() throws Exception {

		PropertyConfigurator.configure("log4j.properties");

		session = new Session();

		// Create the LedSignWriter object and set it to null
		LedSignWriter ledSign = null;
		if(session.isUseLedSign()) { // Check to see if we're using the led sign
			ledSign = new LedSignWriter(session.getLedSignServiceUrl()); // If so, instantiate the object
		}
		AccessLogger accessLogger = new AccessLogger(session);

		log.debug("starting controller threads...");
		// let's get going!
		for (Iterator<DoorController> dcIter = session.getDoorControllers().values().iterator(); dcIter.hasNext();) {
			DoorController dc = (DoorController) dcIter.next();
			if(session.isUseLedSign()) { // Check to see if we're using the led sign
				dc.addDoorAccessListener(ledSign); // If so, register the class to the event
			}
			dc.addDoorAccessListener(accessLogger);
			dc.start();
		}
		
		// start xml-rpc server
		if (session.isRpcServerEnabled() && session.getRpcListenPort() != 0) {
			ServerThread server = new ServerThread(session);
			server.start();
		}
	}
	
	public void stop() {
		log.debug("shutting down controller threads...");
		for (Iterator<DoorController> dcIter = session.getDoorControllers().values().iterator(); dcIter.hasNext();) {
			DoorController dc = (DoorController) dcIter.next();
			dc.interrupt();
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// ok to ignore, we're going to exit anyways
		}
		return;
	}
	
	/**
	 * Basic server class to handle the prototype XML-RPC interface.
	 * 
	 * @author dfraser
	 *
	 */
	private class ServerThread extends Thread {
		
		private final int port;

		public ServerThread(Session session) {
			this.port = session.getRpcListenPort();
			
		}
		
		@Override
		public void run() {
			try {
				super.run();
				Server server = new Server(port);
		    	Context context = new Context(server,"/",Context.SESSIONS);
		    	context.addServlet(new ServletHolder(new RemoteControlService(session.getDoorControllers())), "/xml-rpc/*");        
		    	server.start();
			} catch (Exception e) {
				log.error("couldn't start server: "+e.getMessage());
			}
		}
		
	}

}
