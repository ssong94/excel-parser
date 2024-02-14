package com.spring.boot.excelparser.helper;

import com.spring.boot.excelparser.exception.ExcelParserException;
import com.spring.boot.excelparser.exception.InvalidCellValueException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;


public class FormatHelper {

	private final static DataFormatter fmt;

	static {
		fmt = new DataFormatter();
	}


	public static String format(final String message, Object... args) {
		return ParameterizedMessage.format(message, args);
	}

	public static String toString(Cell cell) {
		String value = fmt.formatCellValue(cell);
		return value.isEmpty() ? value : value.trim();
	}


	@SuppressWarnings("unchecked")
	public static <T> T toFieldType(String value, Class<T> type, String pattern) {

		if (type.equals(String.class)) {
			return (T) value;
		}
		try {
			if (type.equals(LocalDate.class)) {
				return (T) toLocalDate(value, pattern);
			}

			if (type.equals(Integer.class)) {
				return (T) toInteger(value);
			}

			if (type.equals(Double.class)) {
				return (T) toDouble(value);
			}

			if (type.equals(Long.class)) {
				return (T) toLong(value);
			}

			if (type.equals(BigDecimal.class)) {
				return (T) toBigDecimal(value);
			}

		} catch (NumberFormatException ex) {
			throw new InvalidCellValueException("유효하지 않는 값입니다.");
		}

		throw new ExcelParserException(
				format("지원하지 않는 타입입니다. field type= {}", type.getName()));
	}

	private static LocalDate toLocalDate(String value, String pattern) {
		try{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
			return LocalDate.parse(value, formatter);
		} catch (DateTimeParseException e) {
			throw new InvalidCellValueException(format("유효하지 않는 패턴입니다. 입력값= {}", value));
		}
	}

	private static Integer toInteger(String value) {
		return Integer.valueOf(value);
	}

	private static Double toDouble(String value) {
		return Double.valueOf(value);
	}

	private static Long toLong(String value) {
		return Long.valueOf(value);
	}

	private static BigDecimal toBigDecimal(String value) {
		return new BigDecimal(value);
	}


}