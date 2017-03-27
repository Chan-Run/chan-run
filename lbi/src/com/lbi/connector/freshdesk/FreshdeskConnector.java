package com.lbi.connector.freshdesk;

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
import org.apache.http.Header;
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

public class FreshdeskConnector extends Connector implements GeneralConstants, FreshdeskConnectorConstants, ConnectorConstants, ConnectorXMLConstants
{
	
	public FreshdeskConnector() throws Exception
	{
		super(FRESHDESK_CONN_ID);
	}
	
	public FreshdeskConnector(Long connectorId) throws Exception
	{
		super(FRESHDESK_CONN_ID);
	}
	
	JSONArray arr, arr1, arr2;
	int colHeader = 0;
	int respCode = 0;
	String url = "";
	String field = "";
	String baseUrl = "";
	String modName = "";
	String nextPage = "";
	String respLine = "";
	JSONObject obj, obj1;

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
			get.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(authInfo.getToken().getBytes()) + Base64.getEncoder().encodeToString(":ABCD".getBytes()));
			response = client.execute(get);
			respCode = response.getStatusLine().getStatusCode();
			respLine = response.getStatusLine().getReasonPhrase();
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
				arr = new JSONArray(resp);
				for(int i = 0; i < arr.length(); i++)
				{
					count++;
					if(count == 200000)
					{
						count = 0;
						file = new File(modDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
					}
					obj = arr.getJSONObject(i);
					writeModData(csvPrinter, req, modInfo, mode);
				}
				handleConnection(modInfo, mode, req, response, bw, csvPrinter);
			}
			else if(respCode == 429)
			{
				Header[] retryAfter = response.getHeaders("Retry-After");
				int wait = Integer.parseInt(retryAfter[0].getValue());
				Thread.sleep(wait);
				downloadModData(modInfo, mode, req);
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
	
	private void handleConnection(ModuleInfo modInfo, int mode, Request req, CloseableHttpResponse response, BufferedWriter bw, CSVPrinter csvPrinter) throws Exception
	{

		if(response.containsHeader("link"))
		{
			Header[] link = response.getHeaders("link");
			nextPage = link[0].getValue();
		}
		else
		{
			nextPage = "";
		}
		
		if(nextPage.equals(""))
		{
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
			url = nextPage.replace('<', ' ').replace(">; rel=\"next\"", "").trim();
			downloadModData(modInfo, mode, req);
		}
		
	}

	private void handleUrl(ModuleInfo modInfo, int mode)
	{
		
		if(url.equals("") && modName.equals("tickets"))
		{
			baseUrl = dbInfo.getBaseUrl();
			url = modInfo.getModAttrValue(MODULE_URL);
			
			if(mode == INIT_MODE)
			{
				url = "https://"+baseUrl+url+"?per_page=100&include=stats&updated_since=2010-10-10";
			}
			else
			{
				String time = dbInfo.getLastFetchTime().toString();
				url = "https://"+baseUrl+url+"?per_page=100&include=stats&updated_since="+time;
			}
		}
		else if(url.equals(""))
		{
			baseUrl = dbInfo.getBaseUrl();
			url = modInfo.getModAttrValue(MODULE_URL);
			url = "https://"+baseUrl+url+"?per_page=100";
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
	
	private void writeModData(CSVPrinter csvPrinter, Request req, ModuleInfo modInfo, int mode) throws Exception
	{
		String userId = req.getUserId().toString();
		Long connId = dbInfo.getConnectorId();
		csvPrinter.print(userId);
		csvPrinter.print(connId);
		Iterator<String> it = modInfo.getAllFields().keySet().iterator();
		if(modName.equals("agents"))
		{
			obj1 = obj.getJSONObject("contact");
			while(it.hasNext())
			{
				field = it.next();
				if(field.equals("ticket_scope"))
				{
					csvPrinter.print(scope.get(Integer.valueOf(obj.getInt(field))));
				}
				else if(obj.has(field))
				{
					csvPrinter.print(obj.getString(field));
				}
				else
				{
					csvPrinter.print(obj1.getString(field));
				}
			}
			csvPrinter.println();
		}
		else
		{
			if(modName.equals("sla_policies") && obj.getJSONObject("applicable_to").length() != 0)
			{
				obj1 = obj.getJSONObject("applicable_to");
				File dir = new File(CONNECTOR_DIR + File.separator + connId + "_" + mode + File.separator + "sla_info" + File.separator);
				if(!dir.isDirectory())
				{
					dir.mkdir();
				}
				File f = new File(dir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
				BufferedWriter buf = new BufferedWriter(new FileWriter(f));
				CSVPrinter csv = new CSVPrinter(buf, CSVFormat.DEFAULT);
				csv.print("SLA_ID");
				csv.print("GROUP_IDS");
				csv.print("COMPANY_IDS");
				csv.println();
				
				if(obj1.has("group_ids") && obj1.has("company_ids"))
				{
					arr1 = obj1.getJSONArray("group_ids");
					arr2 = obj1.getJSONArray("company_ids");
					
					if(arr1.length()>=arr2.length())
					{
						for(int i = 0; i<arr1.length(); i++)
						{
							csv.print(obj.getString("id"));
							csv.print(arr1.getString(i));
							if(i<arr2.length())
							{
								csv.print(arr2.getString(i));
							}
							else
							{
								csv.print("");
							}
						}
					}
					else
					{
						for(int i = 0; i<arr2.length(); i++)
						{
							csv.print(obj.getString("id"));
							if(i<arr1.length())
							{
								csv.print(arr1.getString(i));
							}
							else
							{
								csv.print("");
							}
							csv.print(arr2.getString(i));
						}
					}
				}
				else if(obj1.has("group_ids") && !obj1.has("company_ids"))
				{
					arr1 = obj1.getJSONArray("group_ids");
					
					for(int i = 0; i<arr1.length(); i++)
					{
						csv.print(obj.getString("id"));
						csv.print(arr1.getString(i));
						csv.print("");
					}
				}
				else if(!obj1.has("group_ids") && obj1.has("company_ids"))
				{
					arr1 = obj1.getJSONArray("company_ids");
					
					for(int i = 0; i<arr1.length(); i++)
					{
						csv.print(obj.getString("id"));
						csv.print("");
						csv.print(arr1.getString(i));
					}
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
				
				if(field.equals("source") || field.equals("status") || field.equals("priority"))
				{
					switch(field)
					{
						case "source" : 
							
							csvPrinter.print(source.get(Integer.valueOf(obj.getInt(field))));
							break;
							
						case "status" :
							
							csvPrinter.print(status.get(Integer.valueOf(obj.getInt(field))));
							break;
							
						case "priority" :
							
							csvPrinter.print(priority.get(Integer.valueOf(obj.getInt(field))));
							break;
					}
				}
				else
				{
					csvPrinter.print(obj.getString(field));
				}
			}
		}
		csvPrinter.println();
	}

	@Override
	public String getImportType(String modId, int mode)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void insAdditionalInfo(Request req)
	{
		// TODO Auto-generated method stub
	}
	
}