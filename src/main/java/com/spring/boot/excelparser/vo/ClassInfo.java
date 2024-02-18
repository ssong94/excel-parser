package com.spring.boot.excelparser.vo;

import com.spring.boot.excelparser.annotation.Date;
import com.spring.boot.excelparser.annotation.ExcelColumn;
import com.spring.boot.excelparser.annotation.ExcelSheet;
import com.spring.boot.excelparser.exception.ExcelParserException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class ClassInfo<T> {

	private final Class<T> tClass;
	private final Field[] fields;

	public static final int STANDARD_NUMBER = 1;

	public static <T> ClassInfo<T> from(Class<T> tClass) {

		validateAnnotation(tClass);

		Field[] fields = tClass.getDeclaredFields();
		Field[] newFields = createNewFields(fields);

		return ClassInfo.<T>builder()
				.tClass(tClass)
				.fields(newFields)
				.build();
	}


	private static void validateAnnotation(Class<?> clazz) {

		ExcelSheet excelSheet = clazz.getAnnotation(ExcelSheet.class);

		String defaultMessage = "Exception occurred in class: " + clazz.getName() + " Cause By: ";

		if (excelSheet == null) {
			throw new ExcelParserException(defaultMessage + "ExcelSheet 어노테이션이 존재하지 않습니다.");
		}
		if (excelSheet.sheetNumber() <= 0) {
			throw new ExcelParserException(defaultMessage + "시작 값이 0보다 작을 수 없습니다.");
		}
		if (excelSheet.start() <= 0) {
			throw new ExcelParserException(defaultMessage + "시작 값이 0보다 작을 수  수 없습니다.");
		}
		if (excelSheet.end() < -1) {
			throw new ExcelParserException(defaultMessage + "종료 값이 -1보다 작을 수 없습니다.");
		}
		if (excelSheet.sheetNumber() > excelSheet.end()) {
			throw new ExcelParserException(defaultMessage + "start 또는 end 값을 확인해 주세요.");
		}
	}

	private static Field[] createNewFields(Field[] fields) {
		return Arrays.stream(fields)
				.filter(field -> field.isAnnotationPresent(ExcelColumn.class))
				.toArray(Field[]::new);
	}

	public List<String> getHeaderNames() {
		return Arrays.stream(fields)
				.filter(field -> field.isAnnotationPresent(ExcelColumn.class))
				.map(field -> field.getAnnotation(ExcelColumn.class))
				.sorted(Comparator.comparingInt(ExcelColumn::order))
				.map(ExcelColumn::headerName)
				.collect(Collectors.toList());
	}


	private ExcelSheet getExcelSheetAnno() {
		return tClass.getAnnotation(ExcelSheet.class);
	}

	private int calculate(int number) {
		return number - STANDARD_NUMBER;
	}

	public int getSheetNumber() {
		return calculate(getExcelSheetAnno().sheetNumber());
	}

	public int getStart() {
		return calculate(getExcelSheetAnno().start());
	}

	public int getEnd() {
		return calculate(getExcelSheetAnno().end());
	}

	public String getDatePattern(Field field) {
		Date dateAnno = field.getAnnotation(Date.class);
		return dateAnno == null ? "" : dateAnno.pattern();
	}

	public int getColumnOrder(Field field) {
		ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
		return calculate(annotation.order());
	}

	public String getMessage(Field field) {
		ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
		return annotation.message();
	}

	public boolean required(Field field) {
		ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
		return annotation.required();
	}


}
