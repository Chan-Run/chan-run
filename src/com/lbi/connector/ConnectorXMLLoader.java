package com.lbi.connector;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;



public class ConnectorXMLLoader
{
  
	private File xmlFile;
	Document doc;
	XPath xpath;
	String connector;
	public long fetchTime;
	  
	public ConnectorXMLLoader(String filePath)
	{
		try
		{        
			xmlFile = new File(filePath);
			DocumentBuilderFactory dom = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dom.newDocumentBuilder();
			this.doc = docBuilder.parse(xmlFile);
			XPathFactory xpathFactory = XPathFactory.newInstance();
			this.xpath = xpathFactory.newXPath();
			this.fetchTime = System.currentTimeMillis();
		}
		catch(Exception e)
	    {
			System.out.println("Default Initialization Failed ::: "+e.getMessage());
	    }
	}
	  
	  
	public ConnectorXMLLoader setConnector(String connector)
	{
		this.connector = connector;
		return this;
	}
	  
	public String constructXPathExprForModules()
	{
		return "connectors/" + connector + "/modules/module/@name"; //NO I18N
	}
	  
	public String constructXPathExprForListOfModules()
	{
		return "connectors/" + connector + "/modules/module"; //NO I18N
	}
	
	public String constructXPathExprForFields(String module)
	{
		return "connectors/" + connector + "/modules/module[@name='"+module+"']/fields/field";        //NO I18N
	}
	
//	public String constructXPathExprForListOfViews()
//    {
//        return "defaults/"+ connector +"/views/view"; //NO I18N
//    }
    
	  
	public ArrayList<String> getModules() throws Exception
	{
		String expr = constructXPathExprForModules();
    	return evaluateExpr(expr);        
	}
	  
	public ArrayList<String> getFields(String moduleName) throws Exception
	{
		String expr = constructXPathExprForFields(moduleName);
	    return evaluateExpr(expr);        
	}
	  
	public Map<String, ArrayList<String>> getModVSFieldNames() throws Exception
	{
		Map<String, ArrayList<String>> modMap = new HashMap<String, ArrayList<String>>();
	    ArrayList<String> modules =  getModules();        
	    for (Iterator<String> iterator = modules.iterator(); iterator.hasNext();)
	    {
	    	String mod = (String) iterator.next();
	        modMap.put(mod, getFields(mod));
	    }
	    return modMap;
	}
	  
	public ArrayList<String> evaluateExpr(String expr) throws Exception
	{
		ArrayList<String> fields = new ArrayList<String>();
	    XPathExpression xpathExpr = xpath.compile(expr);
	    NodeList nodeSet = (NodeList) xpathExpr.evaluate(doc, XPathConstants.NODESET);
	    for(int i = 0; i < nodeSet.getLength(); i++)
	    {
	    	fields.add(nodeSet.item(i).getTextContent().trim());
	    }        
	    return fields;
	}
	  
