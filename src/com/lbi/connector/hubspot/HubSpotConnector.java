 package com.lbi.connector.hubspot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import com.lbi.connector.Connector;
import com.lbi.connector.ConnectorDBInfo;
import com.lbi.connector.ConnectorUtil;
import com.lbi.connector.ModuleInfo;
import com.lbi.connector.capsulecrm.CapsuleCRMGenInfo;
import com.lbi.framework.app.Request;

public class HubSpotConnector extends Connector 
{
	String accessToken;
	
	public HubSpotConnector() throws Exception
	{
		super(HUBSPOT_CRM_ID);
	}
	
	public HubSpotConnector(Long connectorId) throws Exception
	{
		super(connectorId, HUBSPOT_CRM_ID);
	}
	
	@Override
	public String getImportType(String modMetaName, int mode)
	{
		String impType = IMPORT_TYPE_ADD;
		if(modMetaName.equals("Users"))
		{
			impType = IMPORT_TYPE_DELETEADD;
		}
		else if(mode == SYNC_MODE)
		{
			impType = IMPORT_TYPE_UPDATEADD;
		}
		return impType;
	}

	@Override
	public void insAdditionalInfo(Request req)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void downloadModData(ModuleInfo modInfo, int mode, Request req) throws Exception
	{
		accessToken = getAccessToken();
		downloadData(modInfo, mode, req);
		downloadUsers(modInfo, mode, req);
	}
	
	private void downloadData(ModuleInfo modInfo, int mode, Request req) throws Exception
	{ 
		if(modInfo.getModMetaName().equals(USER_MOD_METANAME))
		{
			return;
		}
		
		String url = getUrl(modInfo, mode);
		downloadData(modInfo, url, "start", mode, req, true);
	}
	
