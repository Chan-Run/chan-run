package com.lbi.connector;


import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVPrinter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.NamedNodeMap;

import com.lbi.connector.capsulecrm.CapsuleCRMGenInfo;
import com.lbi.copydb.CopyUtil;
import com.lbi.datadump.DataLoader;
import com.lbi.datadump.ImportConstants;
import com.lbi.general.GeneralConstants;
import com.lbi.general.SysProp;
import com.lbi.sas.SequenceGenerator;
import com.lbi.sql.SQLQueryAPI;

import com.lbi.framework.app.Request;

public abstract class Connector implements ConnectorConstants, ConnectorXMLConstants, ImportConstants, GeneralConstants
{
	
	public int connector;
	
	protected ConnectorGenInfo genInfo = null;
	protected ConnectorDBInfo dbInfo = null;
	protected ConnectorAuthInfo authInfo = null;
	protected ConnectorClientInfo clientInfo = null;
	
	public abstract String getImportType(String modId, int mode);
	public abstract void insAdditionalInfo(Request req) throws Exception;
	public abstract void downloadModData(ModuleInfo modInfo, int mode, Request req) throws Exception;
	public void handleDeletedRecords(ModuleInfo modInfo, Request req) throws Exception {}
	
	private List<String> newMods;
	private List<String> delMods;
	
	public Connector(int connector) throws Exception
	{
		this.connector = connector;
		genInfo = CapsuleCRMGenInfo.getInstance();
		dbInfo = new ConnectorDBInfo();
	}
	
	public Connector(Long connectorId, int connector) throws Exception
	{
		this.connector = connector;
		genInfo = CapsuleCRMGenInfo.getInstance();
		dbInfo = new ConnectorDBInfo(connectorId, genInfo);
	}
	
	public void doInit(Request req) throws Exception
	{
		try
		{
			updateConnStatus(CONNECTOR_STATUS_INPROGRESS);
			deleteDownloadedFiles(false, INIT_MODE);
			downloadData(INIT_MODE, req);
			importData(req, INIT_MODE);
			handleInitSuxes(req);
		}
		catch(Exception e)
		{
			handleInitFailure(e, req);
		}
	}
	
	public void doSync(Request req) throws Exception
	{
		try
		{
			doPreRequisite(SYNC_MODE, false);
			updateConnStatus(CONNECTOR_STATUS_INPROGRESS);
//			handleModSelDeSel(req);
//			handleFieldSelDeSel(req);
			downloadData(SYNC_MODE, req);
			importData(req, SYNC_MODE);
// some connectors will provide deleted records as a seperate API. We need to download and delete in our db
			updateConnStatus(CONNECTOR_STATUS_SYNCSUXES);
		}
		catch(Exception e)
		{
			handleSyncFailure(e, req);
		}
	}
	
	public void downloadData(int mode, Request req) throws Exception
	{
		loadAuthInfo(connector, req.getUserId(), dbInfo.getEmail());
		loadClientInfo();
		HashMap<String, ModuleInfo> curModules = dbInfo.getCurModules();
		Iterator<Map.Entry<String, ModuleInfo>> it = curModules.entrySet().iterator();
		Map.Entry<String, ModuleInfo> entry;
		ModuleInfo modInfo;
		
		while(it.hasNext())
		{
			entry = it.next();
			modInfo = entry.getValue();
			downloadModData(modInfo, mode, req);
		}
	}
	
	
	public void importData(Request req, int mode) throws Exception
	{
		HashMap<String, ModuleInfo> curModules = dbInfo.getCurModules();
		Iterator<Map.Entry<String, ModuleInfo>> it = curModules.entrySet().iterator();
		Map.Entry<String, ModuleInfo> entry;
		ModuleInfo modInfo;
		
		while(it.hasNext())
		{
			entry = it.next();
			modInfo = entry.getValue();
			importModData(modInfo, mode, req);
		}
	}
	
