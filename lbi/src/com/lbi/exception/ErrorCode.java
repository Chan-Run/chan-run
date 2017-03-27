package com.lbi.exception;

import java.util.concurrent.ConcurrentHashMap;

import com.lbi.framework.app.CommonErrorCodes;

public class ErrorCode implements CommonErrorCodes
{
	
	private int errNo;
	private String errName;
	private String errMsg;
	private int httpStatus;
	
	private static final ConcurrentHashMap<Integer, ErrorCode> errCodeMap = new ConcurrentHashMap<Integer, ErrorCode>();
	
	public ErrorCode(int no, String name, String msg, int httpStatus)
	{
		this.errNo = no;
		this.errName = name;
		this.errMsg = msg;
		this.httpStatus = httpStatus;
		
		throwIfAlreadyExist();
		
	}
	
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		
		s.append("Error No : ").append(errNo)
		.append(" :: Error Name : ").append(errName)
		.append(" :: Error Msg : ").append(errMsg)
		.append(" :: HttpStatus : ").append(httpStatus);
		
		return s.toString();
	}
	
	public void throwIfAlreadyExist()
	{
		ErrorCode errCode = errCodeMap.put(errNo, this);
		if(errCode != null)
		{
			throw new LBIException(duplicateErrorCode, null, errCode.errName);
		}
	}
	
	public int getErrNo()
	{
		return this.errNo;
	}
	
	public String getErrMsg()
	{
		return this.errMsg;
	}
	
	public String getErrName()
	{
		return this.errName;
	}
	
	public int getHttpStatus()
	{
		return this.httpStatus;
	}
}
