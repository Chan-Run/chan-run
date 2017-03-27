package com.lbi.connector.freshdesk;

import java.util.HashMap;

public interface FreshdeskConnectorConstants
{
	
//  ##################     FRESHDESK CONNECTOR CONSTANTS      ###############
	
	public static final HashMap<Integer, String> source = new HashMap<Integer, String>()
	{
		private static final long serialVersionUID = 1L;

		{ put(1, "Email"); put(2, "Portal"); put(3, "Phone"); put(7, "Chat"); put(8, "Mobihelp"); put(9, "Feedback Widget"); put(10, "Outbound Email"); }
	};
	public static final HashMap<Integer, String> status = new HashMap<Integer, String>()
	{
		private static final long serialVersionUID = 1L;

		{ put(2, "Open"); put(3, "Pending"); put(4, "Resolved"); put(5, "Closed"); }
	};
	public static final HashMap<Integer, String> priority = new HashMap<Integer, String>()
	{
		private static final long serialVersionUID = 1L;

		{ put(1, "Low"); put(2, "Medium"); put(3, "High"); put(4, "Urgent"); }
	};
	public static final HashMap<Integer, String> scope = new HashMap<Integer, String>()
	{
		private static final long serialVersionUID = 1L;
		
		{ put(1, "Global Access"); put(2, "Group Access"); put(3, "Restricted Access"); }
	};	

//  ##################     FRESHDESK ADDITIONAL INFO CONSTANTS      ###############

}