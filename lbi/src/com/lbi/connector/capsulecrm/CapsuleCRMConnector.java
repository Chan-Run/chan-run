package com.lbi.connector.capsulecrm;

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.ClosedWatchServiceException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.w3c.dom.NamedNodeMap;

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
import com.lbi.connector.ConnectorAuthInfo;
import com.lbi.connector.ConnectorConstants;
import com.lbi.connector.ConnectorDBInfo;
import com.lbi.connector.ConnectorUtil;
import com.lbi.connector.ConnectorXMLConstants;
import com.lbi.connector.ModuleInfo;

import com.lbi.framework.app.Request;
import com.lbi.general.GeneralConstants;
import com.lbi.sql.SQLQueryAPI;

public class CapsuleCRMConnector extends Connector implements CapsuleCRMConstants
{
	String accessToken;
	
	public CapsuleCRMConnector() throws Exception
	{
		super(CAPSULE_CRM_ID);
	}
	
	public CapsuleCRMConnector(Long connectorId) throws Exception
	{
		super(connectorId, CAPSULE_CRM_ID);
	}
	
	public void downloadModData(ModuleInfo modInfo, int mode, Request req) throws Exception
	{
		accessToken = getAccessToken();
		downloadUsers(modInfo, mode, true, req);
		downloadParties(modInfo, mode, req);
		downloadOpportunities(modInfo, mode, req);
	}
	
