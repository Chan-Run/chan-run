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
import com.lbi.connector.ConnectorUtil;
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
					String userId = req.getUserId().toString();
					Long connId = dbInfo.getConnectorId();
					csvPrinter.print(userId);
					csvPrinter.print(connId);
					writeModData(modInfo, csvPrinter);
				}
				
				if(response.containsHeader("link"))
				{
					Header[] link = response.getHeaders("link");
					nextPage = link[0].getValue();
					url = nextPage.replace('<', ' ').replace(">; rel=\"next\"", "").trim();
					downloadModData(modInfo, mode, req);
				}
				else
				{
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
		
		if(modName.equals("agents"))
		{
			obj1 = obj.getJSONObject("contact");
			while(it.hasNext())
			{
				field = it.next();
				if(field.equals("ticket_scope"))
				{
					csvPrinter.print(SCOPE.get(obj.get(field)));
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
			while(it.hasNext())
			{
				field = it.next();
				
				if(field.equals("source") || field.equals("status") || field.equals("priority"))
				{
					switch(field)
					{
						case "source" : 
							
							csvPrinter.print(SOURCE.get(obj.get(field)));
							break;
							
						case "status" :
							
							csvPrinter.print(STATUS.get(obj.get(field)));
							break;
							
						case "priority" :
							
							csvPrinter.print(PRIORITY.get(obj.get(field)));
							break;
					}
				}
				else
				{
					csvPrinter.print(obj.getString(field));
				}
			}
			csvPrinter.println();
		}
	}

	private void handleResponse(ModuleInfo modInfo, int mode, Request req, CloseableHttpResponse response) throws Exception
	{
		if(respCode == 429)
		{
			if(response.containsHeader("Retry-After"))
			{
				Header[] retryAfter = response.getHeaders("Retry-After");
				int wait = Integer.parseInt(retryAfter[0].getValue())*1000;
				Thread.sleep(wait);
			}
			else
			{
				Thread.sleep(3600000);
			}
			downloadModData(modInfo, mode, req);
		}
		else
		{
			throw new Exception(respLine);
		}
	}

	private void handleUrl(ModuleInfo modInfo, int mode)
	{
		baseUrl = dbInfo.getBaseUrl();
		
		if(url.equals("") && modName.equals("tickets"))
		{
			url = modInfo.getModAttrValue(MODULE_URL);
			
			if(mode == INIT_MODE)
			{
				url = "https://"+baseUrl+url+"2001-01-01";
			}
			else
			{
				String time = dbInfo.getLastFetchTime().toInstant().toString();
				url = "https://"+baseUrl+url+time;
			}
		}
		else if(url.equals(""))
		{
				url = modInfo.getModAttrValue(MODULE_URL);
				url = "https://"+baseUrl+url;
		}
		
	}

	private void createColHeader(CSVPrinter csvPrinter, ModuleInfo modInfo) throws Exception
	{
		if(colHeader == 0)
		{
			colHeader ++;
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