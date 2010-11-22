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
		
		JAXBContext context = JAXBContext.newInstance(dae.getClass());
		Marshaller marshaller = context.createMarshaller();

		StringWriter sw = new StringWriter();
		marshaller.marshal(dae,sw);
		System.out.println(sw.toString());
		sw.close();
	}

}
