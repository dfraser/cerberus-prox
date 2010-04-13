/*
 * Copyright 2010 Greg Prosser
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

/* Implement a CardReader parent which can be subclassed for 
 * different readers. */

import java.io.IOException;

/**
 * A class to handle interfacing with a card reader and door strike.
 * (to be subclassed to handle files, serial, etc)
 *
 * @author greg.prosser
 *
 */
interface CardReader {
	/**
	 * Reads any available data from the source.
	 * If the data included a card read, the card data is returned.
	 * @return a {@link HIDCard} object if a card was read, null otherwise.
	 * @throws IOException if there was an error reading from the serial port.
	 */
	public HIDCard read() throws IOException;
	
	/**
	 * Indicates an error by sounding the beeper for 2 seconds.
	 * @throws IOException if there was an error reading from the serial port.
	 */
	public void errorBeep() throws IOException;
	
	/**
	 * Indicates an error by sounding the beeper for 2 seconds.
	 * @throws IOException if there was an error reading from the serial port.
	 */
	public void notifyBeep() throws IOException;
	
	/** 
	 * Opens the door by opening the strike and turning the led green for 4 seconds.
	 * 
	 * @param seconds how long to hold the door open
	 * @throws IOException if there was an error reading from the serial port.
	 */
	public void openDoor(int seconds) throws IOException;
	
	/** 
	 * Sets the absolute state of the latches in the door controller and returns the current 
	 * communication status.
	 * 
	 * @param unlocked whether or not the door should be unlocked
	 * @throws IOException if there was an error reading from the serial port.
	 */
	public void setDoorLatches(boolean unlocked) throws IOException;
}
