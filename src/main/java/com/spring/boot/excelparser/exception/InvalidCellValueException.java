package com.spring.boot.excelparser.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidCellValueException extends ExcelParserException {

	public InvalidCellValueException(String message) {
		super(message);
	}

	public InvalidCellValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidCellValueException(Throwable cause) {
		super(cause);
	}


}
