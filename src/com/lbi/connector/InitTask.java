package com.lbi.connector;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import com.lbi.framework.app.Request;

public class InitTask implements Job
{
	public void execute(JobExecutionContext context)
	{
		JobDetail detail = context.getJobDetail();
		
		Long connectorId = (Long) detail.getJobDataMap().get("CONNECTORID");
		Request req = null;
		
		try
		{
			Connector conn = ConnectorFactory.getConnector(connectorId);
			conn.doInit(req);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception occurred in Initial Fetch. Connector Id :: " + connectorId);
		}
		
	}
}
