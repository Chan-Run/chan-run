package com.lbi.connector;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.NamedNodeMap;

import com.lbi.framework.app.Request;
import com.lbi.meta.Fields;
import com.lbi.sas.SequenceGenerator;
import com.lbi.sql.SQLQueryAPI;

public class ConnectorUtil implements Fields
{
	public static Long insConnSelMod(Long connDBInfoId, String modId) throws Exception
	{
		Long connSelModId;
		String sql;
		connSelModId = SequenceGenerator.genNextValue("ConnectorSelModules.CONNSELMODID");
		sql = SQLQueryAPI.getSQLString("InsConnSelModules", new Object[][]{{"CONNNSELMODID", connSelModId}, {"CONNDBINFOID", connDBInfoId}, {"MODID", modId}});
		SQLQueryAPI.executeUpdate(sql);
		
		return connSelModId;
	}
	
	public static Long insConnSelField(Long connSelModId, String fieldId) throws Exception
	{
		Long connSelFieldId;
		String sql;
		
		connSelFieldId = SequenceGenerator.genNextValue("ConnectorSelFields.CONNSELFIELDID");
		sql = SQLQueryAPI.getSQLString("InsConnSelFields", new Object[][]{{"CONNSELFIELDID", connSelFieldId}, {"CONNSELMODID", connSelFieldId}, {"FIELDID", fieldId}});
		SQLQueryAPI.executeUpdate(sql);
		return connSelFieldId;
	}
	
	public static String[] getSelModulesFromReq(Request req, boolean isFieldsAvailable) throws Exception
	{
		
		String selModsFieldsJson = req.getParam("SEL_MODS_FIELDS");
		JSONObject obj = new JSONObject(selModsFieldsJson);
		JSONArray modArr = obj.getJSONArray("modules");
		
		String mod;
		String[] arr = new String[modArr.length()];
		
		if(!isFieldsAvailable)
		{
			for(int i = 0; i < modArr.length(); i++)
			{
				mod = modArr.getString(i);
				arr[i] = mod;
			}
		}
		else
		{
			JSONObject ob;
			Iterator<String> it;
			
			for(int i = 0; i< modArr.length(); i++)
			{
				ob = modArr.getJSONObject(i);
				it = ob.keys();
				while(it.hasNext())
				{
					mod = it.next();
					arr[i] = mod;
				}
			}
		}
		return arr;
	}
	
	public static void delFields(ModuleInfo modInfo, Request req) throws Exception
	{
		String sql = SQLQueryAPI.getSQLString("DelFeilds", new Object[][]{{"MODULEID", modInfo.getModuleId()}, {"FIELDS", modInfo.getDelFields()}});
		SQLQueryAPI.executeQuery(sql);
	}
	
	public static void addFields(ModuleInfo modInfo, Request req) throws Exception
	{
		Object[][] fieldInfo = new Object[modInfo.getNewFields().size()][Fields.FIELDS_TOTALCOLS];
		NamedNodeMap map;
		String field;
		
		for(int i = 0; i < modInfo.getNewFields().size(); i++)
		{
			field = modInfo.getNewFields().get(i);
			map = modInfo.getAllFields().get(field);
			fieldInfo[i][Fields.FIELDS_METANAME] = field;
			fieldInfo[i][Fields.FIELDS_ID] = SequenceGenerator.genNextValue("FIELDS");
			fieldInfo[i][Fields.FIELDS_NAME] = map.getNamedItem(ConnectorXMLConstants.FIELD_DISPLAYNAME);
			fieldInfo[i][Fields.FIELDS_MAXSIZE] = "";
			fieldInfo[i][Fields.FIELDS_MODULEID] = modInfo.getModuleId();
			fieldInfo[i][Fields.FIELDS_DATATYPE] = modInfo.getAllFields().get(field).getNamedItem(ConnectorXMLConstants.FIELD_TYPE);
		}
		
		SQLQueryAPI.executeQuery("InsFields", new Object[][]{{"VALUES", fieldInfo}});
	}
	
	public static void delModules(Long connectorId, List<String> modules)
	{
		// remove entries from ConnectorModuleInfo and records from corresponding table
	}
	
	public static void updateLastFetchTime(Long moduleId, Timestamp time) throws Exception
	{
		String sql = SQLQueryAPI.getSQLString("UpdModLastFetchTime", new Object[][]{{"MODULEID", moduleId}, {"LASTSYNCTIME", time}});
		SQLQueryAPI.executeQuery(sql);
	}
	
	public static void closeWriter(Writer w) throws Exception
	{
		if(w != null)
		{
			try{ w.close(); } catch(IOException e) { throw new Exception("IOException occurred while writing data"); }
		}
	}

	
}
