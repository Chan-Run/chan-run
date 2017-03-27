package com.lbi.connector;

import java.util.HashMap;

import java.util.LinkedHashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.lbi.general.SysProp;

public abstract class ConnectorGenInfo
{
	protected ConnectorXMLLoader loader;
	
	public abstract String getConnectorKey();
	
	public abstract String getConnDBInfoQuery();
	
	public LinkedHashMap<String, Element> modIdVsDetails;
	
	public HashMap<String, LinkedHashMap<String, NamedNodeMap>> modIdVsFieldsDets;
	
	public void loadConnectorXML() throws Exception
	{
		loader = new ConnectorXMLLoader(ConnectorConstants.CONNECTOR_DETAILS_XML_FILE_PATH);
		loader.setConnector(getConnectorKey());
		modIdVsDetails = loader.getModIdVsDetails();
		modIdVsFieldsDets = loader.getModIdVsFieldDetsmap();
	}
	
	public Element getModDetails(String modId)
	{
		return modIdVsDetails.get(modId);
	}
	
	public LinkedHashMap<String, NamedNodeMap> getModFieldDets(String modId)
	{
		return modIdVsFieldsDets.get(modId);
	}
	
	public boolean isApiKey()
	{
		return Boolean.FALSE;
	}
	
	public Long getTemplateId()
	{
		return (Long)SysProp.getSysProp("lbi." + getTemplateKey());
	}
	
	public boolean isFieldsSelectionAvailable()
	{
		return Boolean.TRUE;
	}
	
	public String getTemplateKey()
	{
		return ".template.id";
	}
	
	public String getDeleteDataCheckKey()
	{
		return ".del_conn_file_without_check";
	}
	
	public String getAccessTokenUrl()
	{
		return "";
	}
	
}
