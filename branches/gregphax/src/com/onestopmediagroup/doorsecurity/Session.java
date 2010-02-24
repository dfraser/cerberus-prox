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
	private final boolean rpcServerEnabled;
	private final int rpcListenPort;
	private final int cacheReloadSeconds;
	private final String dbUrl;
	private final String dbDriver;
	private final int afterHoursStart;
	private final int afterHoursEnd;
	private final boolean afterHoursEnabled;
	private final boolean friendlyLogRealName;
	private final boolean useLedSign;
	private final String ledSignServiceUrl;

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

		dbDriver = properties.getProperty("dbDriver");
		dbUrl = properties.getProperty("dbUrl");
		
		if (dbDriver == null || dbUrl == null) {
			throw new IllegalArgumentException("expected property dbDriver and/or dbUrl not found");
		}
		
		if (properties.getProperty("cacheReloadSeconds") != null) {
			cacheReloadSeconds = Integer.parseInt(properties.getProperty("cacheReloadSeconds"));
		} else {
			cacheReloadSeconds = 120; // reasonable default
		}		
		
		boolean tmpAfterHoursEnabled = true;
		if (properties.getProperty("afterHoursStart") != null) {
			afterHoursStart = Integer.parseInt(properties.getProperty("afterHoursStart"));
		} else {
			afterHoursStart = 0;
			tmpAfterHoursEnabled = false;
		}		

		if (properties.getProperty("afterHoursStart") != null) {
			afterHoursEnd = Integer.parseInt(properties.getProperty("afterHoursEnd"));
		} else {
			afterHoursEnd = 0;
			tmpAfterHoursEnabled = false;
		}		
		
		this.afterHoursEnabled = tmpAfterHoursEnabled;
		
		
		this.friendlyLogRealName = "user".equals(properties.getProperty("friendlyLogName"));

		this.useLedSign = "true".equals(properties.getProperty("useLedSign"));
		
		this.ledSignServiceUrl = properties.getProperty("ledSignServiceUrl");
		
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
				DoorController dc = new DoorController(comPort, doorName, this);
				doorControllers.put(doorName, dc);
			}
		}

	}

	public String getLedSignServiceUrl() {
		return ledSignServiceUrl;
	}

	public boolean isFriendlyLogRealName() {
		return friendlyLogRealName;
	}

	public int getAfterHoursStart() {
		return afterHoursStart;
	}

	public int getAfterHoursEnd() {
		return afterHoursEnd;
	}

	public boolean isAfterHoursEnabled() {
		return afterHoursEnabled;
	}

	public int getRpcListenPort() {
		return rpcListenPort;
	}

	public Boolean isRpcServerEnabled() {
		return rpcServerEnabled;
	}

	public Map<String, DoorController> getDoorControllers() {
		return Collections.unmodifiableMap(doorControllers);
	}
	
		
	public int getCacheReloadSeconds() {
		return cacheReloadSeconds;
	}

	public String getDbUrl() {
		return dbUrl;
	}
	
	public String getDbDriver() {
		return dbDriver;
	}
	
	public boolean isUseLedSign() {
		return useLedSign;
	}
	
}
