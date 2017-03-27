package com.lbi.connector.pipedrive;

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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.lbi.connector.Connector;
import com.lbi.connector.ConnectorUtil;
import com.lbi.connector.ModuleInfo;
import com.lbi.framework.app.Request;
import com.lbi.sql.SQLQueryAPI;

public class PDConnector extends Connector
{

	public PDConnector() throws Exception
	{
		super(PD_CRM_ID);
	}
	
	public PDConnector(Long connectorId) throws Exception
	{
		super(connectorId, PD_CRM_ID);
	}

	@Override
	public String getImportType(String modId, int mode)
	{
		return IMPORT_TYPE_DELETEADD;
	}

	@Override
	public void insAdditionalInfo(Request req) throws Exception
	{
		// TODO Auto-generated method stub
		String sql = SQLQueryAPI.getSQLString("InsertFD", new Object[][]{{"CONNECTORID", null}, {"OPRGID"}, null});
		SQLQueryAPI.executeQuery(sql);
	}

	@Override
	public void downloadModData(ModuleInfo modInfo, int mode, Request req) throws Exception
	{
		String url = modInfo.getModAttrValue(MODULE_URL);
		url.replace(BASEURL_REPLACEMENT_VALUE, dbInfo.getBaseUrl());
		downloadData(url + "&api_token=" + authInfo.getToken(), modInfo, mode, req, 0, true);
	}
	
	private void downloadData(String url, ModuleInfo modInfo, int mode, Request req, int offSet, boolean includeHeaders) throws Exception
	{
		File modDir = modInfo.getModuleDir(mode, dbInfo.getConnectorId());
		File file = new File(modDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
		
		BufferedWriter bw = null;
		CSVPrinter csvPrinter;
		
		StringBuilder tempUrl = new StringBuilder(url);
		
		int rowCount = 0;
		int totalRowCount = 0;
		int iterationLimit = 0;
		
		try
		{
			bw = new BufferedWriter(new FileWriter(file));
			csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
			
			if(includeHeaders)
			{
				printHeaders(modInfo, csvPrinter, req);
			}
		
			do
			{
				tempUrl.append("&").append(modInfo.getModAttrValue(MODULE_OFFSET)).append(offSet);
				CloseableHttpClient client  = HttpClients.createDefault();
				HttpGet method = new HttpGet();
				method.setURI(new URI(tempUrl.toString()));
				CloseableHttpResponse resp = client.execute(method);
				int respCode = resp.getStatusLine().getStatusCode();
				if(respCode == 429) // Limit Exceeded 
				{// This header contains The time at which the current rate limit window resets in UTC epoch seconds.
					int time = Integer.parseInt(resp.getHeaders("Retry-After")[0].getValue()); 
					System.out.println("Rate Limit Exceeded");
					Thread.sleep(time);
					downloadData(url, modInfo, mode, req, offSet, false);
				}
				rowCount = writeResponse(resp, modInfo, mode, req, csvPrinter);
				totalRowCount = totalRowCount + rowCount;
				
				if(totalRowCount % 200000 == 0)
				{
					ConnectorUtil.closeWriter(bw);
					file = new File(modDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
					bw = new BufferedWriter(new FileWriter(file));
					csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
					printHeaders(modInfo, csvPrinter, req);
				}
				
			}while(iterationLimit < 20000);
		}
		finally
		{
			ConnectorUtil.closeWriter(bw);
		}
	}
	
	private int writeResponse(CloseableHttpResponse resp, ModuleInfo modInfo, int mode, Request req, CSVPrinter printer) throws Exception
	{
		InputStream is = resp.getEntity().getContent();
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, UTF8);
		System.out.println(writer.toString());
		
		Iterator<String> it = modInfo.getAllFields().keySet().iterator();
		JSONObject respObj = new JSONObject(writer.toString());
		
		if(!respObj.has("success"))
		{
			throw new Exception("Exception occurred while downloading data. Error :: " + respObj.toString());
		}
		
		JSONArray arr = respObj.getJSONArray(modInfo.getModAttrValue(MODULE_RESPNAME));
		JSONObject obj;
		JSONObject subObj;
		String field;
		String val = "";
		NamedNodeMap map;
		String[] subFields;
		int i;
		for(i = 0; i < arr.length(); i++)
		{
			printer.print(req.getUserId());
			obj = arr.getJSONObject(i);
			while(it.hasNext())
			{
				val = "";
				field = it.next();
				map = modInfo.getAllFields().get(field);
				if(!isAttrPresent(map, "skip")) // sub fields are need to be skipped. It will be writen in sub object
				{
					if(!obj.has(field))
					{
						printer.print(val);
					}
					else if(isAttrPresent(map, "object"))
					{
						subObj = obj.getJSONObject(field);
						subFields = map.getNamedItem("fields").getNodeValue().toString().split(","); // sub fields given in connectordetails.xml
						for(String subField : subFields)
						{
							printer.print(subObj.getString(subField));
						}
					}
					else
					{
						val = obj.getString(field);
						printer.print(val);
					}
				}
			}
			printer.println();
		}
		
		return i+1;
	}
	
	private boolean isAttrPresent(NamedNodeMap map, String field)
	{
		Node node = map.getNamedItem(field);
		return node == null ? false : true;
	}
}
