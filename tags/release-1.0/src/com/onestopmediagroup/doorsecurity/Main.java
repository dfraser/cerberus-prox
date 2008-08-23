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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class Main implements Daemon {

	private static Logger log = Logger.getLogger(Main.class);
	private HashMap<String,DoorController> doorControllers = new HashMap<String,DoorController>();
	
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
	@Override
	public void start() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		FileInputStream fis;
		Properties properties = new Properties();
		try {
			fis = new FileInputStream("doorsystem.properties");
			properties.load(fis);
		} catch (IOException e) {
			System.err.println("couldn't open properties file (doorsystem.properties): "+e.getMessage());
			return;
		}		
		
		String dbDriver = properties.getProperty("dbDriver");
		String dbUrl = properties.getProperty("dbUrl");
		
		if (dbDriver == null || dbUrl == null) {
			throw new IllegalArgumentException("expected property dbDriver and/or dbUrl not found");
		}
		
		Enumeration<Object> propKeys = properties.keys();
		try {
			while (propKeys.hasMoreElements()) {
				String keyName = (String) propKeys.nextElement();
				if (keyName.startsWith("port")) {
					int portNum = Integer.parseInt(keyName.substring(4));
					String port = properties.getProperty(keyName);
					String doorName = properties.getProperty("name"+portNum);					
					if (doorName == null) {
						throw new IllegalArgumentException("expected property (name"+portNum+") not found");
					}
					RS232SerialPort comPort = new RS232SerialPort(port,9600,1000);
					DoorController dc = new DoorController(comPort, doorName, dbUrl, dbDriver);
					doorControllers.put(doorName,dc);
				}
		     }
		} catch (IOException e) {
			System.err.println("Error configuring system: "+e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		
		log.debug("starting controller threads...");
		// let's get going!
		for (Iterator<DoorController> dcIter = doorControllers.values().iterator(); dcIter.hasNext();) {
			DoorController dc = (DoorController) dcIter.next();
			dc.start();
		}
		
		// start xml-rpc server
		ServerThread server = new ServerThread();
		server.start();
  
	}
	
	/**
	 * Handler for the Commons-Daemon system.  Attempts to shut down all
	 * {@link DoorController} threads.
	 */
	@Override
	public void stop() throws Exception {

		log.debug("shutting down controller threads...");
		for (Iterator<DoorController> dcIter = doorControllers.values().iterator(); dcIter.hasNext();) {
			DoorController dc = (DoorController) dcIter.next();
			dc.interrupt();
		}
		Thread.sleep(2000);
		System.exit(0);
	}

	@Override
	public void destroy() {
		// nop
	}

	@Override
	public void init(DaemonContext arg0) throws Exception {
		// nop
		
	}

	/**
	 * Basic server class to handle the prototype XML-RPC interface.
	 * 
	 * @author dfraser
	 *
	 */
	private class ServerThread extends Thread {	
		@Override
		public void run() {
			try {
				super.run();
				Server server = new Server(8080);
		    	Context context = new Context(server,"/",Context.SESSIONS);
		    	context.addServlet(new ServletHolder(new RemoteControlService(doorControllers)), "/xml-rpc/*");        
		    	server.start();
			} catch (Exception e) {
				log.error("couldn't start server: "+e.getMessage());
			}
		}
		
	}

}
