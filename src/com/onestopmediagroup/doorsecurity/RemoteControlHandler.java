package com.onestopmediagroup.doorsecurity;

import java.util.HashMap;

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