	private void downloadUsers(ModuleInfo modInfo, int mode, boolean isFirst, Request req) throws Exception
	{
		if(!modInfo.getModMetaName().equals("Users"))
		{
			return;
		}
		
		String url = modInfo.getModAttrValue(MODULE_URL);
		CloseableHttpClient client  = HttpClients.createDefault();
		HttpGet method = getMethod(url, accessToken);
		CloseableHttpResponse resp = client.execute(method);
		int respCode = resp.getStatusLine().getStatusCode();
		if(respCode == HttpStatus.SC_UNAUTHORIZED)
		{
			if(!isFirst)
			{
				throw new RuntimeException("Access token may revoked. Unauthorized error occurred");
			}
			isFirst = false;
			accessToken = getAccessToken();
			downloadUsers(modInfo, mode, isFirst, req);
		}
		Header[] header = resp.getHeaders("X-RateLimit-Remaining");
		if(header[0].getValue().equals(0)) // Per Hour Limit Exceeded 
		{
			int time = Integer.parseInt(resp.getHeaders("X-RateLimit-Reset")[0].getValue()); // This header contains The time at which the current rate limit window resets in UTC epoch seconds.
			System.out.println("Rate Limit Exceeded");
			Thread.sleep(time-System.currentTimeMillis());
			downloadUsers(modInfo, mode, isFirst, req);
		}
		InputStream is = resp.getEntity().getContent();
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, UTF8);
		System.out.println(writer.toString());
		writeUsersData(modInfo, mode, writer.toString(), req);
	}
	
	private void writeUsersData(ModuleInfo modInfo, int mode, String response, Request req) throws Exception
	{
		File modDir = modInfo.getModuleDir(mode, dbInfo.getConnectorId());
		File file = new File(modDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
		BufferedWriter bw = null;
		CSVPrinter csvPrinter;
		
		try
		{
			bw = new BufferedWriter(new FileWriter(file));
			csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
			printHeaders(modInfo, csvPrinter, req);
			Iterator<String> fieldsIt = modInfo.getAllFields().keySet().iterator();
			JSONObject obj = new JSONObject(response);
			JSONArray users = obj.getJSONArray(modInfo.getModAttrValue(MODULE_RESPNAME));
			JSONObject user;
			String field;
			for(int i = 0; i < users.length(); i++)
			{
				user = users.getJSONObject(i);
				csvPrinter.print(req.getUserId());
				while(fieldsIt.hasNext())
				{
					field = fieldsIt.next();
					if(user.has(field))
					{
						csvPrinter.print(user.getString(field));
					}
					else
					{
						csvPrinter.print("");
					}
				}
				csvPrinter.println();
				if(i %  200000 == 0)
				{
					ConnectorUtil.closeWriter(bw);
					file = new File(modDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
					bw = new BufferedWriter(new FileWriter(file));
					csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
					printHeaders(modInfo, csvPrinter, req);
				}
			}
		}
		finally
		{
			ConnectorUtil.closeWriter(bw);
		}
	
	}

	// This API response contains Orgs and Contacts details. 
	private void downloadParties(ModuleInfo modInfo, int mode, Request req) throws Exception
	{
		if(!modInfo.getModMetaName().equals("Contacts"))
		{
			return;
		}
		String url = modInfo.getModAttrValue(MODULE_URL);
		url = appendSyncParams(modInfo, url, mode);
		downloadParties(modInfo, url, mode, true, 1, true, req);
	}
	
	private void downloadParties(ModuleInfo modInfo, String url, int mode, boolean isFirst, int page, boolean includeColHeaders, Request req) throws Exception
	{
		File contactsModDir = modInfo.getModuleDir(mode, dbInfo.getConnectorId());
		File contactsFile = new File(contactsModDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
		
		ModuleInfo orgsModInfo = dbInfo.getCurModules().get("Organisations");
		File orgsModDir = orgsModInfo.getModuleDir(mode, dbInfo.getConnectorId());
		File orgsFile = new File(orgsModDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
		
		BufferedWriter contactsWriter = null;
		BufferedWriter orgsWriter = null;
		CSVPrinter contactsCSVPrinter;
		CSVPrinter orgsCSVPrinter;
		
		int contactsRowCount = 0;
		int contactsTotalRowCount = 0;
		int orgsRowCount = 0;
		int orgsTotalRowCount = 0;
		int iterationLimit = 0;
		
		try
		{
			contactsWriter = new BufferedWriter(new FileWriter(contactsFile));
			contactsCSVPrinter = new CSVPrinter(contactsWriter, CSVFormat.DEFAULT);
			
			orgsWriter = new BufferedWriter(new FileWriter(orgsFile));
			orgsCSVPrinter = new CSVPrinter(orgsWriter, CSVFormat.DEFAULT);
			
			if(includeColHeaders)
			{
				printHeaders(modInfo, contactsCSVPrinter, req);
				printHeaders(orgsModInfo, orgsCSVPrinter, req);
			}
		
			do
			{
				String tempUrl = url + "&page=" + page;
				CloseableHttpClient client  = HttpClients.createDefault();
				HttpGet method = getMethod(tempUrl, accessToken);
				CloseableHttpResponse resp = client.execute(method);
				int respCode = resp.getStatusLine().getStatusCode();
				if(respCode == HttpStatus.SC_UNAUTHORIZED)
				{
					if(!isFirst)
					{
						throw new RuntimeException("Access token may revoked. Unauthorized error occurred");
					}
					isFirst = false;
// if access token expired after 20 pages, we need to write those in file and generate new. Should I close the writer.
//					ConnectorUtil.closeWriter(bw); 
					accessToken = getAccessToken();
					downloadParties(modInfo, url, mode, isFirst, page, false, req);
				}
				Header[] header = resp.getHeaders("X-RateLimit-Remaining");
				if(header[0].getValue().equals(0)) // Per Hour Limit Exceeded 
				{
// This header contains The time at which the current rate limit window resets in UTC epoch seconds.
					int time = Integer.parseInt(resp.getHeaders("X-RateLimit-Reset")[0].getValue()); 
					System.out.println("Rate Limit Exceeded");
					Thread.sleep(time-System.currentTimeMillis());
					downloadParties(modInfo, url, mode, isFirst, page, false, req);
				}
				InputStream is = resp.getEntity().getContent();
				StringWriter writer = new StringWriter();
				IOUtils.copy(is, writer, UTF8);
				System.out.println(writer.toString());
				contactsRowCount = writePartyData(modInfo, writer.toString(), contactsCSVPrinter, req);
				contactsTotalRowCount += contactsRowCount;
				if(contactsTotalRowCount %  200000 == 0)
				{
					ConnectorUtil.closeWriter(contactsWriter);
					contactsFile = new File(contactsModDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
					contactsWriter = new BufferedWriter(new FileWriter(contactsFile));
					contactsCSVPrinter = new CSVPrinter(contactsWriter, CSVFormat.DEFAULT);
					printHeaders(modInfo, contactsCSVPrinter, req);
				}
				orgsRowCount = writePartyData(orgsModInfo, writer.toString(), orgsCSVPrinter, req);
				orgsTotalRowCount += orgsRowCount;
				if(orgsTotalRowCount % 200000 == 0)
				{
					ConnectorUtil.closeWriter(orgsWriter);
					orgsFile = new File(orgsModDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
					orgsWriter = new BufferedWriter(new FileWriter(orgsFile));
					orgsCSVPrinter = new CSVPrinter(orgsWriter, CSVFormat.DEFAULT);
					printHeaders(modInfo, orgsCSVPrinter, req);
				}
				page ++;
			}
			while(orgsRowCount + contactsRowCount == 100 && iterationLimit < 20000); // 100 records per page. for safety purposes allowed 20000 iterations.
		}
		finally
		{
			ConnectorUtil.closeWriter(contactsWriter);
			ConnectorUtil.closeWriter(orgsWriter);
		}
	}
	
	private int writePartyData(ModuleInfo modInfo, String response, CSVPrinter printer, Request req) throws Exception
	{
		int rowCount = 0;
		JSONObject obj = new JSONObject(response);
		JSONArray parties = obj.getJSONArray("parties");
		JSONObject party;
		JSONObject ob;
		JSONArray arr;
		String field;
		
		Iterator<String> fieldsIt = modInfo.getAllFields().keySet().iterator(); 
		
		for(int i = 0; i < parties.length(); i++)
		{
			party = parties.getJSONObject(i);
			printer.print(req.getUserId());
			while(fieldsIt.hasNext())
			{
				field = fieldsIt.next();
				if(field.equals("emailAddresses") || field.equals("websites"))
				{
					arr = party.getJSONArray(field);
					for(int j = 0; j < arr.length(); j++)
					{
						ob = arr.getJSONObject(j);
						if(ob.getString("type").equals("Work") || ob.getString("type").equals("URL"))
						{
							printer.print(ob.getString("address"));
						}
					}
				}
				else
				{
					printer.print(party.getString(field));
				}
			}
			printer.println();
		}
		return rowCount;
	}
	
	private void downloadOpportunities(ModuleInfo modInfo, int mode, Request req) throws Exception
	{
		if(!modInfo.getModMetaName().equals("Contacts"))
		{
			return;
		}
		String url = modInfo.getModAttrValue(MODULE_URL);
		url = appendSyncParams(modInfo, url, mode);
		downloadOpportunities(modInfo, url, mode, true, 1, true, req);
	}
	
	private void downloadOpportunities(ModuleInfo modInfo, String url, int mode, boolean isFirst, int page, boolean includeColHeaders, Request req) throws Exception
	{
		File modDir = modInfo.getModuleDir(mode, dbInfo.getConnectorId());
		File file = new File(modDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
		
		BufferedWriter bw = null;
		CSVPrinter csvPrinter;
		
		int rowCount = 0;
		int totalRowCount = 0;
		int iterationLimit = 0;
		
		try
		{
			bw = new BufferedWriter(new FileWriter(file));
			csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
			
			if(includeColHeaders)
			{
				printHeaders(modInfo, csvPrinter, req);
			}
		
			do
			{
				String tempUrl = url + "&page=" + page;
				CloseableHttpClient client  = HttpClients.createDefault();
				HttpGet method = getMethod(tempUrl, accessToken);
				CloseableHttpResponse resp = client.execute(method);
				int respCode = resp.getStatusLine().getStatusCode();
				if(respCode == HttpStatus.SC_UNAUTHORIZED)
				{
					if(!isFirst)
					{
						throw new RuntimeException("Access token may revoked. Unauthorized error occurred");
					}
					isFirst = false;
// if access token expired after 20 pages, we need to write those in file and generate new. Should I close the writer.
//					ConnectorUtil.closeWriter(bw); 
					accessToken = getAccessToken();
					downloadParties(modInfo, url, mode, isFirst, page, false, req);
				}
				Header[] header = resp.getHeaders("X-RateLimit-Remaining");
				if(header[0].getValue().equals(0)) // Per Hour Limit Exceeded 
				{
// This header contains The time at which the current rate limit window resets in UTC epoch seconds.
					int time = Integer.parseInt(resp.getHeaders("X-RateLimit-Reset")[0].getValue()); 
					System.out.println("Rate Limit Exceeded");
					Thread.sleep(time-System.currentTimeMillis());
					downloadParties(modInfo, url, mode, isFirst, page, false, req);
				}
				InputStream is = resp.getEntity().getContent();
				StringWriter writer = new StringWriter();
				IOUtils.copy(is, writer, UTF8);
				System.out.println(writer.toString());
				rowCount = writeOpportunitiesData(modInfo, writer.toString(), csvPrinter, req);
				totalRowCount += rowCount;
				if(totalRowCount %  200000 == 0)
				{
					ConnectorUtil.closeWriter(bw);
					file = new File(modDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
					bw = new BufferedWriter(new FileWriter(file));
					csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
					printHeaders(modInfo, csvPrinter, req);
				}
				page ++;
			}
			while(rowCount == 100 && iterationLimit < 20000); // 100 records per page. for safety purposes allowed 20000 iterations.
		}
		finally
		{
			ConnectorUtil.closeWriter(bw);
		}
	}
	
	private int writeOpportunitiesData(ModuleInfo modInfo, String response, CSVPrinter printer, Request req) throws Exception
	{
		int i;
		Iterator<String> fieldsIt = modInfo.getAllFields().keySet().iterator();
		JSONObject obj = new JSONObject(response);
		JSONArray opportunities = obj.getJSONArray("opportunities");
		JSONObject opportunity;
		JSONObject subObj;
		String field;
		String partyType;
		
		for(i = 0; i < opportunities.length(); i++)
		{
			opportunity = opportunities.getJSONObject(i);
			printer.print(req.getUserId());
			while(fieldsIt.hasNext())
			{
				field = fieldsIt.next();
				if(field.equals("owner") && opportunity.has(field))
				{
					subObj = opportunity.getJSONObject(field);
					printer.print(subObj.getString("id"));
					printer.print(subObj.getString("name"));
				}
				else if(field.equals("milestone") && opportunity.has(field))
				{
					subObj = opportunity.getJSONObject(field);
					printer.print(subObj.getString("name"));
				}
				else if(field.equals("value") && opportunity.has(field))
				{
					printer.print(opportunity.getString("amount"));
				}
				else if(field.equals("party") && opportunity.has(field))
				{
					subObj = opportunity.getJSONObject(field);
					partyType = subObj.getString("type");
					if(partyType.equals("person")) // need to test with contacts mapped with org
					{
						printer.print(subObj.getString("id")); // contact id
						printer.print(subObj.getString("name")); // contact name
						printer.print(""); // orgid
						printer.print(""); // orgname
					}
					else
					{
						printer.print("");
						printer.print("");
						printer.print(subObj.getString("id"));
						printer.print(subObj.getString("name"));
					}
				}
				else
				{
					printer.print(opportunity.getString(field));
				}
			}
			printer.println();
		}
		
		return i;
	}
	
	private HttpGet getMethod(String url, String accessToken) throws Exception
	{
		HttpGet method = new HttpGet();
		
		method.setURI(new URI(url));
		method.addHeader("Authorization", "Bearer " + accessToken);	//NO I18N
		method.addHeader("Accept", "application/json");	//NO I18N
		return method;
		
	}
	
	private String appendSyncParams(ModuleInfo modInfo, String url, int mode)
	{
		if(mode != SYNC_MODE)
		{
			url = url + "since=" + new SimpleDateFormat(API_DATE_FORMAT).format(modInfo.getLastFetchTime().getTime() - DAY_IN_MILLI);
		}
		return url;
	}
	
	@Override
	public String getImportType(String modId, int mode)
	{
		String impType = "TRUNCATEADD";
		if(mode == SYNC_MODE && !modId.equalsIgnoreCase("Users"))
		{
			impType = "UPDATEADD";
		}
		return impType;
	}

	@Override
	public void insAdditionalInfo(Request req)
	{
		// TODO Auto-generated method stub
		
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
	
	public void handleDeletedRecords(ModuleInfo modInfo, boolean isFirst, Request req) throws Exception
	{
		if(modInfo.getModMetaName().equalsIgnoreCase(USER_MOD_METANAME))
		{
			return;
		}
		List<String> deletedIds = new ArrayList<String>();
		String url = modInfo.getModAttrValue(MODULE_DELETEURL);
		CloseableHttpClient client  = HttpClients.createDefault();
		HttpGet method = getMethod(url, accessToken);
		CloseableHttpResponse resp = client.execute(method);
		int respCode = resp.getStatusLine().getStatusCode();
		if(respCode == HttpStatus.SC_UNAUTHORIZED)
		{
			if(!isFirst)
			{
				throw new RuntimeException("Access token may revoked. Unauthorized error occurred");
			}
			isFirst = false;
//if access token expired after 20 pages, we need to write those in file and generate new. Should I close the writer.
//			ConnectorUtil.closeWriter(bw); 
			accessToken = getAccessToken();
			handleDeletedRecords(modInfo, req);
		}
		Header[] header = resp.getHeaders("X-RateLimit-Remaining");
		if(header[0].getValue().equals(0)) // Per Hour Limit Exceeded 
		{
//This header contains The time at which the current rate limit window resets in UTC epoch seconds.
			int time = Integer.parseInt(resp.getHeaders("X-RateLimit-Reset")[0].getValue()); 
			System.out.println("Rate Limit Exceeded");
			Thread.sleep(time-System.currentTimeMillis());
		}
		InputStream is = resp.getEntity().getContent();
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, UTF8);
		
		JSONObject obj = new JSONObject(writer.toString());
		JSONArray arr = obj.getJSONArray(modInfo.getModAttrValue(MODULE_RESPNAME));
		JSONObject ob;
		for(int i = 0; i < arr.length(); i++)
		{
			ob = arr.getJSONObject(i);
			deletedIds.add(ob.getString("id"));
		}
		String sql = SQLQueryAPI.getSQLString("ConnDelDeletedRows", new Object[][]{{"IDS", deletedIds}, {"COLUMN", "Id"}, {"USERID", req.getUserId()}});
		SQLQueryAPI.executeQuery(sql);
		
	}
	

}
