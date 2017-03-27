package com.lbi.connector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface ConnectorConstants
{
	
//  ##################    IMPORT MODE CONSTANTS     ##################	
	
	public static final int INIT_MODE = 1; 
	
	public static final int SYNC_MODE = 2;
	
	
//	##################    CONNDBINFO TABLE CONSTANTS  ###################
	
	public static final int CONNECTOR_CONNECTORID = 0;
	
	public static final int CONNECTOR_NAME = 1;
	
	public static final int CONNECTOR_CONNECTOR = 2;
	
	public static final int CONNECTOR_PROJID = 3;

	public static final int CONNECTOR_CREATEDTIME = 4;
	
	public static final int CONNECTOR_LASTSYNCTIME = 5;
	
	public static final int CONNECTOR_STATUS = 6;
	
	public static final int CONNECTOR_CREATEDBY = 7;
	
	public static final int CONNECTOR_SCHTIME = 8;
	
	public static final int CONNECTOR_ORGNAME = 9;
	
	public static final int CONNECTOR_EMAIL = 10;
	
	public static final int CONNECTOR_BASEURL = 11;
	
//  ##################     CONNDBINFO STATUS VALUES     ################
	
	public static final Integer CONNECTOR_STATUS_YETTOSTART = 0;
	
	public static final Integer CONNECTOR_STATUS_INPROGRESS = 1;
	
	public static final Integer CONNECTOR_STATUS_INITSUXES = 2;
	
	public static final Integer CONNECTOR_STATUS_INITFAIL = 3;
	
	public static final Integer CONNECTOR_STATUS_SYNCSUXES = 4;
	
	public static final Integer CONNECTOR_STATUS_SYNCFAIL = 5;
	
//  ##################     CONNMODULEINFO TABLE CONSTANTS   #################
	
	public static final int MODULES_ID = 0; 
	
	public static final int MODULES_CONNECTORID = 1;
	
	public static final int MODULES_MODMETANAME = 2;
	
	public static final int MODULES_STATUS = 3;
	
	public static final int MODULES_LASTFETCHTIME = 4;
	
	public static final int MODULES_FETCHEDUPTO = 5;
	
//  ##################     CONNMODINFO STATUS VALUES     ################
	
	public static final Integer MODULES_STATUS_YETTOSTART = 0;
	
	public static final Integer MODULES_STATUS_INPROGRESS = 1;
	
	public static final Integer MODULES_STATUS_INITSUXES = 2;
	
	public static final Integer MODULES_STATUS_INITFAIL = 3;
	
	public static final Integer MODULES_STATUS_PARTIALSUXES = 4;
	
	public static final Integer MODULES_STATUS_SYNCSUXES = 5;
	
	public static final Integer MODULES_STATUS_SYNCFAIL = 6;
	
//  ##################     CONNMODINFO STATUS VALUES     ################
	
	public static final int CONNSELMODULES_CONNECTORID = 0;
	
	public static final int CONNSELMODULES_CONNSELMODID = 1;
	
//  ##################     CONNECTOR AUTH INFO CONSTANTS      ###############	

	public static final int CONNAUTHINFO_USERID = 0;
	
	public static final int CONNAUTHINFO_EMAIL = 1;
	
	public static final int CONNAUTHINFO_CONNECTOR = 2;
	
	public static final int CONNAUTHINFO_AUTHTOKEN = 3;
	
	public static final int CONNAUTHINFO_REFRESHTOKEN = 4;
	
	public static final int CONNAUTHINFO_LASTREFRESHEDTIME = 5;
	
//  ##################     CONNECTOR CLIENT INFO CONSTANTS      ###############	
	
	public static final int CONNCLIENTINFO_CLIENTID = 0;
	
	public static final int CONNCLIENTINFO_CLIENTSECRET = 1;
	
//  ##################     CONNECTOR ID      ###############	
	
	public static final int INSIGHTLY_CONN_ID = 1;
	
	public static final int CAPSULE_CRM_ID = 2;
	
	public static final int HUBSPOT_CRM_ID = 3;
	
	public static final int PD_CRM_ID = 4; // PipeDrive
	
	public static final int FRESHDESK_CONN_ID = 22;
	
	public static final int TEAMSUPPORT_CONN_ID = 23;
	
//  ##################     CONNECTOR GENERAL CONSTANTS      ###############
	
	public static final String CONNECTOR_DETAILS_XML_FILE_PATH = "";

	public static final String CONNECTOR_DIR = "";
	
	public static final long DAY_IN_MILLI = 86400000;
	
//  ##################     CRM CONNECTOR CONSTANTS      ###############
	
	public static final String USER_MOD_METANAME = "Users";
	
	public static final String OPPORTUNITY_MOD_METANAME = "Opportunities";
	
	public static final String CONTACT_MOD_METANAME = "Contacts";
	
	public static final String ORGANISATION_MOD_METANAME = "Organisations";
	
}
