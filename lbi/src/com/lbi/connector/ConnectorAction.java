package com.lbi.connector;

import javax.servlet.http.*;

import com.lbi.framework.app.Request;

public class ConnectorAction
{
	public void executeTxn(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		Request req = new Request(request);
		String action = req.getParam("ACTION");
		
		if("SETUP".equals(action))
		{
			int connectorId = req.getIntParam("CONNECTORID", true);
			Connector conn = ConnectorFactory.getConnector(connectorId);
			conn.setup(req);
		}
	
	}
}
