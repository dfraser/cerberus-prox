package com.onestopmediagroup.doorsecurity;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Session {

	private final Map<String, DoorController> doorControllers = new HashMap<String, DoorController>();
	private final Boolean rpcServerEnabled;
	private final int rpcListenPort;

	public Session() throws IOException {
		
		FileInputStream fis;
		Properties properties = new Properties();
		fis = new FileInputStream("doorsystem.properties");
		properties.load(fis);

		if (properties.getProperty("rpcServerEnabled") != null && Integer.parseInt(properties.getProperty("rpcServerEnabled")) == 1) {
			rpcServerEnabled = true;
		} else {
			rpcServerEnabled = false;
		}
		
		if (properties.getProperty("rpcListenPort") != null) {
			rpcListenPort = Integer.parseInt(properties.getProperty("rpcListenPort"));
		} else {
			rpcListenPort = 0;
		}

		
		String dbDriver = properties.getProperty("dbDriver");
		String dbUrl = properties.getProperty("dbUrl");
		
		if (dbDriver == null || dbUrl == null) {
			throw new IllegalArgumentException("expected property dbDriver and/or dbUrl not found");
		}
		
		Enumeration<Object> propKeys = properties.keys();
		while (propKeys.hasMoreElements()) {
			String keyName = (String) propKeys.nextElement();
			if (keyName.startsWith("port")) {
				int portNum = Integer.parseInt(keyName.substring(4));
				String port = properties.getProperty(keyName);
				String doorName = properties.getProperty("name" + portNum);
				if (doorName == null) {
					throw new IllegalArgumentException(
							"expected property (name" + portNum + ") not found");
				}
				RS232SerialPort comPort = new RS232SerialPort(port, 9600, 1000);
				DoorController dc = new DoorController(comPort, doorName,
						dbUrl, dbDriver);
				doorControllers.put(doorName, dc);
			}
		}
	}

	public int getRpcListenPort() {
		return rpcListenPort;
	}

	public Boolean getRpcServerEnabled() {
		return rpcServerEnabled;
	}

	public Map<String, DoorController> getDoorControllers() {
		return Collections.unmodifiableMap(doorControllers);
	}
}
