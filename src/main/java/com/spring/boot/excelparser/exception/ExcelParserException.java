package com.spring.boot.excelparser.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ExcelParserException extends RuntimeException {

	public ExcelParserException(String message) {
		super(message);
	}

	public ExcelParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExcelParserException(Throwable cause) {
		super(cause);
	}

}
