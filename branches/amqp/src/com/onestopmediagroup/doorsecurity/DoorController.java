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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * Thread to handle continuous control over a door.
 * 
 * It periodically updates the state of the related door controller.
 * 
 * @author dfraser
 *
 */
public class DoorController implements Runnable {

	private static Logger log = Logger.getLogger(DoorController.class);
	private final AccessVerifier av;
	private final String doorName;
	private final CardReader cr;
	
	private final int pollInterval = 5000; // millis
	private long lastPollTime;
	private boolean triggerOpen;
	private List<DoorAccessListener> listeners = new ArrayList<DoorAccessListener>();

	/**
	 * Creates a new DoorController thread.
	 * 
	 * @param port the RS232SerialPort this thread is to use
	 * @param name the name of the door to control (from database door table)
	 * @param session the current configuration session
	 */
	public DoorController(RS232SerialPort port, String name, Session session) {
		this.doorName = name;
		this.cr = new CardReader(port);
		this.av = new AccessVerifier(name, session);
		port.setRxTimeout(1000);
	}
	
	public void addDoorAccessListener(DoorAccessListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}
	
	public void removeDoorAccessListener(DoorAccessListener listener) {
		listeners.remove(listener);
	}
	
	public List<DoorAccessListener> getListeners() {
		return Collections.unmodifiableList(listeners);
	}
	
	/**
	 * The main program loop for each door.
	 */
	@Override
	public void run() {
		log.debug("Controller for door '"+doorName+"' starting.");
		while (true) {
			HIDCard card;
			try {
				card = cr.read();
				if (card != null) {
					boolean allowed = false;
					UserCard userCard = av.checkAccess(card.getCardId());
					if (userCard != null && userCard.isMagic()) {
						// this is a magic card.  switch the door state.
						boolean oldState = av.forceUnlocked;
						av.setDefaultUnlocked(!oldState);
						cr.notifyBeep();
						allowed = true;
					} else {
						if (userCard != null) {
							if (!av.forceUnlocked) {
								cr.openDoor(4);
							}
							allowed = true;
						} else {
							cr.errorBeep();
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// we should exit if interrupted
								return;
							}
							log.info(card.getCardId()+","+doorName+",access denied");
							allowed = false;
						}
					}

					Date timeRead = new Date();
					for (DoorAccessListener listener : listeners) {
						DoorAccessEvent dae = new DoorAccessEvent();
						dae.setAllowed(allowed);
						dae.setUnknown(userCard == null);
						dae.setCardId(card.getCardId());
						dae.setDoorName(doorName);
						if (userCard != null) {
							dae.setMagic(userCard.isMagic());
							dae.setNickName(userCard.getNickName());
							dae.setRealName(userCard.getRealName());
						} else {
							dae.setMagic(false);
							dae.setNickName("Unknown");
							dae.setRealName("Unknown");
						}
						dae.setTimeRead(timeRead);
						listener.doorActionEvent(dae); 
					}
				} else {
					// unlock the door if we got a trigger
					if (triggerOpen) {
						synchronized (this) {
							triggerOpen = false;						
						}
						if (!av.forceUnlocked) {
							cr.openDoor(4);
							try {
								Thread.sleep(4000);
							} catch (InterruptedException e) {
								// it's okay not to sleep
							}
						}
					}

					// update the door state
					if (lastPollTime + pollInterval < System.currentTimeMillis()) {
						lastPollTime = System.currentTimeMillis();
						cr.setDoorLatches(av.forceUnlocked);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void triggerOpen() {
		triggerOpen = true;
	}
	
	
	
}
