package com.lbi.connector.desk;

import com.lbi.connector.ConnectorConstants;
import com.lbi.connector.ConnectorGenInfo;
import com.lbi.connector.ConnectorXMLLoader;

public class DeskGenInfo extends ConnectorGenInfo
{
	
	public static DeskGenInfo genInfo = null;
	
	public DeskGenInfo() throws Exception
	{
		loadConnectorXML();
	}
	
	public static DeskGenInfo getInstance() throws Exception
	{
		if(genInfo == null)
		{
			genInfo =new DeskGenInfo();
		}
		return genInfo;
	}
	
	public void loadConnectorXML() throws Exception
	{
		loader = new ConnectorXMLLoader(ConnectorConstants.CONNECTOR_DETAILS_XML_FILE_PATH);
		loader.setConnector("desk");
		modIdVsDetails = loader.getModIdVsDetails();
		modIdVsFieldsDets = loader.getModIdVsFieldDetsmap();
	}

	public String getConnectorKey()
	{
		return "Desk";
	}

	public String getConnDBInfoQuery()
	{
		return "GetDeskDBInfo";
	}
	
}