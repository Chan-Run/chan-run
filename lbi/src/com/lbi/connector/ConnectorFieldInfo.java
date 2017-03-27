package com.lbi.connector;

import org.w3c.dom.NamedNodeMap;

public class ConnectorFieldInfo
{
	String field;
	NamedNodeMap details;
	
	public ConnectorFieldInfo(String field, NamedNodeMap fieldInfo) 
	{
		this.field = field;
		this.details = fieldInfo;
	}
	
	public String getAttrValue(String attrName)
	{
		return details.getNamedItem(attrName).getNodeValue();
	}
	
}