	private void importModData(ModuleInfo modInfo, int mode, Request req) throws Exception
	{
		String importType = getImportType(modInfo.getModMetaName(), mode);
		String tableName = modInfo.getModAttrValue(MODULE_TABLENAME);
		String dirPath = modInfo.getModuleDir(mode, dbInfo.getConnectorId()).getAbsolutePath();
		handleDeletedRecords(modInfo, req);
		DataLoader dd = new DataLoader(importType, tableName, dirPath);
		dd.loadData(req, modInfo);
		ConnectorUtil.updateLastFetchTime(modInfo.getModuleId(), new Timestamp(System.currentTimeMillis()));
	}
	
	public void handleModSelDeSel(Request req) throws Exception
	{
		List<String> selModules = dbInfo.getSelModules();
		Set<String> curModules = dbInfo.getCurModules().keySet();
		newMods = new ArrayList<String>();
		delMods = new ArrayList<String>();
		
		for(String mod : curModules)
		{
			if(!selModules.contains(mod))
			{
				delMods.add(mod);
			}
		}
		
		for(String mod : selModules)
		{
			if(!curModules.contains(mod))
			{
				newMods.add(mod);
			}
		}
		CopyUtil.copyNewModules(newMods, genInfo.getTemplateId(), dbInfo.getProjId(), req);
		ConnectorUtil.delModules(dbInfo.getConnectorId(), delMods);
	}
	
	public void handleFieldSelDeSel(Request req) throws Exception
	{
		HashMap<String, ModuleInfo> curModules = dbInfo.getCurModules();
		ModuleInfo modInfo;
		
		for(Map.Entry<String, ModuleInfo> entry : curModules.entrySet())
		{
			modInfo = entry.getValue();
			
			for(String field : modInfo.getCurFields().keySet())
			{
				if(!modInfo.getSelFields().contains(field))
				{
					modInfo.addDelField(field);
				}
			}
			for(Object field : modInfo.getSelFields())
			{
				if(modInfo.getCurFields().containsKey(field))
				{
					modInfo.addNewField((String)field);
				}
			}
			modInfo.delFields(req);
			modInfo.addFields(req);
		}
	}
	
	public List<String> getNewModules()
	{
		return this.newMods;
	}
	
	public List<String> getDelModules()
	{
		return delMods;
	}
	
	public void handleInitFailure(Exception e, Request req) throws Exception
	{
		updateConnStatus(CONNECTOR_STATUS_INITFAIL);
		sendFailureMail(req);
	}
	
	public void handleInitSuxes(Request req) throws Exception
	{
		updateConnStatus(CONNECTOR_STATUS_INITSUXES);
		sendSuccessMail(req);
	}
	
	public void handleSyncFailure(Exception e, Request req) throws Exception
	{
		updateConnStatus(CONNECTOR_STATUS_SYNCFAIL);
		sendFailureMail(req);
	}
	
	public void sendSuccessMail(Request req)
	{
		
	}
	public void sendFailureMail(Request req)
	{
		
	}
	
	public void projNameCheck()
	{
		
	}
	
	public void addInitSch()
	{
		
	}
	
	public void addSyncSch(boolean status, Request req) throws Exception
	{
		String schTime = req.getParam("SCHTIME");
	}
	
	public void setup(Request req) throws Exception
	{
		Long projId = req.getLongParam("PROJID");
		
		if("true".equals(req.getBooleanParam("ISNEWPROJ")))
		{
			projId = CopyUtil.copyProject(req, genInfo.getTemplateId());
		}
		else 
		{
			CopyUtil.copyWithinProject(req, genInfo.getTemplateId(), projId);
		}
		insConnectorInfo(req, projId);
		insModuleInfo(req);
		insConnSelModsFields(req);
		insAdditionalInfo(req);
		addInitSch();
		addSyncSch(false, req);
	}
	
	/*
	 * {"modules":[{"mod1": ["f1", "f2"]}, {"mod2": ["f1", "f2"]}]}
	 */
	
