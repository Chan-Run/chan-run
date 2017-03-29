 package com.lbi.connector.hubspot;

import com.lbi.connector.ConnectorGenInfo;

public class HubSpotGenInfo extends ConnectorGenInfo
{
	
	private static HubSpotGenInfo genInfo;
	
	private HubSpotGenInfo() throws Exception
	{
		loadConnectorXML(); 
	}
	
	@Override
	public String getConnectorKey()
	{
		return "hubspot";
	}

	@Override
	public String getConnDBInfoQuery()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getAccessTokenUrl()
	{
		return "https://api.capsulecrm.com/oauth/v1/token";
	}
	
	public static HubSpotGenInfo getInstance() throws Exception
	{
		if(genInfo == null)
		{
			genInfo = new HubSpotGenInfo();
		}
		return genInfo;
	}
	

}