	public HashMap<String, LinkedHashMap<String, NamedNodeMap>> getModIdVsFieldDetsmap() throws Exception
	{
		HashMap<String, LinkedHashMap<String, NamedNodeMap>> modIdVdFieldDetsMap = new HashMap<String, LinkedHashMap<String, NamedNodeMap>>();
	    XPathExpression xpathExprMod = xpath.compile(constructXPathExprForListOfModules());
	    NodeList moduleSet = (NodeList) xpathExprMod.evaluate(doc, XPathConstants.NODESET);
	    for(int i = 0; i < moduleSet.getLength(); i++)
	    {
	    	LinkedHashMap<String, NamedNodeMap> fieldDets = new LinkedHashMap<String, NamedNodeMap>();
	        Node node = moduleSet.item(i);
	        Element module = (Element) node;
	        String modName = module.getAttribute("name");
	        XPathExpression xpathExprField = xpath.compile(constructXPathExprForFields(modName));
	        NodeList fieldSet = (NodeList) xpathExprField.evaluate(doc, XPathConstants.NODESET);
	        for(int j = 0; j < fieldSet.getLength(); j++)
	        {
	        	Node nodeField = fieldSet.item(j);
	            Element field = (Element) nodeField;
	            NamedNodeMap map = field.getAttributes();
	            String fieldKey = field.getTextContent().trim();
	            fieldDets.put(fieldKey, map);
	        }
	        modIdVdFieldDetsMap.put(modName, fieldDets);
	    }
	    return modIdVdFieldDetsMap;
	}
	  
//	public HashMap<String, ArrayList<String>> getMatchColsMap() throws Exception
//	{
//		HashMap<String, ArrayList<String>> modIdVdFieldDetsMap = new HashMap<String, ArrayList<String>>();
//	    XPathExpression xpathExprMod = xpath.compile(constructXPathExprForListOfModules());
//	    NodeList moduleSet = (NodeList) xpathExprMod.evaluate(doc, XPathConstants.NODESET);
//	    for(int i = 0; i < moduleSet.getLength(); i++)
//	    {
//	    	ArrayList<String> matchColList = new ArrayList<String>();
//	    	Node node = moduleSet.item(i);
//	        Element module = (Element) node;
//	        String modName = module.getAttribute("name");
//	        XPathExpression xpathExprField = xpath.compile(constructXPathExprForFields(modName));
//	        NodeList fieldSet = (NodeList) xpathExprField.evaluate(doc, XPathConstants.NODESET);
//	        for(int j = 0; j < fieldSet.getLength(); j++)
//	        {
//	        	Node nodeField = fieldSet.item(j);
//	        	Element field = (Element) nodeField;
//	            NamedNodeMap attributes = field.getAttributes();
//	            Node matchColNode = attributes.getNamedItem("matchcol"); // No I18N
//	            if(matchColNode != null && matchColNode.getNodeValue().equals("true"))
//	            {
//	            	matchColList.add(attributes.getNamedItem("displayName").getNodeValue());
//	            }
//	        }
//	        modIdVdFieldDetsMap.put(modName, matchColList);
//	    }
//	    return modIdVdFieldDetsMap;
//	}
//	  
	public LinkedHashMap<String, String> getModIdVsTableDispName() throws Exception
	{
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
	    XPathExpression xpathExprMod = xpath.compile(constructXPathExprForListOfModules());
	    NodeList moduleSet = (NodeList) xpathExprMod.evaluate(doc, XPathConstants.NODESET);
	    for(int i = 0; i < moduleSet.getLength(); i++)
	    {
	    	Node node = moduleSet.item(i);
	        Element module = (Element) node;
	        String modId = module.getAttribute("name");
	        String modDispName = module.getAttribute("displayName");
	        map.put(modId, modDispName);
	    }
	    return map;
	}
	  
	public LinkedHashMap<String, Element> getModIdVsDetails() throws Exception
	{
		LinkedHashMap<String, Element> map = new LinkedHashMap<String, Element>();
	    XPathExpression xpathExprMod = xpath.compile(constructXPathExprForListOfModules());
	    NodeList moduleSet = (NodeList) xpathExprMod.evaluate(doc, XPathConstants.NODESET);
	    for(int i = 0; i < moduleSet.getLength(); i++)
	    {
	    	Node node = moduleSet.item(i);
	        Element module = (Element) node;
	        String modId = module.getAttribute("name");
	        map.put(modId, module);
	    }
	    return map;
	}
	
//	public LinkedHashMap<String, Element> getCombinedViewDetails() throws Exception
//	{
//		LinkedHashMap<String, Element> map = new LinkedHashMap<>();
//	    XPathExpression xpathExprMod = xpath.compile(constructXPathExprForListOfViews());
//	    NodeList moduleSet = (NodeList) xpathExprMod.evaluate(doc, XPathConstants.NODESET);
//	    for(int i = 0; i < moduleSet.getLength(); i++)
//	    {
//	    	Node node = moduleSet.item(i);
//	        Element module = (Element) node;
//	        String modId = module.getTextContent();
//	        map.put(modId, module);
//	    }
//	    return map;
//	}
	  
}