	public void insConnSelModsFields(Request req) throws Exception
	{
		String selModsFieldsJson = req.getParam("SEL_MODS_FIELDS");
		JSONObject obj = new JSONObject(selModsFieldsJson);
		JSONArray modArr = obj.getJSONArray("modules");
		String mod;
//		Long connSelModId;
		
//		if(!genInfo.isFieldsSelectionAvailable())
//		{
			for(int i = 0; i < modArr.length(); i++)
			{
				mod = modArr.getString(i);
				ConnectorUtil.insConnSelMod(dbInfo.getConnectorId(), mod);
			}
//		}
//		else
//		{
//			JSONArray fieldsArr;
//			JSONObject ob;
//			Iterator<String> it;
//			String field;
//			
//			for(int i = 0; i< modArr.length(); i++)
//			{
//				ob = modArr.getJSONObject(i);
//				it = ob.keys();
//				while(it.hasNext())
//				{
//					mod = it.next();
//					connSelModId = ConnectorUtil.insConnSelMod(dbInfo.getConnectorId(), mod);
//					fieldsArr = ob.getJSONArray(mod);
//					for(int j = 0; j < fieldsArr.length(); j++)
//					{
//						field = fieldsArr.getString(j);
//						ConnectorUtil.insConnSelField(connSelModId, field);
//					}
//				}
//			}
//		}
	}
	
	public void insConnectorInfo(Request req, Long projId) throws Exception
	{
		dbInfo = ConnectorDBInfo.createAndUpdateNewInstance(req, projId, genInfo);
	}
	
	public void insModuleInfo(Request req) throws Exception
	{
		String[] selMods = ConnectorUtil.getSelModulesFromReq(req, genInfo.isFieldsSelectionAvailable());
		Long modId;
		for(String mod : selMods)
		{
			modId = SequenceGenerator.genNextValue("MOULES.MODULEID");
			String sql = SQLQueryAPI.getSQLString("InsModuleInfo", new Object[][]{{"MODULEID", modId}, {"CONNECTORID", dbInfo.getConnectorId()}, {"MODMETANAME", mod}, {"STATUS", MODULES_STATUS_YETTOSTART}, {"LASTFETCHTIME", ""}, {"FETCHEDUPTO", ""}});
			SQLQueryAPI.executeUpdate(sql);
		}
	}
	
	public void doPreRequisite(int mode, boolean delWithoutCheck) throws Exception
	{
		deleteDownloadedFiles(delWithoutCheck, mode);
		checkConnectorStatus();
	}
	
	public void deleteDownloadedFiles(boolean delWithoutCheck, int mode)
	{
		if(delWithoutCheck || (Boolean)SysProp.getSysProp(genInfo.getConnectorKey() + genInfo.getDeleteDataCheckKey()))
		{
			File dir = getConnDir(mode);
			if(dir.exists())
			{
				dir.delete();
			}
			return;
		}
	}
	
	public void checkConnectorStatus() throws Exception
	{
		if(dbInfo.getStatus() == CONNECTOR_STATUS_INPROGRESS)
		{
			throw new Exception("Another sync in progress");
		}
	}
	
	public File getConnDir(int mode)
	{
		File file = new File(CONNECTOR_DIR + dbInfo.getConnectorId() + "_" + mode);
		file.mkdir();
		return file;
		
	}
	
	public void updateConnStatus(int status) throws Exception
	{
		String sql = SQLQueryAPI.getSQLString("UpdConnStatus", new Object[][]{{"CONNECTOR", dbInfo.getConnectorId()}, {"STATUS", status}});
		SQLQueryAPI.executeUpdate(sql);
	}
	
	public void loadAuthInfo(int connector, Long userId, String email) throws Exception
	{
		authInfo = new ConnectorAuthInfo(connector, userId, email);
	}
	
	public void loadClientInfo() throws Exception
	{
		clientInfo = new ConnectorClientInfo(connector);
	}
	
	protected void printHeaders(ModuleInfo modInfo, CSVPrinter printer, Request req) throws Exception
	{
		Iterator<NamedNodeMap> it = modInfo.getAllFields().values().iterator();
		NamedNodeMap fieldDets;
		printer.print(req.getUserId());
		
		while(it.hasNext())
		{
			fieldDets = it.next();
			printer.print(fieldDets.getNamedItem(FIELD_METANAME).getNodeValue());
		}
		printer.println();
	}
}
