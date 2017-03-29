package com.lbi.connector;

import com.lbi.connector.capsulecrm.CapsuleCRMGenInfo;
import com.lbi.connector.desk.DeskGenInfo;
import com.lbi.connector.freshdesk.FreshdeskGenInfo;
import com.lbi.connector.hubspot.HubSpotGenInfo;
import com.lbi.connector.insightly.InsightlyGenInfo;
import com.lbi.connector.teamsupport.TeamsupportGenInfo;
import com.lbi.connector.zendesk.ZendeskGenInfo;

public class ConnectorGenInfoFactory implements ConnectorConstants
{
	static ConnectorGenInfo info;
	
	public static ConnectorGenInfo getGenInfo(int id) throws Exception
	{
		switch(id)
		{
			case INSIGHTLY_CONN_ID : 
				
				info = InsightlyGenInfo.getInstance();		
				break;
				
			case CAPSULE_CRM_ID :
				
				info = CapsuleCRMGenInfo.getInstance();
				break;
				
			case HUBSPOT_CRM_ID :
				
				info = HubSpotGenInfo.getInstance();
				break;
				
			case ZENDESK_CONN_ID :
				
				info = ZendeskGenInfo.getInstance();
				break;
				
			case FRESHDESK_CONN_ID :
				
				info = FreshdeskGenInfo.getInstance();
				break;
				
			case DESK_CONN_ID :
				
				info = DeskGenInfo.getInstance();
				break;
				
			case TEAMSUPPORT_CONN_ID :
				
				info = TeamsupportGenInfo.getInstance();
				break;
				
			default:
				
				throw new Exception("Wrong connector id");
				
		}
		return info;
	}
}
