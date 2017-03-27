package com.lbi.connector.insightly;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Iterator;
import java.util.Base64;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.lbi.general.GeneralConstants;
import com.lbi.framework.app.Request;

import com.lbi.connector.*;

public class InsightlyConnector extends Connector implements GeneralConstants, ConnectorConstants, ConnectorXMLConstants
{
	public InsightlyConnector() throws Exception
	{
		super(INSIGHTLY_CONN_ID);
	}
	
	public InsightlyConnector(long connectorId) throws Exception
	{
		super(INSIGHTLY_CONN_ID);
	}
	
	public void downloadModData(ModuleInfo modInfo, int mode, Request req) throws Exception
	{
		//6ee0a801-b27b-4641-bbba-66be9c180a96:
		String url = modInfo.getModAttrValue(MODULE_URL);
		int pageLimit = Integer.parseInt(modInfo.getModAttrValue(MODULE_PERPAGE));
		File modDir = modInfo.getModuleDir(mode, dbInfo.getConnectorId());
		File file = new File(modDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
		BufferedWriter bw = null;
		CSVPrinter csvPrinter;
		
		CloseableHttpClient client  = HttpClients.createDefault();
		HttpGet method = new HttpGet();
		
		int skip = 0;
		int respCode;
		boolean limitXceeded = false;
		int rowCount = 0;
		int totalCount;
		int iterationLimit = 0;
		
		try
		{
			bw = new BufferedWriter(new FileWriter(file));
			csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
		
			do
			{
				url = url + "&skip=" + skip + "&top=" + pageLimit;
				method.setURI(new URI(url));
				method.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(authInfo.getToken().getBytes()));
				CloseableHttpResponse resp = client.execute(method);
				respCode = resp.getStatusLine().getStatusCode();
				Header[] header = resp.getHeaders("x-total-count");
				totalCount = Integer.parseInt(header[0].getValue());
				if(respCode == 429) // Limit Exceeded. Either day limit or req / sec
				{
					if(limitXceeded)
					{
						throw new Exception("Limit Exceeded");
					}
					Thread.sleep(3000);
				}
				InputStream is = resp.getEntity().getContent();
				StringWriter writer = new StringWriter();
				IOUtils.copy(is, writer, UTF8);
				System.out.println(writer.toString());
				rowCount += writeModData(writer.toString(), modInfo, mode, csvPrinter);
				skip = skip + pageLimit;
				iterationLimit ++;
				
				if(rowCount == 200000)
				{
					closeWriter(bw);
					file = new File(modDir + File.separator + System.currentTimeMillis() + new Random().nextInt() + ".csv");
				}
			}
			while(rowCount < totalCount && iterationLimit < 200000);
		}
		finally
		{
			closeWriter(bw);
		}

	}
	
	public int writeModData(String resp, ModuleInfo modInfo, int mode, CSVPrinter w) throws Exception
	{
		int rowCount = 0;
		String modId = modInfo.getModMetaName();
		if(modId.equals("Contacts"))
		{
			rowCount = InsightlyUtil.writeContacts(resp, w);
		}
		else if(modId.equals("Leads"))
		{
			rowCount = InsightlyUtil.writeLeads(resp, w);
		}
		else if(modId.equals("LeadSources"))
		{
			
		}
		else if(modId.equals("LeadStatuses"))
		{
			
		}
		else if(modId.equals(""))
		{
			
		}
		return rowCount;
	}
	
//	public static void main(String args[]) throws Exception
//	{
//		InsightlyConnector conn = new InsightlyConnector();
//		conn.download();
//		
//		System.out.println(System.getProperty("java.runtime.version"));
//	}

	@Override
	public void insAdditionalInfo(Request req)
	{
		// TODO Auto-generated method stub
		
	}
	
	public void closeWriter(Writer w) throws Exception
	{
		if(w != null)
		{
			try{ w.close(); } catch(IOException e) { throw new Exception("IOException occurred while writing data"); }
		}
	}

	@Override
	public String getImportType(String modId, int mode)
	{
		return "TRUNCATEADD";
	}
}
