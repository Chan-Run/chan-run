package com.lbi.connector.pipedrive;

import com.lbi.connector.ConnectorGenInfo;

public class PDGenInfo extends ConnectorGenInfo
{

	static PDGenInfo genInfo;
	
	@Override
	public String getConnectorKey()
	{
		return "pd";
	}

	@Override
	public String getConnDBInfoQuery()
	{
		return null;
	}
	
	public static PDGenInfo getInstance() throws Exception
	{
		if(genInfo == null)
		{
			genInfo = new PDGenInfo();
		}
		return genInfo;
	}

}
