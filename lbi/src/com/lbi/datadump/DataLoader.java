package com.lbi.datadump;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;

import com.lbi.connector.ConnectorXMLConstants;
import com.lbi.connector.ModuleInfo;
import com.lbi.exception.LBIException;
import com.lbi.framework.app.Request;
import com.lbi.metadata.MetaDataErrorCodes;
import com.lbi.metadata.MetaDataUtil;
import com.lbi.sql.SQLQueryAPI;

public class DataLoader implements ConnectorXMLConstants, ImportConstants, ImportErrorCodes, MetaDataErrorCodes
{
	String importType;
	String tableName;
	String dirPath;
	
	public DataLoader(String importType, String tableName, String dirPath) throws Exception
	{
		this.importType = importType;
		this.tableName = tableName;
		this.dirPath = dirPath;
	}
	
	public int loadData(Request req, ModuleInfo modInfo) throws Exception
	{
		int rowCount = 0;
		String tempTableName = null;
		ResultSetMetaData metaData = null;
		try(Connection conn = getConnection(req.getUserId());
			Statement st = conn.createStatement())
		{
			conn.setAutoCommit(false);
//			StringBuilder sql = new StringBuilder();
//			sql.append("SELECT * FROM ").append(orgTableName).append(" LIMIT 0");
//			System.out.println("Constructed select query :: " + sql.toString());
//			
//			
//			ResultSetMetaData metaData = st.executeQuery(sql.toString()).getMetaData();
			
			String sql = SQLQueryAPI.getSQLString("CREATE_TEMP_TBL", new Object[][]{{"ORGTABLENAME", tableName}});
			metaData = SQLQueryAPI.executeAndGetMetaData(sql, req);
			if(metaData.getColumnCount() == 0)
			{
				throw new LBIException(ZERO_COLUMNS_IN_TEMP_TABLE_CREATION, null, tableName, sql);
			}
			
			tempTableName = MetaDataUtil.createTempTable(tableName, req, conn, st, metaData);
			File dir = new File(dirPath);
			if(!dir.isDirectory() || !dir.exists())
			{
				throw new LBIException(WRONG_PATH_IN_MODULE_IMPORT, null, dirPath);
			}
			
			String[] fileList = dir.list();
			String loadSql;
			for(int i = 0; i < fileList.length; i++)
			{
				loadSql = "LOAD DATA LOCAL INFILE " + fileList[i] + " INTO " + tempTableName + " FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' IGNORE LINES 1";
				rowCount += st.executeUpdate(loadSql);
			}
			
			if(importType.equals(IMPORT_TYPE_DELETEADD))
			{
				deleteAdd(tempTableName, conn, st, metaData);
			}
			else if(importType.equals(IMPORT_TYPE_UPDATEADD))
			{
				updateAdd(tempTableName, conn, st, metaData);
			}
			else if(importType.equals(IMPORT_TYPE_ADD))
			{
				add(tempTableName, conn, st, metaData);
			}
			conn.commit();
			conn.setAutoCommit(true);
		}
		return rowCount;
	}
	
	private void add(String tempTableName, Connection conn, Statement st, ResultSetMetaData orgTblMetaData) throws Exception
	{
		StringBuilder insertQuery = new StringBuilder();
		
		insertQuery.append("Insert into ").append(tableName);
//		.append(" ( ");
//		
//		
//		for(int i = 1; i <= orgTblMetaData.getColumnCount(); i++)
//		{
//			insertQuery.append(orgTblMetaData.getColumnName(i)).append(" ")
//			.append(orgTblMetaData.getColumnTypeName(i)).append( "(").append(orgTblMetaData.getColumnDisplaySize(i)).append("), ");
//		}
//		insertQuery.deleteCharAt(insertQuery.lastIndexOf(","));
		insertQuery.append(" ) select * from ").append(tempTableName);
		System.out.println("Insert query into OG table :: " + insertQuery.toString());
		st.executeUpdate(insertQuery.toString());
	}
	
	private void deleteAdd(String tempTableName, Connection conn, Statement st, ResultSetMetaData orgTblMetaData) throws Exception
	{
		String sql = "Delete " + tempTableName + " where CONNECTORID = " + null + " USERID = " + null;
		System.out.println("Delete Query :: " + sql);
		st.executeUpdate(sql);
		System.out.println("Old Data deleted");
		
		add(tempTableName, conn, st, orgTblMetaData);
	}
	
	private void updateAdd(String tempTableName, Connection conn, Statement st, ResultSetMetaData orgTblMetaData) throws Exception
	{
		String colName;
		StringBuilder sql = new StringBuilder();
		StringBuilder insertSql = new StringBuilder();
		StringBuilder updateSql = new StringBuilder();
		StringBuilder selectSql = new StringBuilder();
		
		insertSql.append("INSERT INTO ").append(tableName).append(" (");
		updateSql.append(" UPDATE ");
		selectSql.append(" SELECT ");
		for(int i = 1; i <= orgTblMetaData.getColumnCount(); i++)
		{
			colName = orgTblMetaData.getColumnName(i);
			insertSql.append(colName).append(", ");
			selectSql.append("t.").append(colName).append(", ");
			updateSql.append(colName).append(" = t.").append(colName).append(", ");
		}
		insertSql.deleteCharAt(insertSql.lastIndexOf(","));
		selectSql.deleteCharAt(selectSql.lastIndexOf(","));
		updateSql.deleteCharAt(updateSql.lastIndexOf(","));
		selectSql.append(" FROM ").append(tempTableName).append(" AS t ").append(" ON DUPLICATE KEY ");
		insertSql.append(")");
		sql.append(insertSql).append(selectSql).append(updateSql);
		
		st.executeUpdate(sql.toString());
	}
	
	private Connection getConnection(Long userId) throws Exception
	{
		String url = "jdbc:mysql://localhost:3306";
		Connection conn = DriverManager.getConnection(url, "root", "lbi");
		return conn;
	}
	
}
