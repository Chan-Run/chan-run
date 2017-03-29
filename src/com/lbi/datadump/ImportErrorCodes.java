package com.lbi.datadump;

import org.apache.http.HttpStatus;

import com.lbi.exception.ErrorCode;

public interface ImportErrorCodes
{
	public static final ErrorCode WRONG_PATH_IN_MODULE_IMPORT = new ErrorCode(201, "WRONG_PATH_IN_MODULE_IMPORT", "There is no directory in the given path {0}", HttpStatus.SC_INTERNAL_SERVER_ERROR);
}
