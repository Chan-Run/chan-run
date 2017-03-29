package com.lbi.metadata;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import com.lbi.exception.LBIException;
import com.lbi.framework.app.Request;

public class MetaDataUtil implements MetaDataErrorCodes
{
	
	public static String createTempTable(String orgTableName, Request req, Connection conn, Statement st, ResultSetMetaData metaData) throws Exception
	{
		String tempTableName = "temp_" + orgTableName;
		StringBuilder tempTableQuery = new StringBuilder();
			
		if(metaData.getColumnCount() == 0)
		{
			throw new LBIException(ZERO_COLUMNS_IN_TEMP_TABLE_CREATION, null, orgTableName);
		}
		tempTableQuery.append("CREATE TABLE ").append(tempTableName).append(" ( ");
		
		for (int i = 1; i <= metaData.getColumnCount(); i++)
		{
			tempTableQuery.append(metaData.getColumnName(i)).append(" ")
			.append(metaData.getColumnTypeName(i))
			.append(" (").append(metaData.getColumnDisplaySize(i)).append(") ")
			.append(", ");
		}
		tempTableQuery.deleteCharAt(tempTableQuery.lastIndexOf(","));
		tempTableQuery.append(" )");
		st.executeUpdate(tempTableQuery.toString());
		
		return tempTableName;
	}

}
