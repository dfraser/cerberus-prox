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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class Main {

	private static Logger log = Logger.getLogger(Main.class);
	private Session session;
	private List<ExecutorService> doorThreadExecutors;
	
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

		AccessLogger accessLogger = new AccessLogger(session);

		AmqpSender amqpSender = null;
		if (session.isAmqpEnabled()) {
			amqpSender = new AmqpSender(session.getAmqpUsername(), session.getAmqpPassword(), session.getAmqpVirtualhost(), session.getAmqpHost(), session.getAmqpPort(), session.getAmqpExchange(), session.isAmqpSendMessageAsXml());
		}
		
		log.debug("starting controller threads...");
		
		doorThreadExecutors = new ArrayList<ExecutorService>();

		for (DoorController dc: session.getDoorControllers().values()) {
			if (session.isAmqpEnabled()) {
				dc.addDoorAccessListener(amqpSender);
			}
			dc.addDoorAccessListener(accessLogger);
			ExecutorService dcExecutor = Executors.newSingleThreadExecutor();
			dcExecutor.execute(dc);
			doorThreadExecutors.add(dcExecutor);
		}
		
		// start xml-rpc server
		if (session.isRpcServerEnabled() && session.getRpcListenPort() != 0) {
			ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
			serverExecutor.execute(new ServerThread(session));
		}
	}
	
	public void stop() {
		log.debug("shutting down controller threads...");
		for (ExecutorService doorThreadExecutor : doorThreadExecutors) {
			doorThreadExecutor.shutdown();
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
	private class ServerThread implements Runnable {
		
		private final int port;

		public ServerThread(Session session) {
			this.port = session.getRpcListenPort();
			
		}
		
		@Override
		public void run() {
			try {
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
