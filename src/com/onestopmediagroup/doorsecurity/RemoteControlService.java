/*
 * Copyright 2008 Dan Fraser
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import redstone.xmlrpc.XmlRpcServlet;
import java.util.*;

/**
 * XML-RPC Servlet interface to allow a door to be unlocked using XML-RPC.
 * 
 * @author dfraser
 *
 */
public class RemoteControlService extends XmlRpcServlet {

	private static final long serialVersionUID = 2091781880167729012L;
	
	private Map<String, DoorController> doorControllers;

	public RemoteControlService(Map<String,DoorController> doorControllers) {
		this.doorControllers = doorControllers;
	}

	public void init( ServletConfig servletConfig ) throws ServletException
    {
        super.init( servletConfig );
        getXmlRpcServer().addInvocationHandler( "DoorControl", new RemoteControlHandler(doorControllers) );
        getXmlRpcServer().addInvocationHandler( "RandomNumberGenerator", new Random() );
    }


}
