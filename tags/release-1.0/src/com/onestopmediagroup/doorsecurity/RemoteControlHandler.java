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

import java.util.HashMap;


/**
 * Handler to allow a door to be unlocked via XML-RPC.
 * 
 * @author dfraser
 *
 */
public class RemoteControlHandler {
	private final HashMap<String, DoorController> doorControllers;

	public RemoteControlHandler(HashMap<String,DoorController> doorControllers) {
		this.doorControllers = doorControllers;
	}
	
	public int openDoor(String name) {
		DoorController dc = doorControllers.get(name);
		if (dc != null) {
			dc.triggerOpen();
			return 0;
		}
		return -1;
	}
}
