package com.onestopmediagroup.doorsecurity;

import java.io.IOException;

import org.apache.log4j.Logger;

public class CardReader {
	
	private static Logger log = Logger.getLogger(CardReader.class);

	private final RS232SerialPort port;
	
	public CardReader(RS232SerialPort port) {
		this.port = port;
	}
	
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
	 * @throws IOException
	 */
	public synchronized void errorBeep() throws IOException {
		byte[] beep = { 'B','2', '\n'};
		port.send(beep, beep.length);
	
	}


	/**
	 * Indicates an error by sounding the beeper for 2 seconds.
	 * @throws IOException
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

	public void flush() {
		try {
			port.flushReceiver();
		} catch (IOException e) {
			log.error("got ioexception flushing buffer: "+e.getMessage());
			// no big deal...
		}
	}
	
	/** 
	 * Opens the door by opening the strike and turning the led green for 4 seconds.
	 * @param seconds how long to hold the door open
	 * @throws IOException
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
	 * @param unlocked whether or not the door should be unlocked
	 * @throws IOException if serial port communication failed
	 */
	public synchronized void setDoorLatches(boolean unlocked) throws IOException {
		byte strike = unlocked ? (byte)'L' : (byte)'0';
		byte led = unlocked ? (byte)'L' : (byte)'0';
		byte[] buf = { 'G',led, '\n', 'B','0', '\n', 'S', strike,'\n','?','?','\n' };
		port.send(buf, buf.length);
	}
}