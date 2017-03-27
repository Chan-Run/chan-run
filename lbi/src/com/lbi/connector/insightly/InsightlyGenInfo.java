package com.lbi.connector.insightly;

import com.lbi.connector.ConnectorConstants;
import com.lbi.connector.ConnectorGenInfo;
import com.lbi.connector.ConnectorXMLLoader;

public class InsightlyGenInfo extends ConnectorGenInfo
{
	private static InsightlyGenInfo genInfo = null;
	
	private InsightlyGenInfo() throws Exception
	{
		loadConnectorXML();
	}
	
	public static InsightlyGenInfo getInstance() throws Exception
	{
		if(genInfo == null)
		{
			genInfo = new InsightlyGenInfo();
		}
		return genInfo;
	}
	
	public void loadConnectorXML() throws Exception
	{
		loader = new ConnectorXMLLoader(ConnectorConstants.CONNECTOR_DETAILS_XML_FILE_PATH);
		loader.setConnector("insightly");
		modIdVsDetails = loader.getModIdVsDetails();
		modIdVsFieldsDets = loader.getModIdVsFieldDetsmap();
	}

	public String getConnectorKey()
	{
		return "insightly";
	}
	
	public String getConnDBInfoQuery()
	{
		return "GetInsightlyDBInfo";
	}
 
}
