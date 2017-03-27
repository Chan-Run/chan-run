package com.lbi.framework.app;

import org.apache.http.HttpStatus;

import com.lbi.exception.ErrorCode;

public interface CommonErrorCodes
{
	ErrorCode duplicateErrorCode = new ErrorCode(100, "LBI_DUPLICATE_ERROR_CODE", "Already an error code {0} exist with this error no", HttpStatus.SC_CONFLICT);
}
