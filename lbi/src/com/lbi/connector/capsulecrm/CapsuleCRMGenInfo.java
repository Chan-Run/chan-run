 package com.lbi.connector.capsulecrm;

import com.lbi.connector.ConnectorGenInfo;

public class CapsuleCRMGenInfo extends ConnectorGenInfo
{

	private static CapsuleCRMGenInfo genInfo = null;
	
	private CapsuleCRMGenInfo () throws Exception
	{
		loadConnectorXML(); 
	}
	
	@Override
	public String getConnectorKey()
	{
		return "capsulecrm";
	}
	
	public static CapsuleCRMGenInfo getInstance() throws Exception
	{
		if(genInfo == null)
		{
			genInfo = new CapsuleCRMGenInfo();
		}
		return genInfo;
	}
	
	public String getConnDBInfoQuery()
	{
		return "GetCapsuleCRMDBInfo";
	}
	
	public String getAccessTokenUrl()
	{
		return "https://api.capsulecrm.com/oauth/token";
	}

}
