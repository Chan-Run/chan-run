package com.lbi.exception;

import java.text.MessageFormat;

public class LBIException extends RuntimeException
{
	ErrorCode errCode;
	
	Throwable cause;
	
	Object[] args;
	
	public LBIException(ErrorCode errCode)
	{
		this(errCode, null);
	}
	
	public LBIException(ErrorCode errCode, Throwable cause, Object... arguments)
	{
		this.errCode = errCode;
		this.cause = cause;
		this.args = arguments;
	}
	
	public boolean equals(Object obj)
	{
		if(!(obj instanceof LBIException))
		{
			return false;
		}
		LBIException ex = (LBIException) obj;
		return ex.errCode.getErrNo() == this.errCode.getErrNo();
	}
	
	public String getMsg()
	{
		String errMsg = errCode.getErrMsg();
		return MessageFormat.format(errMsg, args);
	}
	
}
