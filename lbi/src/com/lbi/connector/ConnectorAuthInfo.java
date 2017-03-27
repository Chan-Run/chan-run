package com.lbi.connector;

import java.sql.Timestamp;

import com.lbi.sql.SQLQueryAPI;

public class ConnectorAuthInfo implements ConnectorConstants
{
	private String token;
	private String refreshToken;
	private Timestamp lastRefreshedTime;
	
	public ConnectorAuthInfo(int connectorId, Long userId, String email) throws Exception
	{
		String sql = SQLQueryAPI.getSQLString("GetConnAuthInfo", new Object[][]{{"CONNECTORID", connectorId}, {"USERID", userId}, {"EMAIL", email}});
		Object[] arr = (Object[]) SQLQueryAPI.executeQuery(sql);
		
		token = (String) arr[CONNAUTHINFO_AUTHTOKEN]; // access token or api token
		refreshToken = (String) arr[CONNAUTHINFO_REFRESHTOKEN];
		lastRefreshedTime = (Timestamp) arr[CONNAUTHINFO_LASTREFRESHEDTIME];
		
	}
	
	public String getToken()
	{
		return this.token;
	}
	
	public String getRefreshToken()
	{
		return this.refreshToken;
	}
	
	public Timestamp getLastRefreshedTime()
	{
		return this.lastRefreshedTime;
	}	
}
