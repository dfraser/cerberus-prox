package com.onestopmediagroup.doorsecurity;

import org.apache.commons.daemon.DaemonContext;

public class Daemon implements org.apache.commons.daemon.Daemon {

//	private static Logger log = Logger.getLogger(Daemon.class);
	
	private final Main main = new Main();
	
	public void start() throws Exception {
		main.start();
	}
	
	/**
	 * Handler for the Commons-Daemon system.  Attempts to shut down all
	 * {@link DoorController} threads.
	 */
	@Override
	public void stop() throws Exception {
		main.stop();
	}

	@Override
	public void destroy() {
		// nop
	}

	@Override
	public void init(DaemonContext arg0) throws Exception {
		// nop
		
	}



}
