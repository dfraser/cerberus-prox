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


/**
 * A class to handle interfacing with a card reader and door strike, 
 * via a Cerberus-Prox RS-232 interface board.
 * 
 * @author dfraser
 *
 */
public class CardReader {
	
	// private static Logger log = Logger.getLogger(CardReader.class);

	/**
	 * The serial port used to communicate with the Cerberus-Prox board.
	 */
	private final RS232SerialPort port;
	
	/**
	 * Creates a new CardReader object.
	 * @param port the serial port used to communicate with the Cerberus-Prox board.
	 */
	public CardReader(RS232SerialPort port) {
		this.port = port;
	}
	
	/**
	 * Reads any available data from the Cerberus-Prox board.
	 * If the data included a card read, the card data is returned.
	 * @return a {@link HIDCard} object if a card was read, null otherwise.
	 * @throws IOException if there was an error reading from the serial port.
	 */
	public synchronized HIDCard read() throws IOException {
		HIDCard hid = null;
		byte[] buf = new byte[1024];
		int rc = port.receive(buf, buf.length, 0x0a);
		if (rc == 0) {
			return null;
		}
		if (rc != -1) {
				try {
					if (buf[0] == 'H') {
						hid = new HIDCard(buf);
					}
					else if (buf[0] == '?') {
						// got status
						// use it as a heartbeat or something for monitoring
					}
				} catch(Exception e) {
					throw new IOException("can't parse card data", e);
				}
		} else {
			throw new IOException("error reading from serial port: "+rc);
		}
		return hid;
	}	
	
	/**
	 * Indicates an error by sounding the beeper for 2 seconds.
	 * @throws IOException if there was an error reading from the serial port.
	 */
	public synchronized void errorBeep() throws IOException {
		byte[] beep = { 'B','2', '\n'};
		port.send(beep, beep.length);
	
	}


	/**
	 * Indicates an error by sounding the beeper for 2 seconds.
	 * @throws IOException if there was an error reading from the serial port.
	 */
	public synchronized void notifyBeep() throws IOException {
		try {
			byte[] beepOn = { 'B','L', '\n'};
			port.send(beepOn, beepOn.length);
			Thread.sleep(500);
			byte[] beepOff = { 'B','0', '\n'};
			port.send(beepOff, beepOff.length);
			Thread.sleep(500);
			port.send(beepOn, beepOn.length);
			Thread.sleep(500);
			port.send(beepOff, beepOff.length);
		} catch (InterruptedException e) {
			// don't worry about it... no big deal!
		}
		
	}

	/** 
	 * Opens the door by opening the strike and turning the led green for 4 seconds.
	 * 
	 * @param seconds how long to hold the door open
	 * @throws IOException if there was an error reading from the serial port.
	 */
	public synchronized void openDoor(int seconds) throws IOException {

		byte[] buf = { 'S','L', '\n', 'G', 'L', '\n'};
		port.send(buf, buf.length);
		try {
			Thread.sleep(seconds*1000);
		} catch (InterruptedException e) {
			// it's ok if we're interrupted, just lock the door
		}
		byte[] buf2 = { 'S','0', '\n', 'G', '0', '\n'};
		port.send(buf2, buf2.length);
		
	}
	
	/** 
	 * Sets the absolute state of the latches in the door controller and returns the current 
	 * communication status.
	 * 
	 * @param unlocked whether or not the door should be unlocked
	 * @throws IOException if there was an error reading from the serial port.
	 */
	public synchronized void setDoorLatches(boolean unlocked) throws IOException {
		byte strike = unlocked ? (byte)'L' : (byte)'0';
		byte led = unlocked ? (byte)'L' : (byte)'0';
		byte[] buf = { 'G',led, '\n', 'B','0', '\n', 'S', strike,'\n','?','?','\n' };
		port.send(buf, buf.length);
	}
}