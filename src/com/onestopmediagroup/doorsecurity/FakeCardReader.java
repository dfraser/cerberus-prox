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

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * A class to handle (fake out) interfacing with a card reader and door strike, 
 * via a Cerberus-Prox RS-232 interface board, reading and writing to/from files,
 * instead of directly to a serial port.
 * 
 * @author greg.prosser
 *
 */
public class FakeCardReader implements CardReader {
	
	/**
	 * The buffered reader representing the files we're pretending to use as
	 * a cerberus-prox board.
	 */
	private BufferedReader inbr;
	
	/**
	 * The buffered writer representing the files we're pretending to use as
	 * a cerberus-prox board.
	 */
	private BufferedWriter outbw;
	
	/**
	 * Creates a new FakeCardReader object.
	 * @param infile Filename to use as input, receiving cerberus-prox style input.
	 * @param outfile Filename to use as output, sending cerberus-prox style commands.
	 */
	public FakeCardReader(String infile, String outfile) throws IOException {
		inbr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(infile))));
		outbw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outfile))));
	}
	
	/**
	 * Reads any available data from the fake Cerberus-Prox board.
	 * If the data included a card read, the card data is returned.
	 * @return a {@link HIDCard} object if a card was read, null otherwise.
	 * @throws IOException if there was an error reading from the files.
	 */
	public synchronized HIDCard read() throws IOException {
		boolean readInput = false;
		HIDCard hid = null;
		String inputBuffer = new String("");

		if (inbr.ready())
		{
			readInput = true;
			inputBuffer = inbr.readLine();
		
			if (readInput) {
					try {
						if (inputBuffer.charAt(0) == 'H') {
							hid = new HIDCard(inputBuffer.getBytes());
						}
						else if (inputBuffer.charAt(0) == '?') {
							// got status
							// use it as a heartbeat or something for monitoring
						}
					} catch(Exception e) {
						throw new IOException("can't parse card data", e);
					}
			}
		}
		return hid;

	}	
	
	/**
	 * Indicates an error by sounding the beeper for 2 seconds.
	 * @throws IOException if there was an error reading from the serial port.
	 */
	public synchronized void errorBeep() throws IOException {
		outbw.write("B2\n");
	}


	/**
	 * Indicates a notification by beeping two times quickly.
	 * @throws IOException if there was an error reading from the serial port.
	 */
	public synchronized void notifyBeep() throws IOException {
		try {
			String beepOn = "BL\n";
			String beepOff = "B0\n";
			outbw.write(beepOn);
			Thread.sleep(500);
			outbw.write(beepOff);
			Thread.sleep(500);
			outbw.write(beepOn);
			Thread.sleep(500);
			outbw.write(beepOff);
		} catch (InterruptedException e) {
			// don't worry about it... no big deal!
		}
		
	}

	/** 
	 * Opens the door by opening the strike and turning the led green for seconds seconds.
	 * 
	 * @param seconds how long to hold the door open
	 * @throws IOException if there was an error reading from the serial port.
	 */
	public synchronized void openDoor(int seconds) throws IOException {
		outbw.write("SL\nGL\n");
		
		try {
			Thread.sleep(seconds*1000);
		} catch (InterruptedException e) {
			// it's ok if we're interrupted, just lock the door
		}
		
		outbw.write("S0\nG0\n");
	}
	
	/** 
	 * Sets the absolute state of the latches in the door controller and returns the current 
	 * communication status.
	 * 
	 * @param unlocked whether or not the door should be unlocked
	 * @throws IOException if there was an error reading from the serial port.
	 */
	public synchronized void setDoorLatches(boolean unlocked) throws IOException {
		char strike = unlocked ? (char)'L' : (char)'0';
		char led = unlocked ? (char)'L' : (char)'0';
		outbw.write("G"+led+"\nB0\nS"+strike+"\n??\n");
	}
}