	private void downloadData(ModuleInfo modInfo, String url, String offSet, int mode, Request req, boolean includeHeaders) throws Exception
	{
		CloseableHttpClient client  = HttpClients.createDefault();
		HttpGet method;
		JSONObject obj = null;
		StringBuilder tempUrl = new StringBuilder(url);
		
		File modDir = modInfo.getModuleDir(mode, dbInfo.getConnectorId());
		File file = new File(modDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
		
		BufferedWriter bw = null;
		CSVPrinter csvPrinter;
		StringWriter writer;
		
		int rowCount = 0;
		int totalRowCount = 0;
		int iterationLimit = 1;
		
		
		if(!offSet.equals("start"))
		{
			tempUrl.append("&").append(modInfo.getModAttrValue("offset")).append("=").append(offSet);
		}
		try
		{
			
			bw = new BufferedWriter(new FileWriter(file));
			csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
			
			if(includeHeaders)
			{
				printHeaders(modInfo,csvPrinter, req);
			}
			
			do
			{
				method = getMethod(tempUrl.toString(), accessToken);
				CloseableHttpResponse resp = client.execute(method);
				int respCode = resp.getStatusLine().getStatusCode();
				
				if(respCode == 429) // Rate Limit per day
				{
					String errResp = IOUtils.toString(resp.getEntity().getContent(), UTF8);
					obj = new JSONObject(errResp);
					if(obj.getString("policyName").equals("DAILY"))
					{
						throw new Exception("Rate Limit Exceeded");
					}
					Thread.sleep(2000);
					downloadData(modInfo, url, offSet, mode, req, false);
				}
				
				if(respCode == 502 || respCode == 504)
				{
					Thread.sleep(60000);
					downloadData(modInfo, url, offSet, mode, req, false);
				}
				
				writer = new StringWriter();
				IOUtils.copy(resp.getEntity().getContent(), writer, UTF8);
				obj = new JSONObject(writer.toString());
				rowCount = writeData(modInfo, csvPrinter, obj, req);
				totalRowCount += rowCount;
				
				if(totalRowCount % 200000 == 0)
				{
					ConnectorUtil.closeWriter(bw);
					bw = new BufferedWriter(new FileWriter(file));
					csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
					printHeaders(modInfo, csvPrinter, req);
				}
				
				offSet = obj.getString(modInfo.getModAttrValue("offset"));
				iterationLimit ++;
				
			}while(obj != null && obj.getBoolean(modInfo.getModAttrValue("hasmore")) && iterationLimit <= 200000);
		}
		finally
		{
			ConnectorUtil.closeWriter(bw);
		}
			
	}
	
	private int writeData(ModuleInfo modInfo, CSVPrinter printer, JSONObject resp, Request req) throws Exception
	{
		JSONArray arr = resp.getJSONArray(modInfo.getModAttrValue(MODULE_RESPNAME));
		Iterator<String> fieldIt = modInfo.getAllFields().keySet().iterator();
		JSONObject company;
		String val = "";
		String field;
		JSONObject properties;
		
		int i;
		
		for(i = 0; i < arr.length(); i++)
		{
			company = arr.getJSONObject(i);
			printer.print(req.getUserId());
			properties = company.getJSONObject("properties");
			while(fieldIt.hasNext())
			{
				val = "";
				field = fieldIt.next();
				if(!isProperty(field, modInfo) && company.has(field))
				{
					val = company.getString(field);
				}
				else if(properties.has(field))
				{
					val = properties.getJSONObject(field).getString("value");
				}
				printer.print(val);
			}
			printer.println();
		}
		return i + 1;
	}
	
	private void downloadUsers(ModuleInfo modInfo, int mode, Request req) throws Exception
	{
		if(!modInfo.getModMetaName().equals(USER_MOD_METANAME))
		{
			return;
		}
		String url = modInfo.getModAttrValue(MODULE_URL);
		CloseableHttpClient client  = HttpClients.createDefault();
		HttpGet method;
		JSONObject obj = null;
		StringBuilder tempUrl = new StringBuilder(url);
		
		method = getMethod(tempUrl.toString(), accessToken);
		CloseableHttpResponse resp = client.execute(method);
		int respCode = resp.getStatusLine().getStatusCode();
		
		if(respCode == 429) // Rate Limit per day
		{
			String errResp = IOUtils.toString(resp.getEntity().getContent(), UTF8);
			obj = new JSONObject(errResp);
			if(obj.getString("policyName").equals("DAILY"))
			{
				throw new Exception("Rate Limit Exceeded");
			}
			Thread.sleep(2000);
			downloadUsers(modInfo, mode, req);
		}
		
		if(respCode == 502 || respCode == 504)
		{
			Thread.sleep(60000);
			downloadUsers(modInfo, mode, req);
		}
		
		StringWriter writer = new StringWriter();
		IOUtils.copy(resp.getEntity().getContent(), writer, UTF8);
		obj = new JSONObject(writer.toString());
		writeUsersData(modInfo, writer, mode, req);
	}
	
	private void writeUsersData(ModuleInfo modInfo, StringWriter writer, int mode, Request req) throws Exception
	{
		File modDir = modInfo.getModuleDir(mode, dbInfo.getConnectorId());
		File file = new File(modDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		CSVPrinter csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
		
		JSONArray users = new JSONArray(writer.toString());
		JSONObject user;
		String field;
		String val;
		
		Iterator<String>  it = modInfo.getAllFields().keySet().iterator();
		
		printHeaders(modInfo, csvPrinter, req);
		
		for(int i = 0; i < users.length(); i++)
		{
			user = users.getJSONObject(i);
			while(it.hasNext())
			{
				val = "";
				field = it.next();
				if(user.has(field))
				{
					val = user.getString(field);
				}
				csvPrinter.print(val);
			}
			
			if(i % 200000 == 0)
			{
				ConnectorUtil.closeWriter(bw);
				bw = new BufferedWriter(new FileWriter(file));
				csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
				printHeaders(modInfo, csvPrinter, req);
			}
			csvPrinter.println();
		}
	}
	
	private String getUrl(ModuleInfo modInfo, int mode)
	{
		StringBuilder url = new StringBuilder();
		if(mode == INIT_MODE)
		{
			url.append(modInfo.getModAttrValue(MODULE_URL));
			Iterator<String> it = modInfo.getAllFields().keySet().iterator();
			String field;
			while(it.hasNext())
			{
				field = it.next();
				if(isProperty(field, modInfo))
				{
					url.append("&").append(modInfo.getModAttrValue("property")).append("=").append(field);
				}
			}
		}
		else
		{
			url.append(modInfo.getModAttrValue(MODULE_SYNCURL));
		}
		return url.toString();
	}
	
	private boolean isProperty(String field, ModuleInfo modInfo)
	{
		return modInfo.getAllFields().get(field).getNamedItem(FIELD_ISPROPERTY).getNodeValue().toString().equals("true");
	}
	
	public String getAccessToken() throws Exception
	{
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet method = new HttpGet();
		StringBuilder url = new StringBuilder(genInfo.getAccessTokenUrl());
		
		url.append("?refresh_token=").append(authInfo.getRefreshToken()).
		append("&client_id=").append(clientInfo.getClientId()).append("&client_secret=").
		append(clientInfo.getClientSecret()).append("&grant_type=refresh_token");
	
		CloseableHttpResponse response = client.execute(method);
		String accToken = "";
		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
		{
			InputStream is = response.getEntity().getContent();
			StringWriter writer = new StringWriter();
			IOUtils.copy(is, writer, UTF8);
			JSONObject obj =  new JSONObject(writer.toString());
			accToken = obj.getString("access_token");
		}
		else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED)
		{
			throw new Exception("Unable to fetch access token. Invalid Refresh token");
		}
		
		return accToken;
	}
	
	private HttpGet getMethod(String url, String accessToken) throws Exception
	{
		HttpGet method = new HttpGet();
		
		method.setURI(new URI(url));
		method.addHeader("Authorization", "Bearer " + accessToken);	//NO I18N
		method.addHeader("Accept", "application/json");	//NO I18N
		return method;
		
	}

}
