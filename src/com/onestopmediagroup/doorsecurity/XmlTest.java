package com.onestopmediagroup.doorsecurity;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class XmlTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		XmlTest xt = new XmlTest();
		xt.run();

		
	}

	private void run() throws IOException, JAXBException {
		DoorAccessEvent dae = new DoorAccessEvent();
		dae.setDoorName("front");
		dae.setAllowed(true);
		dae.setCardId("40-12345");
		dae.setMagic(false);
		dae.setNickName("Optic");
		dae.setRealName("Dan Fraser");
		dae.setTimeRead(new Date());
		dae.setUnknown(false);
		
		Session session = new Session();
		
		AmqpSender as = new AmqpSender(session.getAmqpUsername(), session.getAmqpPassword(), session.getAmqpVirtualhost(), session.getAmqpHost(), session.getAmqpPort(), session.getAmqpExchange(), null, null, false);
		as.doorActionEvent(dae);
		as.close();
		
	}

}
