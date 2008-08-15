package com.onestopmediagroup.doorsecurity;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import redstone.xmlrpc.XmlRpcServlet;
import java.util.*;

public class RemoteControlService extends XmlRpcServlet {

	private static final long serialVersionUID = 2091781880167729012L;
	
	private HashMap<String, DoorController> doorControllers;

	public RemoteControlService(HashMap<String,DoorController> doorControllers) {
		this.doorControllers = doorControllers;
	}

	
	
	public void init( ServletConfig servletConfig ) throws ServletException
    {
        super.init( servletConfig );
        getXmlRpcServer().addInvocationHandler( "DoorControl", new RemoteControlHandler(doorControllers) );
        getXmlRpcServer().addInvocationHandler( "RandomNumberGenerator", new Random() );
    }


}
