/*
 * Copyright 2008 Andrew Kilpatrick
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

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.apache.log4j.Logger;

/**
 * This is the serial driver code for communication with devices
 * on the twisted-pair AV bus.
 * 
 * @author akilpatrick
 */
public class RS232SerialPort {
	private Logger logger;
	private SerialPort port;
	private InputStream in;
	private OutputStream out;
	private int receiveTimeout;
	private int baudrate = -1;
	
	
	/**
	 * Creates a new serial port.
	 * 
	 * @param serialPortName the serial port to use
	 * @param baud the baud rate to use in bps
	 * @param timeout the receive timeout in ms
	 */
	public RS232SerialPort(String serialPortName, int baud, int timeout) throws IOException {
		logger = Logger.getLogger(this.getClass());
		logger.debug("setting up serial port: " + serialPortName);
		
		// set up the serial port
		try {
			CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(serialPortName);
			CommPort cp = cpi.open("Java", 1000);
			if(cp instanceof SerialPort) {
				port = (SerialPort)cp;
			}
			else {
				logger.debug("wrong type of port to be opened");
				logger.debug(availablePorts());
			}
			this.baudrate = baud;
			port.setSerialPortParams(baudrate, SerialPort.DATABITS_8, 
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			receiveTimeout = timeout;			
			port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			port.disableReceiveFraming();
			port.disableReceiveThreshold();
			in = port.getInputStream();
			out = port.getOutputStream();			
		} catch (NoSuchPortException e) {
			logger.debug(availablePorts());
			throw new IOException("port not found: "+serialPortName);
		} catch (PortInUseException e) {
			throw new IOException("port in use: "+serialPortName);
		} catch (UnsupportedCommOperationException e) {
			throw new IOException("unsupported comm operation: "+e.getMessage());
		} 
		logger.debug("port set up: " + serialPortName + "  baudrate: " + baudrate);
	}
	
	
	/**
	 * Sends a message.
	 * 
	 * @param txBuf buffer to send
	 * @param txLen number of bytes to send
	 * @return the number of bytes sent
	 */
	public int send(byte txBuf[], int txLen) throws IOException {
		if(txBuf == null || txBuf.length < 1) {
			throw new IllegalArgumentException("txBuf is null or empty");
		}
		if(txLen > txBuf.length || txLen < 1) {			
			throw new IllegalArgumentException("txLen is invalid: " + txLen);
		}
		out.write(txBuf, 0, txLen);
		return txLen;
	}
	
	
	/**
	 * Receives a message terminated in 0x04 EOT.
	 * 
	 * @param rxBuf buffer to read into
	 * @param rxLen max number of bytes to receive
	 * @return the number of bytes received
	 */
	public int receive(byte[] rxBuf, int rxLen) throws IOException {
		return receive(rxBuf, rxLen, 0x04);
	}
	
	
	/**
	 * Receives a message.
	 * 
	 * @param rxBuf buffer to read into
	 * @param rxLen max number of bytes to receive
	 * @param eotChar the byte which will cause reception to stop
	 * @return the number of bytes received, or -1 for error.
	 */
	public int receive(byte rxBuf[], int rxLen, int eotChar) throws IOException {
		if(rxBuf == null || rxBuf.length < 1) {
			throw new IllegalArgumentException("rxBuf is null or empty");
		}
		if(rxLen > rxBuf.length || rxLen < 1) {
			throw new IllegalArgumentException("rxLen is invalid: "+rxLen);
		}
		long currentTime = System.currentTimeMillis();
		
		int rxCount = 0;
		while(System.currentTimeMillis() - currentTime < receiveTimeout && rxCount < rxLen) {
			if(in.available() > 0) {
				int ret = in.read(rxBuf, rxCount, rxLen - rxCount);
				rxCount += ret;
				if(rxBuf[rxCount - 1] == eotChar) {
					return rxCount;
				}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// do nothing here... it's the same as a timeout
			}
		}
		return rxCount;
	}
	
	
	/**
	 * Flushes the receive buffer.
	 */
	public void flushReceiver() throws IOException {
		while(in.available() > 0) {
			int ret = in.read();
			logger.error("eating a byte: " + Integer.toHexString(ret));
		}
	}
	
	
	/**
	 * Closes the serial port.
	 */
	public void close() {
		port.close();
	}
	
	
	/**
	 * Gets a list of ports as a String.
	 * 
	 * @return a list of ports
	 */
	public String availablePorts() {
		String str = "available ports: ";
		Enumeration<?> iter = CommPortIdentifier.getPortIdentifiers();
		String portString = "";
		while(iter.hasMoreElements()) {
			CommPortIdentifier cpi = (CommPortIdentifier)iter.nextElement();
			if(cpi != null) {
				portString += ", "+cpi.getName();
			}
		}
		str += portString.substring(2);
		return str;
	}
	
	
	/**
	 * Sets the baudrate.
	 * 
	 * @param baud the baudrate in bps
	 * @throws SerialPortException if there is a problem setting the baud rate
	 */
	public void setBaudrate(int baud) throws SerialPortException {
		if(this.baudrate == baud) {
			return;
		}
		try {
			port.setSerialPortParams(baud, SerialPort.DATABITS_8, 
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			baudrate = baud;
		} catch (UnsupportedCommOperationException e) {
			throw new SerialPortException("unsupported baudrate setting: " + baudrate);
		}
	}
	
	
	/**
	 * Sets the receive timeout.
	 * 
	 * @param rxTimeout the receive timeout in ms
	 */
	public void setRxTimeout(int rxTimeout) {
		if(this.receiveTimeout == rxTimeout || rxTimeout < 0) {
			return;
		}
		this.receiveTimeout = rxTimeout;
	}
}
