package com.onestopmediagroup.doorsecurity;

import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

public class LedSignWriter implements DoorAccessListener {

	private static Logger log = Logger.getLogger(DoorController.class);
	private String ledSignServiceUrl;
	
	public LedSignWriter(String ledSignServiceUrl) {
		this.ledSignServiceUrl = ledSignServiceUrl;
	}
	
	@Override
	public void doorActionEvent(DoorAccessEvent event) {
		log.debug("led sign event gotten!");
		String message;
		if (event.isAllowed()) {
			message = event.getUserCard().getNickName()+"\nhas entered";
		} else {
			message = event.getCardId()+"\nunknown hid card";
		}
		
		try {
			log.debug("writing url");
			URL url = new URL(ledSignServiceUrl+"SignService?FontSize=10&Action=ShowMessage&Message="+URLEncoder.encode(message, "UTF-8")+"&Version=2009-02-03");
			url.getContent();
			log.debug("url returned");
		} catch (Exception e) {
			log.error("couldn't make url", e);
		}

	}
	
}


