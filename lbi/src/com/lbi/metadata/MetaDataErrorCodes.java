package com.lbi.metadata;

import org.apache.http.HttpStatus;

import com.lbi.exception.ErrorCode;

public interface MetaDataErrorCodes
{
	public static final ErrorCode ZERO_COLUMNS_IN_TEMP_TABLE_CREATION = new ErrorCode(301, "ZERO_COLUMNS_IN_TEMP_TABLE_CREATION", "Zero columns return while creating temp table from {0}. Constructed Query {1}", HttpStatus.SC_INTERNAL_SERVER_ERROR);
}
