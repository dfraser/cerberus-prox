package com.onestopmediagroup.doorsecurity;

import java.util.EventListener;

public interface DoorAccessListener extends EventListener {
	void doorActionEvent(DoorAccessEvent event);
}
