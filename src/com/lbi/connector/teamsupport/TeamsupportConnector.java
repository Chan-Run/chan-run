package com.lbi.connector.teamsupport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.NamedNodeMap;

import com.lbi.connector.Connector;
import com.lbi.connector.ConnectorConstants;
import com.lbi.connector.ConnectorUtil;
import com.lbi.connector.ConnectorXMLConstants;
import com.lbi.connector.ModuleInfo;
import com.lbi.framework.app.Request;
import com.lbi.general.GeneralConstants;
import com.lbi.sql.SQLQueryAPI;

public class TeamsupportConnector extends Connector implements GeneralConstants, ConnectorConstants, ConnectorXMLConstants
{

	public TeamsupportConnector() throws Exception
	{
		super(TEAMSUPPORT_CONN_ID);
	}
	
	public TeamsupportConnector(Long connectorId) throws Exception
	{
		super(TEAMSUPPORT_CONN_ID);
	}
	
	JSONArray arr,arr1;
	int respCode = 0;
	int colHeader = 0;
	String url = "";
	String baseUrl = "";
	String field = "";
	long orgId = 0l;
	String modName = "";
	String respLine = "";
	JSONObject json, obj,obj1,obj2;
	
	public void downloadModData(ModuleInfo modInfo, int mode, Request req) throws Exception
	{
		CSVPrinter csvPrinter = null;
		File modDir;
		File file;
		HttpGet get;
		int count = 0;
		InputStream is;
		String resp = "";
		BufferedWriter bw = null;
		CloseableHttpClient client;
		CloseableHttpResponse response;
		String userId = req.getUserId().toString();
		Long connId = dbInfo.getConnectorId();
		
		try
		{
			modName = modInfo.getModMetaName();
			handleUrl(modInfo, mode);
			client  = HttpClients.createDefault();
			get = new HttpGet();
			get.setURI(new URI(url));
			get.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(String.valueOf(orgId).getBytes()) + Base64.getEncoder().encodeToString(":".getBytes()) + Base64.getEncoder().encodeToString(authInfo.getToken().getBytes()));
			response = client.execute(get);
			respCode = response.getStatusLine().getStatusCode();
			if(respCode == 200)
			{
				is = response.getEntity().getContent();
				StringWriter writer = new StringWriter();
				IOUtils.copy(is, writer, UTF8);
				resp = writer.toString();
				modDir = modInfo.getModuleDir(mode, dbInfo.getConnectorId());
				file = new File(modDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
				bw = new BufferedWriter(new FileWriter(file));
				csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
				createColHeader(csvPrinter, modInfo);
				json = new JSONObject(resp);
				arr = json.getJSONArray(modName);
				for(int i = 0; i < arr.length(); i++)
				{
					count++;
					if(count == 200000)
					{
						count = 0;
						file = new File(modDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
					}
					obj = arr.getJSONObject(i);
					csvPrinter.print(userId);
					csvPrinter.print(connId);
					writeModData(modInfo, csvPrinter);
				}
				try
				{
					url="";
					colHeader = 0;
					ConnectorUtil.closeWriter(bw);
					csvPrinter.close(); 
				}
				catch(IOException e) 
				{ 
					throw new Exception("IOException occurred while writing data");
				}
			}
			else
			{
				handleResponse(modInfo, mode, req, response);
			}
		}
		
		finally
		{
			try
			{
				ConnectorUtil.closeWriter(bw);
				csvPrinter.close(); 
			} 
			catch(IOException e) 
			{ 
				throw new Exception("IOException occurred while writing data"); 
			}
		}		
	}

	private void writeModData(ModuleInfo modInfo, CSVPrinter csvPrinter) throws Exception
	{
		Iterator<String> it = modInfo.getAllFields().keySet().iterator();
		
		while(it.hasNext())
		{
			field = it.next();
			
			if(field.equals("Name") && !obj.has("Name"))
			{
				String fName = getName("FirstName");
				String mName = getName("MiddleName");
				String lName = getName("LastName");
				String name = (fName.equals("null") ? "" : fName)  + (mName.equals("null") ? "" : mName) + (lName.equals("null") ? "" : lName);
				csvPrinter.print(name);
			}
			else if(obj.isNull(field))
			{
				csvPrinter.print("");
			}
			else
			{
				csvPrinter.print(obj.getString(field));
			}
		}
		csvPrinter.println();
	}

	private void handleResponse(ModuleInfo modInfo, int mode, Request req, CloseableHttpResponse response) throws Exception
	{
		if(respCode == 429)
		{
			Thread.sleep(3600000);
			downloadModData(modInfo, mode, req);
		}
		else
		{
			throw new Exception(respLine);
		}
	}

	private void handleUrl(ModuleInfo modInfo, int mode) throws Exception
	{
		baseUrl = dbInfo.getBaseUrl();
		
		if(url.equals(""))
		{
			url = modInfo.getModAttrValue(MODULE_URL);
			
			if(mode == INIT_MODE)
			{
				url = "https://"+baseUrl+url+"20010101000000";
			}
			else
			{
			    SimpleDateFormat d = new SimpleDateFormat("yyyyMMddHHmmss");
			    String time = d.format(dbInfo.getLastFetchTime().clone());
				url = "https://"+baseUrl+url+time;
			}
		}
		
	}
	
	private void createColHeader(CSVPrinter csvPrinter, ModuleInfo modInfo) throws Exception
	{
		if(colHeader == 0)
		{
			colHeader = 1;
			csvPrinter.print("USER_ID");
			csvPrinter.print("CONNECTOR_ID");
			Iterator<NamedNodeMap> it = modInfo.getAllFields().values().iterator();
			while(it.hasNext())
			{
				csvPrinter.print(it.next().getNamedItem("name").getNodeValue());
			}
			csvPrinter.println();
		}
	}
	
	private String getName(String name) throws Exception
	{
			return obj.getString(name);
	}
	
	@Override
	public String getImportType(String modId, int mode)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insAdditionalInfo(Request req) throws Exception
	{
		orgId = req.getLongParam("ORGID", true);
		String sql = SQLQueryAPI.getSQLString("InsTeamsupportAddInfo", new Object[][]{{"CONNECTORID", dbInfo.getConnectorId()},{"ORGID", orgId}});
		SQLQueryAPI.executeUpdate(sql);
	}

}