package com.onestopmediagroup.doorsecurity;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

/**
 * Sends door access messages to an AMQP queue via the RabbitMQ Client library.
 * 
 * @author dfraser
 *
 */
public class AmqpSender implements DoorAccessListener {

	private final Connection conn;
	private final String exchangeName;
	
	private static Logger log = Logger.getLogger(AmqpSender.class);
	private final boolean sendAsXml;

	public AmqpSender(String userName, String password, String virtualHost, String hostName, int portNumber, String exchangeName, String queueName, String routingKey, boolean sendAsXml) throws IOException {
		this.sendAsXml = sendAsXml;
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setUsername(userName);
		connectionFactory.setPassword(password);
		connectionFactory.setVirtualHost(virtualHost);
		connectionFactory.setHost(hostName);
		connectionFactory.setPort(portNumber);
		conn = connectionFactory.newConnection();
		this.exchangeName = exchangeName;		
	}
	
	@Override
	public void doorActionEvent(DoorAccessEvent event) {
		Channel channel = null;
		byte[] messageBodyBytes = null;
		
		try {
			channel = conn.createChannel();		
			if (sendAsXml) {
				try {
					JAXBContext context = JAXBContext.newInstance(event.getClass());
					Marshaller marshaller = context.createMarshaller();
		
					StringWriter sw = new StringWriter();
					marshaller.marshal(event,sw);
					System.out.println(sw.toString());
					messageBodyBytes = sw.toString().getBytes();
					sw.close();
				} catch (JAXBException e) {
							log.error("error marshalling door event to xml",e);
				}
			} else {
				messageBodyBytes = (event.getNickName()+" has entered").getBytes();
			}
			channel.basicPublish(exchangeName, event.getDoorName(), MessageProperties.TEXT_PLAIN, messageBodyBytes);
		} catch (IOException e) {
			log.error("error sending door access amqp message",e);
		} finally {
			if (conn != null) {
				try {
					channel.close();
				} catch (IOException e) {
					log.error("error closing amqp connection",e);
				}
			}
		}
	}	
	
	public void close() throws IOException {
		if (conn != null) {
			conn.close();
		}
	}
}
