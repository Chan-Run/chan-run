package com.lbi.connector.zendesk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.sql.Timestamp;
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

public class ZendeskConnector extends Connector implements GeneralConstants, ZendeskConnectorConstants, ConnectorConstants, ConnectorXMLConstants
{
	
	public ZendeskConnector() throws Exception
	{
		super(ZENDESK_CONN_ID);
	}
	
	public ZendeskConnector(Long connectorId) throws Exception
	{
		super(ZENDESK_CONN_ID);
	}

	JSONArray arr;
	int colHeader = 0;
	int respCode = 0;
	int time = 0, objCount = 0;
	String url = "";
	String field = "";
	String baseUrl = "";
	String modName = "";
	String nextPage = "";
	String respLine = "";
	JSONObject json,obj;

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
					nextPage = obj.getString("next_page");
					objCount = obj.getInt("count");
					csvPrinter.print(userId);
					csvPrinter.print(connId);
					writeModData(modInfo, csvPrinter);
				}

				if(modName.equals("groups") || modName.equals("nps_surveys"))
				{
					if(!nextPage.equals("null"))
					{
						url = nextPage;
						downloadModData(modInfo, mode, req);
					}
					else
					{
						try
						{
							url="";
							colHeader = 0;
							ConnectorUtil.closeWriter(bw);
							bw.close();
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
					if(objCount == 1000)
					{
						url = nextPage;
						downloadModData(modInfo, mode, req);
					}
					else
					{
						try
						{
							url="";
							colHeader = 0;
							ConnectorUtil.closeWriter(bw);
							bw.close();
							csvPrinter.close(); 
						}
						catch(IOException e)
						{ 
							throw new Exception("IOException occurred while writing data");
						}
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
		
		if(modName.equals("tickets"))
		{
			JSONObject via, metric_sets, metrics;

			via = obj.getJSONObject("via");
			metric_sets = obj.getJSONObject("metric_set");
			
			while(it.hasNext())
			{
				field = it.next();
				if(TICKET.containsKey(field))
				{
					if(field.equals("via"))
					{
						csvPrinter.print(via.get(TICKET.get(field)));
					}
					else if(metric_sets.get(field) instanceof JSONObject)
					{
						metrics = metric_sets.getJSONObject(field);
						if(metrics.isNull(TICKET.get(field)))
						{
							csvPrinter.print("");
						}
						else
						{
							csvPrinter.print(metrics.get(TICKET.get(field)));
						}
					}
					else
					{
						if(metric_sets.isNull(TICKET.get(field)))
						{
							csvPrinter.print("");
						}
						else
						{
							csvPrinter.print(metric_sets.get(field));
						}
					}
				}
				else if(obj.isNull(field))
				{
					csvPrinter.print("");
				}
				else
				{
					csvPrinter.print(obj.get(field));
				}
			}
			csvPrinter.println();
		}
		else
		{
			while(it.hasNext())
			{
				if(obj.isNull(field))
				{
					csvPrinter.print("");
				}
				else
				{
					csvPrinter.print(obj.get(field));
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
				Thread.sleep(60000);
			}
			downloadModData(modInfo, mode, req);
		}
		else if(respCode == 503 && response.containsHeader("Retry-After"))
		{
				Header[] retryAfter = response.getHeaders("Retry-After");
				int wait = Integer.parseInt(retryAfter[0].getValue())*1000;
				Thread.sleep(wait);
				downloadModData(modInfo, mode, req);
		}
		else
		{
			throw new Exception(respLine);
		}
	}

	private void handleUrl(ModuleInfo modInfo, int mode) throws Exception
	{
		String baseUrl = dbInfo.getBaseUrl();
		
		if(url.equals("") && modName.equals("groups") || modName.equals("nps_surveys"))
		{
				url = modInfo.getModAttrValue(MODULE_URL);
				url = "https://"+baseUrl+url;
		}
		else if(url.equals(""))
		{
			url = modInfo.getModAttrValue(MODULE_URL);
			
			if(mode == INIT_MODE)
			{
				url = "https://"+baseUrl+url+"978307200";
			}
			else
			{
				Timestamp time = dbInfo.getLastFetchTime();
				url = "https://"+baseUrl+url+(int)(time.getTime()/1000);
			}
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