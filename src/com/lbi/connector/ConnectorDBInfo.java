package com.lbi.connector;

import java.sql.Timestamp;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.lbi.sas.SequenceGenerator;
import com.lbi.sql.SQLQueryAPI;

import com.lbi.framework.app.Request;

public class ConnectorDBInfo implements ConnectorConstants
{
	private Long connectorId;
	private Integer status;
	private String schTime;
	private Timestamp lastFetchTime;
	private String profileName;
	private Long projId;
	private String email;
	private String baseUrl;
	private ConnectorAuthInfo authInfo;
	
	private HashMap<String, ModuleInfo> curModules;
	private List<String> selModules;
		
	public ConnectorDBInfo()
	{
		
	}
	
	public ConnectorDBInfo(Long connectorId, ConnectorGenInfo genInfo) throws Exception
	{
		String sql = SQLQueryAPI.getSQLString(genInfo.getConnDBInfoQuery(), new Object[][]{{"CONNECTORID", connectorId}});
		Object[] result = (Object[]) SQLQueryAPI.executeQuery(sql);
		connectorId = (Long) result[CONNECTOR_CONNECTORID];
		status = (Integer) result [CONNECTOR_STATUS];
		schTime = (String) result[CONNECTOR_SCHTIME];
		lastFetchTime = (Timestamp) result[CONNECTOR_LASTSYNCTIME];
		profileName = (String) result[CONNECTOR_ORGNAME];
		projId = (Long) result[CONNECTOR_PROJID];
		email = (String) result[CONNECTOR_EMAIL];
		baseUrl = (String) result[CONNECTOR_BASEURL];
				
		loadCurrentModules(genInfo);
		loadSelectedModules();
	}
	
	public String getConnDBInfoQuery()
	{
		return "GetConnDBInfo";
	}
	
	public static ConnectorDBInfo createAndUpdateNewInstance(Request req, Long projId, ConnectorGenInfo genInfo) throws Exception
	{
		Long connectorId = SequenceGenerator.genNextValue("CONNECTORID");
		String baseUrl = req.getOptionalParam("BASEURL") == null ? "" : req.getParam("BASEURL");
		String sql = SQLQueryAPI.getSQLString("InsConnDBInfo", new Object[][]{{"CONNECTORID", connectorId}, {"PROJID", projId}, {"STATUS", CONNECTOR_STATUS_YETTOSTART}, {"LASTFETCHTIME", ""}, {"PROFILENAME", req.getParam("PROFILENAME", true)}, {"SCHTIME", req.getParam("SCHTIME", true)}, {"BASEURL", baseUrl}});
		SQLQueryAPI.executeUpdate(sql);
		return new ConnectorDBInfo(connectorId, genInfo);
	}
	
	private void loadSelectedModules() throws Exception
	{ // ConnSelModules
		String sql = SQLQueryAPI.getSQLString("GetSelModules", new Object[][]{{"CONNECTORID", this.connectorId}});
		selModules = (List) SQLQueryAPI.executeQuery(sql);
	}
	
	private void loadCurrentModules(ConnectorGenInfo genInfo) throws Exception
	{ // ModuleInfo
		String sql = SQLQueryAPI.getSQLString("GetConnCurModules", new Object[][]{{"CONNECTORID", this.connectorId}});
		Object[][] result = (Object[][]) SQLQueryAPI.executeQuery(sql);
		ModuleInfo modInfo;
		for(int i = 0; i < result.length; i++)
		{
			modInfo = new ModuleInfo(result[i], genInfo);
			curModules.put((String)result[i][MODULES_MODMETANAME], modInfo);
		}
	}
	
	public HashMap<String, ModuleInfo> getCurModules()
	{
		return curModules;
	}
	
	public List<String> getSelModules()
	{
		return selModules;
	}
	
	public Long getConnectorId()
	{
		return this.connectorId;
	}
	
	public Integer getStatus()
	{
		return this.status;
	}
	
	public Timestamp getLastFetchTime()
	{
		return this.lastFetchTime;
	}
	
	public Long getProjId()
	{
		return this.projId;
	}
	
	public String getEmail()
	{
		return this.email;
	}
	
	public String getBaseUrl()
	{
		return this.baseUrl;
	}
	
}
