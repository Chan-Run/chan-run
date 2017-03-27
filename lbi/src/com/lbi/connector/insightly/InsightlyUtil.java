package com.lbi.connector.insightly;

import org.apache.commons.csv.CSVPrinter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InsightlyUtil
{
	public static int writeContacts(String resp, CSVPrinter writer) throws Exception
	{
		int i = 0;
		
		try
		{
			JSONArray arr = new JSONArray(resp);
			JSONArray contactInfo;
			JSONObject obj;
			JSONObject contactObj;
			
			for(i = 0; i < arr.length(); i++)
			{
				obj = arr.getJSONObject(i);
				writer.print(obj.getString("CONTACT_ID"));
				writer.print(obj.getString("FIRST_NAME"));
				writer.print(obj.getString("LAST_NAME"));
				writer.print(obj.getString("OWNER_USER_ID"));
				writer.print(obj.getString("DEFAULT_LINKED_ORGANISATION"));
				writer.print(obj.getString("DATE_CREATED_UTC"));
				writer.print(obj.getString("DATE_UPDATED_UTC"));
				writer.print(obj.getString("SOCIAL_LINKEDIN"));
				writer.print(obj.getString("SOCIAL_FACEBOOK"));
				writer.print(obj.getString("SOCIAL_TWITTER"));
				writer.print(obj.getString("OPPORTUNITY_ID"));
				writer.print(obj.getString("ORGANISATION_ID"));
				contactInfo = obj.getJSONArray("CONTACTINFOS");
				for(int j = 0; j < contactInfo.length(); j++)
				{
					contactObj = contactInfo.getJSONObject(j);
					if(contactObj.getString("TYPE").equals("EMAIL"))
					{
						writer.print(contactObj.getString("DETAIL"));
					}
				}
			}
		}
		catch(JSONException je)
		{
			throw new Exception("Data download failed");
		}
		
		return i;
	}
	
	public static int writeLeads(String resp, CSVPrinter writer) throws Exception
	{
		int i = 0;
		
		try
		{
			JSONArray arr = new JSONArray(resp);
			JSONArray contactInfo;
			JSONObject obj;
			JSONObject contactObj;
			
			for(i = 0; i < arr.length(); i++)
			{
				obj = arr.getJSONObject(i);
				writer.print(obj.getString("CONTACT_ID"));
				writer.print(obj.getString("FIRST_NAME"));
				writer.print(obj.getString("LAST_NAME"));
				writer.print(obj.getString("DEFAULT_LINKED_ORGANISATION"));
				writer.print(obj.getString("DATE_CREATED_UTC"));
				writer.print(obj.getString("DATE_UPDATED_UTC"));
				writer.print(obj.getString("SOCIAL_LINKEDIN"));
				writer.print(obj.getString("SOCIAL_FACEBOOK"));
				writer.print(obj.getString("SOCIAL_TWITTER"));
				contactInfo = obj.getJSONArray("CONTACTINFOS");
				for(int j = 0; j < contactInfo.length(); j++)
				{
					contactObj = contactInfo.getJSONObject(j);
					if(contactObj.getString("TYPE").equals("EMAIL"))
					{
						writer.print(contactObj.getString("DETAIL"));
					}
				}
			}
		}
		catch(JSONException je)
		{
			throw new Exception("Data download failed");
		}
		return i;
	}
}
