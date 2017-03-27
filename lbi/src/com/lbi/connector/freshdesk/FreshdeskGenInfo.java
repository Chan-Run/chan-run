package com.lbi.connector.freshdesk;

import com.lbi.connector.ConnectorConstants;
import com.lbi.connector.ConnectorGenInfo;
import com.lbi.connector.ConnectorXMLLoader;

public class FreshdeskGenInfo extends ConnectorGenInfo
{
	
	public static FreshdeskGenInfo genInfo = null;
	
	public FreshdeskGenInfo() throws Exception
	{
		loadConnectorXML();
	}
	
	public static FreshdeskGenInfo getInstance() throws Exception
	{
		if(genInfo == null)
		{
			genInfo =new FreshdeskGenInfo();
		}
		return genInfo;
	}
	
	public void loadConnectorXML() throws Exception
	{
		loader = new ConnectorXMLLoader(ConnectorConstants.CONNECTOR_DETAILS_XML_FILE_PATH);
		loader.setConnector("freshdesk");
		modIdVsDetails = loader.getModIdVsDetails();
		modIdVsFieldsDets = loader.getModIdVsFieldDetsmap();
	}

	public String getConnectorKey()
	{
		return "freshdesk";
	}

	public String getConnDBInfoQuery()
	{
		return "GetFreshdeskDBInfo";
	}
	
}