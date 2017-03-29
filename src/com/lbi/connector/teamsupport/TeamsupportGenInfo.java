package com.lbi.connector.teamsupport;

import com.lbi.connector.ConnectorConstants;
import com.lbi.connector.ConnectorGenInfo;
import com.lbi.connector.ConnectorXMLLoader;

public class TeamsupportGenInfo extends ConnectorGenInfo
{
	
	public static TeamsupportGenInfo genInfo = null;
	
	public TeamsupportGenInfo() throws Exception
	{
		loadConnectorXML();
	}
	
	public static TeamsupportGenInfo getInstance() throws Exception
	{
		if(genInfo == null)
		{
			genInfo =new TeamsupportGenInfo();
		}
		return genInfo;
	}
	
	public void loadConnectorXML() throws Exception
	{
		loader = new ConnectorXMLLoader(ConnectorConstants.CONNECTOR_DETAILS_XML_FILE_PATH);
		loader.setConnector("teamsupport");
		modIdVsDetails = loader.getModIdVsDetails();
		modIdVsFieldsDets = loader.getModIdVsFieldDetsmap();
	}

	public String getConnectorKey()
	{
		return "Teamsupport";
	}

	public String getConnDBInfoQuery()
	{
		return "GetTeamsupportDBInfo";
	}
	
}