package com.onestopmediagroup.doorsecurity;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Defines the session state for the door system.  Mostly includes configuration data.
 * 
 * @author dfraser
 *
 */
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
	
	/**
	 * True if the AMQP subsystem is supposed to be enabled.
	 */
	private final boolean amqpEnabled;
	
	/**
	 * The hostname for the AMQP broker
	 */
	private final String amqpHost;
	
	/**
	 * The port number for AMQP communication
	 */
	private final int amqpPort;
	
	/** 
	 * The virutal host path on the AMQP broker
	 */
	private final String amqpVirtualhost;
	
	/**
	 * The username for the AMQP broker connection
	 */
	private final String amqpUsername;
	
	/**
	 * The password for the AMQP broker connection
	 */
	private final String amqpPassword;
	
	/**
	 * The exchange name to use on the AMQP broker
	 */
	private final String amqpExchange;
	
	/**
	 * The routing key to be sent with the message
	 */
	private final String amqpRoutingKey;
	
	/**
	 * The queue name on the AMQP broker
	 */
	private final String amqpQueue;

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

		
		if (properties.getProperty("amqp.enabled") != null && Integer.parseInt(properties.getProperty("amqp.enabled")) == 1) {
			amqpEnabled = true;
		} else {
			amqpEnabled = false;
		}
		this.amqpExchange = properties.getProperty("amqp.exchangeName");
		this.amqpQueue = properties.getProperty("amqp.queueName");
		this.amqpHost = properties.getProperty("amqp.host");
		this.amqpUsername = properties.getProperty("amqp.username");
		this.amqpPassword = properties.getProperty("amqp.password");
		this.amqpPort = Integer.parseInt(properties.getProperty("amqp.port"));
		this.amqpRoutingKey = properties.getProperty("amqp.routingKey");
		this.amqpVirtualhost = properties.getProperty("amqp.virtualHost");
		
		Enumeration<Object> propKeys = properties.keys();
		while (propKeys.hasMoreElements()) {
			String keyName = (String) propKeys.nextElement();
			Pattern p = Pattern.compile("[\\d]+");
			if (keyName.startsWith("port")) {
				Matcher m = p.matcher(keyName);
				int portNum;
				if (m.find()) {
				    portNum = Integer.parseInt(m.group(0));
				} else {
				    throw new IllegalArgumentException(
				            "port number value not found in " + keyName);
				}
				String port = properties.getProperty(keyName);
				String doorName = properties.getProperty("name" + portNum);
				if (doorName == null) {
					throw new IllegalArgumentException(
							"expected property (name" + portNum + ") not found");
				}
//				RS232SerialPort comPort = new RS232SerialPort(port, 9600, 1000);
//				DoorController dc = new DoorController(comPort, doorName, this);
//				doorControllers.put(doorName, dc);
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
	
	public String getAmqpExchange() {
		return amqpExchange;
	}
	
	public String getAmqpPassword() {
		return amqpPassword;
	}
	
	public String getAmqpHost() {
		return amqpHost;
	}
	
	public int getAmqpPort() {
		return amqpPort;
	}
	
	public String getAmqpRoutingKey() {
		return amqpRoutingKey;
	}
	
	public String getAmqpUsername() {
		return amqpUsername;
	}
	
	public String getAmqpVirtualhost() {
		return amqpVirtualhost;
	}
	
	public boolean isAmqpEnabled() {
		return amqpEnabled;
	}

	public String getAmqpQueue() {
		return amqpQueue;
	}
}
