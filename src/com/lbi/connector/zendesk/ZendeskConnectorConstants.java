package com.lbi.connector.zendesk;

import java.util.HashMap;

public interface ZendeskConnectorConstants
{
	
//  ##################     ZENDESK CONNECTOR CONSTANTS      ###############	

//  ##################     ZENDESK ADDITIONAL INFO CONSTANTS      ###############
	
//  ##################     ZENDESK CONN MODULE CONSTANTS      ###############

	public static final HashMap<String, String> TICKET = new HashMap<String, String>()
	{
		private static final long serialVersionUID = 1L;

		{ put("via", "channel"); put("group_stations", ""); put("assignee_stations", ""); put("reopens", ""); put("replies", ""); put("status_updated_at", ""); put("initially_assigned_at", ""); put("solved_at", ""); put("reply_time_in_minutes", "business"); put("first_resolution_time_in_minutes", "business"); put("full_resolution_time_in_minutes", "business"); put("agent_wait_time_in_minutes", "business"); put("requester_wait_time_in_minutes", "business"); put("group_stations", "business"); put("on_hold_time_in_minutes", "business");}
	};

}