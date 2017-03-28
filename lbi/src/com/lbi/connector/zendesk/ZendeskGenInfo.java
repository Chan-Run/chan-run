package com.lbi.connector.zendesk;

import com.lbi.connector.ConnectorConstants;
import com.lbi.connector.ConnectorGenInfo;
import com.lbi.connector.ConnectorXMLLoader;

public class ZendeskGenInfo extends ConnectorGenInfo
{
	
	public static ZendeskGenInfo genInfo = null;
	
	public ZendeskGenInfo() throws Exception
	{
		loadConnectorXML();
	}
	
	public static ZendeskGenInfo getInstance() throws Exception
	{
		if(genInfo == null)
		{
			genInfo =new ZendeskGenInfo();
		}
		return genInfo;
	}
	
	public void loadConnectorXML() throws Exception
	{
		loader = new ConnectorXMLLoader(ConnectorConstants.CONNECTOR_DETAILS_XML_FILE_PATH);
		loader.setConnector("zendesk");
		modIdVsDetails = loader.getModIdVsDetails();
		modIdVsFieldsDets = loader.getModIdVsFieldDetsmap();
	}

	public String getConnectorKey()
	{
		return "Zendesk";
	}

	public String getConnDBInfoQuery()
	{
		return "GetZendeskDBInfo";
	}
	
}