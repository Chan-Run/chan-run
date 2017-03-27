package com.lbi.connector;

import com.lbi.sql.SQLQueryAPI;

public class ConnectorClientInfo implements ConnectorConstants
{

	String clientId;
	String clientSecret;
	int connector;
	
	ConnectorClientInfo(int connector) throws Exception
	{
		this.connector = connector;
		loadClientInfo();
	}
	
	private void loadClientInfo() throws Exception
	{
		String sql = SQLQueryAPI.getSQLString("GetConnClientInfo", new Object[][]{{"CONNECTOR", connector}});
		Object[] result = SQLQueryAPI.getResultAsObjArray(sql);
		clientId = (String) result[CONNCLIENTINFO_CLIENTID];
		clientSecret = (String) result[CONNCLIENTINFO_CLIENTSECRET];
	}
	
	public String getClientId()
	{
		return this.clientId;
	}
	
	public String getClientSecret()
	{
		return this.clientSecret;
	}


}
