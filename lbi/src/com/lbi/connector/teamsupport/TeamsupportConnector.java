package com.lbi.connector.teamsupport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
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
	String field = "";
	String orgId="";
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
		
		try
		{
			modName = modInfo.getModMetaName();
			handleUrl(modInfo, mode);
			client  = HttpClients.createDefault();
			get = new HttpGet();
			get.setURI(new URI(url));
			get.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(orgId.getBytes()) + Base64.getEncoder().encodeToString(":".getBytes()) + Base64.getEncoder().encodeToString(authInfo.getToken().getBytes()));
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
					writeModData(bw, csvPrinter, req , modInfo, mode);
				}
				try
				{
					url="";
					colHeader = 0;
					bw.close();
					csvPrinter.close(); 
				}
				catch(IOException e) 
				{ 
					throw new Exception("IOException occurred while writing data");
				}
			}
			else
			{
				throw new Exception(respLine);
			}
		}
		
		finally
		{
			try
			{ 
				bw.close();
				csvPrinter.close(); 
			} 
			catch(IOException e) 
			{ 
				throw new Exception("IOException occurred while writing data"); 
			}
		}		
	}

	private void handleUrl(ModuleInfo modInfo, int mode)
	{
		String baseUrl = dbInfo.getBaseUrl();
		
		if(url.equals(""))
		{
			url = modInfo.getModAttrValue(MODULE_URL);
			
			if(mode == INIT_MODE)
			{
				url = "https://"+baseUrl+url+"?DateModified=20101010000000";
			}
			else
			{
				String time = dbInfo.getLastFetchTime().toString();
				url = "https://"+baseUrl+url+"?DateModified="+time;
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
	
	private void writeModData(BufferedWriter bw, CSVPrinter csvPrinter,Request req, ModuleInfo modInfo, int mode) throws Exception
	{
		String userId = req.getUserId().toString();
		Long connId = dbInfo.getConnectorId();
		csvPrinter.print(userId);
		csvPrinter.print(connId);
		Iterator<String> it = modInfo.getAllFields().keySet().iterator();
		if(modName.equals("tickets") && obj.getJSONObject("Tags") != null)
		{
			obj1 = obj.getJSONObject("Tags");
			arr1 = obj1.getJSONArray("Tag");
			
			File dir = new File(CONNECTOR_DIR + File.separator + connId + "_" + mode + File.separator + "tag_info" + File.separator);
			if(!dir.isDirectory())
			{
				dir.mkdir();
			}
			File f = new File(dir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
			BufferedWriter buf = new BufferedWriter(new FileWriter(f));
			CSVPrinter csv = new CSVPrinter(buf, CSVFormat.DEFAULT);
			csv.print("TICKET_ID");
			csv.print("TAG_ID");
			csv.print("TAG_NAME");
			csv.println();
			for(int i = 0; i < arr1.length(); i++)
			{
				csv.print(obj.getString("id"));
				csv.print(generateTagId());
				csv.print(arr1.getJSONObject(i).getString("Value"));
			}
			try
			{
				buf.close();
				csv.close();
			}
			catch(IOException e) 
			{ 
				throw new Exception("IOException occurred while writing data");
			}	
		}
		while(it.hasNext())
		{
			field = it.next();
			
			if(field.equals("Name") && !obj.has("Name"))
			{
				String fName = getName("FirstName");
				String mName = getName("MiddleName");
				String lName = getName("LastName");
				String name = (fName == null ? "" : fName)  + (mName == null ? "" : mName) + (lName == null ? "" : lName);
				csvPrinter.print(name);
			}
			else if(obj.getString(field) != null)
			{
				csvPrinter.print(obj.getString(field));
			}
			else
			{
				csvPrinter.print("");
			}
		}
		csvPrinter.println();
	}

	private String generateTagId()
	{
		
		return null;
	}
	
	private String getName(String Name) throws Exception
	{
			return obj.getString(Name);
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
		orgId = req.getParam("ORGID", true);
		String sql = SQLQueryAPI.getSQLString("InsTeamsupportAddInfo", new Object[][]{{"CONNECTORID", dbInfo.getConnectorId()},{"ORGID", orgId}});
		SQLQueryAPI.executeUpdate(sql);
	}

}