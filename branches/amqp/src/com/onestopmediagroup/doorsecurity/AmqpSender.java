package com.onestopmediagroup.doorsecurity;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;

/**
 * Sends door access messages to an AMQP queue via the RabbitMQ Client library.
 * 
 * @author dfraser
 *
 */
public class AmqpSender implements DoorAccessListener {

	private final Connection conn;
	private final String queueName;
	private final String exchangeName;
	private final String routingKey;
	
	private static Logger log = Logger.getLogger(AmqpSender.class);

	public AmqpSender(String userName, String password, String virtualHost, String hostName, int portNumber, String exchangeName, String queueName, String routingKey) throws IOException {
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setUsername(userName);
		connectionFactory.setPassword(password);
		connectionFactory.setVirtualHost(virtualHost);
		connectionFactory.setHost(hostName);
		connectionFactory.setPort(portNumber);
		conn = connectionFactory.newConnection();
		this.queueName = queueName;
		this.routingKey = routingKey;
		this.exchangeName = exchangeName;		
	}
	
	@Override
	public void doorActionEvent(DoorAccessEvent event) {
		Channel channel = null; 
		try {
			channel = conn.createChannel();		
			channel.exchangeDeclare(exchangeName, "direct", true);
			channel.queueDeclare(queueName, true, false, false, null);
			channel.queueBind(queueName, exchangeName, routingKey);
			
			try {
				JAXBContext context = JAXBContext.newInstance(event.getClass());
				Marshaller marshaller = context.createMarshaller();
	
				StringWriter sw = new StringWriter();
				marshaller.marshal(event,sw);
				System.out.println(sw.toString());
				byte[] messageBodyBytes = sw.toString().getBytes();
				sw.close();
			
				channel.basicPublish(exchangeName, routingKey, null, messageBodyBytes);
			} catch (JAXBException e) {
				log.error("error marshalling door event to xml",e);
			}
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
}
