package com.lbi.general;

import java.util.HashMap;

public class SysProp
{
	
	static HashMap sysProp;
	
	public static Object getSysProp(String key)
	{
		return sysProp.get(key);
	}
}
