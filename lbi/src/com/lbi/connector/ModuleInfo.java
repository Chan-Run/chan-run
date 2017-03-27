package com.lbi.connector;

import java.io.File;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.lbi.framework.app.Request;
import com.lbi.sql.SQLQueryAPI;


public class ModuleInfo implements ConnectorConstants
{
	
	private Long moduleId;
	private Long connectorId;
	private Timestamp lastFetchTime;
	private Timestamp fetchedUpto;
	private String modMetaName;
	private Integer status;
	
	private HashMap<String, ConnectorFieldInfo> curFields;
	private List selFields;
	private Element modDetails;
	private LinkedHashMap<String, NamedNodeMap> fields;
	private List<String> newFields;
	private List<String> delFields;
	
	public ModuleInfo()
	{
		
	}
	
	public ModuleInfo(Long moduleId, Long connectorId, Timestamp lastFetchTime, Timestamp fetchedUpto, Integer status, String modId)
	{
		this.moduleId = moduleId;
		this.connectorId = connectorId;
		this.lastFetchTime = lastFetchTime;
		this.fetchedUpto = fetchedUpto;
		this.modMetaName = modId;
		this.status = status;
		
//		loadCurFields();
	}
	
	public ModuleInfo(Object[] arr, ConnectorGenInfo genInfo) throws Exception
	{
		this.moduleId = (Long) arr[MODULES_ID];
		this.connectorId = (Long) arr[MODULES_CONNECTORID];
		this.lastFetchTime = (Timestamp) arr[MODULES_LASTFETCHTIME];
		this.fetchedUpto = (Timestamp) arr[MODULES_FETCHEDUPTO];
		this.modMetaName = (String) arr[MODULES_MODMETANAME];
		this.status = (Integer) arr[MODULES_STATUS];
		
		loadModDetails(genInfo);
		loadSelFields();
		loadCurFields();
	}
	
	private void loadSelFields() throws Exception
	{
		String sql = SQLQueryAPI.getSQLString("GetConnSelFields", new Object[][]{{"CONNECTORID", connectorId}, {"MODMETANAME", modMetaName}});
		selFields = SQLQueryAPI.getAsList(sql);
	}
	
	private void loadModDetails(ConnectorGenInfo genInfo) 
	{
		modDetails = genInfo.getModDetails(modMetaName);
		fields = genInfo.getModFieldDets(modMetaName);
	}
	
	public File getModuleDir(int mode, long connectorId)
	{
		File file = new File(CONNECTOR_DIR + File.separator + connectorId + "_" + mode + File.separator + modMetaName + File.separator);
		if(!file.isDirectory())
		{
			file.mkdir();
		}
		return file;
	}
	
	private void loadCurFields() throws Exception
	{
		String sql = SQLQueryAPI.getSQLString("GetConnCurFieldsOfMod", new Object[][]{{"CONNECTORID", this.connectorId}, {"MODMETANAME", modMetaName}});
		List<?> fieldsList = SQLQueryAPI.getAsList(sql);
		String field;
		
		for(int i = 0; i < fieldsList.size(); i++)
		{
			field = (String) fieldsList.get(i);
			curFields.put(field, new ConnectorFieldInfo(field, fields.get(field)));
		}
	}
	
	public Timestamp getLastFetchTime()
	{
		return lastFetchTime;
	}
	
	public Timestamp getFetchedUpto()
	{
		return fetchedUpto;
	}
	
	public int getStatus()
	{
		return status;
	}
	
	public String getModMetaName()
	{
		return this.modMetaName;
	}
	
	public Long getModuleId()
	{
		return this.moduleId;
	}
	
	public String getModAttrValue(String attrName)
	{
		return modDetails.getAttributeNode(attrName).getNodeValue();
	}
	
	public LinkedHashMap<String, NamedNodeMap> getAllFields()
	{
		return this.fields;
	}
	
	public HashMap<String, ConnectorFieldInfo> getCurFields()
	{
		return this.curFields;
	}
	
	public List getSelFields()
	{
		return this.selFields;
	}
	
	public void addNewField(String field)
	{
		if(newFields == null)
		{
			newFields = new ArrayList<String>();
		}
		newFields.add(field);
	}
	
	public void addDelField(String field)
	{
		if(delFields == null)
		{
			delFields = new ArrayList<String>();
		}
		delFields.add(field);
	}
	
	public List<String> getNewFields()
	{
		return newFields;
	}
	
	public List<String> getDelFields()
	{
		return delFields;
	}
	
	public void addFields(Request req) throws Exception
	{
		if(newFields.isEmpty()){ return; }
		ConnectorUtil.addFields(this, req);
	}
	
	public void delFields(Request req) throws Exception
	{
		if(delFields.isEmpty()){ return; }
		ConnectorUtil.delFields(this, req);
	}
	
}
