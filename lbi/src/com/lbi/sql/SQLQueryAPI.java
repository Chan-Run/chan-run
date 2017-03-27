package com.lbi.sql;

import java.sql.ResultSetMetaData;
import java.util.List;

import com.lbi.framework.app.Request;

public class SQLQueryAPI
{
	public static Object executeQuery(String queryName, Object[][] values)
	{
		return null;
	}
	
	public static Object executeQuery(String query) throws Exception
	{
		return null;
	}
	
	public static boolean executeUpdate(String sql)
	{
		return true;
	}
	
	public static String getSQLString(String sqlName, Object[][] values) throws Exception
	{
		return "";
	}
	
	public static Object[] getResultAsObjArray(String sql) throws Exception
	{
		return new Object[5];
	}
	
	public static List<?> getAsList(String sql) throws Exception
	{
		return null;
	}
	
	public static Object getResult(String sql) throws Exception
	{
		return null;
	}
	
	public static ResultSetMetaData executeAndGetMetaData(String sql, Request req) throws Exception
	{
		return null;
	}

}
