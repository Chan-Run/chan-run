package com.lbi.connector;

import com.lbi.connector.capsulecrm.CapsuleCRMConnector;
import com.lbi.connector.freshdesk.FreshdeskConnector;
import com.lbi.connector.hubspot.HubSpotConnector;
import com.lbi.connector.insightly.InsightlyConnector;
import com.lbi.connector.pipedrive.PDConnector;
import com.lbi.connector.teamsupport.TeamsupportConnector;
import com.lbi.connector.zendesk.ZendeskConnector;
import com.lbi.sql.SQLQueryAPI;

public class ConnectorFactory implements ConnectorConstants
{
	public static Connector getConnector(int id, Long connectorId) throws Exception
	{
		Connector conn;
		
		switch(id)
		{
			case INSIGHTLY_CONN_ID : 
				
				conn = new InsightlyConnector(connectorId);		
				break;
				
			case PD_CRM_ID :
				
				conn = new PDConnector(connectorId);
				break;
				
			case CAPSULE_CRM_ID :
				
				conn = new CapsuleCRMConnector(connectorId);
				break;
				
			case HUBSPOT_CRM_ID :
				
				conn = new HubSpotConnector(connectorId);
				break;
				
			case ZENDESK_CONN_ID :
				
				conn = new ZendeskConnector(connectorId);
				break;
				
			case FRESHDESK_CONN_ID :
				
				conn = new FreshdeskConnector(connectorId);
				break;
				
			case DESK_CONN_ID :
				
				conn = new TeamsupportConnector(connectorId);
				break;
				
			case TEAMSUPPORT_CONN_ID :
				
				conn = new TeamsupportConnector(connectorId);
				break;
				
			default:
				
				throw new Exception("Wrong connector id");
				
		}
		
		return conn;
	}
	
	public static Connector getConnector(int id) throws Exception
	{
		Connector conn;
		
		switch(id)
		{
			case INSIGHTLY_CONN_ID : 
				
				conn = new InsightlyConnector();		
				break;
				
			case PD_CRM_ID :
				
				conn = new PDConnector();
				break;
				
			case CAPSULE_CRM_ID :
				
				conn = new CapsuleCRMConnector();
				break;
				
			case HUBSPOT_CRM_ID :
				
				conn = new HubSpotConnector();
				break;
				
			case TEAMSUPPORT_CONN_ID :
				
				conn = new TeamsupportConnector();
				break;
				
			default:
				
				throw new Exception("Wrong connector id");
				
		}
		
		return conn;
	}
	
	public static Connector getConnector(Long connectorId) throws Exception
	{
		String sql = SQLQueryAPI.getSQLString("GetConnector", new Object[][]{{"CONNECTORID", connectorId}});
		int connector = (Integer) SQLQueryAPI.getResult(sql);
		
		return getConnector(connector, connectorId);
	}
	
}
