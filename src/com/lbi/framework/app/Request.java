package com.lbi.framework.app;

import javax.servlet.http.HttpServletRequest;

public class Request  
{
	private HttpServletRequest request;
	
	public Request(HttpServletRequest request)
	{
		this.request = request;
	}
	
	public HttpServletRequest getHttpRequest()
	{
		return this.request;
	}
	
	public Long getLongParam(String param) throws Exception
	{
		return getLongParam(param, false);
	}
	
	public Long getLongParam(String param, boolean throwIfNull) throws Exception
	{
		String obj = request.getParameter(param);
		
		if(obj == null && throwIfNull)
		{
			throw new Exception("Parameter missing");
		}
		
		if(obj == null)
		{
			return null;
		}
		
		Long longValue;
		
		try
		{
			longValue = Long.parseLong(obj);
		}
		catch(NumberFormatException nfe)
		{
			throw new Exception("Not a long value");
		}
			
		return longValue;
		
	}
	
	public Integer getIntParam(String param) throws Exception
	{
		return getIntParam(param, false);
	}
	
	public Integer getIntParam(String param, boolean throwIfNull) throws Exception
	{
		String obj = request.getParameter(param);
		
		if(obj == null && throwIfNull)
		{
			throw new Exception("Parameter missing");
		}
		
		if(obj == null)
		{
			return null;
		}
		
		Integer value;
		
		try
		{
			value = Integer.parseInt(obj);
		}
		catch(NumberFormatException nfe)
		{
			throw new Exception("Not a long value");
		}
			
		return value;
		
	}
	
	public Double getDoubleParam(String param) throws Exception
	{
		return getDoubleParam(param, false);
	}
	
	public Double getDoubleParam(String param, boolean throwIfNull) throws Exception
	{
		String obj = request.getParameter(param);
		
		if(obj == null && throwIfNull)
		{
			throw new Exception("Parameter missing");
		}
		
		if(obj == null)
		{
			return null;
		}
		
		Double value;
		
		try
		{
			value = Double.parseDouble(obj);
		}
		catch(NumberFormatException nfe)
		{
			throw new Exception("Not a long value");
		}
			
		return value;
		
	}
	
	public Float getFloatParam(String param) throws Exception
	{
		return getFloatParam(param, false);
	}
	
	public Float getFloatParam(String param, boolean throwIfNull) throws Exception
	{
		String obj = request.getParameter(param);
		
		if(obj == null && throwIfNull)
		{
			throw new Exception("Parameter missing");
		}
		
		if(obj == null)
		{
			return null;
		}
		
		Float value;
		
		try
		{
			value = Float.parseFloat(obj);
		}
		catch(NumberFormatException nfe)
		{
			throw new Exception("Not a long value");
		}
			
		return value;
		
	}
	
	public String getParam(String param) throws Exception
	{
		return getParam(param, false);
	}
	
	public String getParam(String param, boolean throwIfNull) throws Exception
	{
		String obj = request.getParameter(param);
		
		if(obj == null && throwIfNull)
		{
			throw new Exception("Parameter missing");
		}
		
		return obj;
	}
	
	public Boolean getBooleanParam(String param) throws Exception
	{
		return getBooleanParam(param, false);
	}
	
	public Boolean getBooleanParam(String param, boolean throwIfNull) throws Exception
	{
		String obj = request.getParameter(param);
		
		if(obj == null && throwIfNull)
		{
			throw new Exception("Parameter missing");
		}
		
		if(obj == null)
		{
			return null;
		}
		
		Boolean value;
		
		try
		{
			value = Boolean.parseBoolean(obj);
		}
		catch(NumberFormatException nfe)
		{
			throw new Exception("Not a long value");
		}
			
		return value;
	}
	
	public Long[] getLongParamValues(String param) throws Exception
	{	
		return getLongParamValues(param, false);
	}
	
	public Long[] getLongParamValues(String param, boolean throwIfNull) throws Exception
	{
		String[] objArr = request.getParameterValues(param);
		
		if(objArr == null && throwIfNull)
		{
			throw new Exception("Parameter missing");
		}
		
		if(objArr == null)
		{
			return null;
		}
		
		Long[] longArr = new Long[objArr.length];
		
		try
		{
			int i = 0;
			for(String value : objArr)
			{
				if(value == null)
				{
					longArr[i++] = null;
					continue;
				}
				longArr[i++ ] = Long.parseLong(value);
			}
			
		}
		catch(NumberFormatException nfe)
		{
			throw new Exception("Not a long value");
		}
			
		return null;
	}
	
	public String getOptionalParam(String param) throws Exception
	{
		return request.getParameter(param);
	}
	
	public String[] getParamValues(String param) throws Exception
	{
		return getParamValues(param, false);
	}
	
	public String[] getParamValues(String param, boolean throwIfNull) throws Exception
	{
		String obj[] = request.getParameterValues(param);
		
		if(param == null && throwIfNull)
		{
			throw new Exception("Param is not present in Request");
		}
		
		return obj;
	}
	
	public Long getUserId()
	{
		return Long.MIN_VALUE;
	}
	
}
