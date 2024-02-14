package com.spring.boot.excelparser;

import com.spring.boot.excelparser.annotation.Date;
import com.spring.boot.excelparser.annotation.ExcelColumn;
import com.spring.boot.excelparser.annotation.ExcelSheet;
import com.spring.boot.excelparser.exception.ExcelParserException;
import com.spring.boot.excelparser.exception.InvalidCellValueException;
import com.spring.boot.excelparser.helper.FormatHelper;
import com.spring.boot.excelparser.vo.Error;
import com.spring.boot.excelparser.vo.ExcelResult;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntBinaryOperator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelParser {

	private static final List<String> EXCEL_EXTENSIONS = List.of("xlsx", "cvs");
	public static final int STANDARD_NUMBER = 1;


	public static <T> ExcelResult<T> parse(MultipartFile mf, Class<T> clazz) {

		validateExtension(mf.getOriginalFilename());
		validateAnnotation(clazz);

		try (Workbook workbook = WorkbookFactory.create(mf.getInputStream())) {
			return doParsing(workbook, clazz);
		} catch (IOException ex) {
			throw new ExcelParserException(ex.getMessage(), ex);
		} catch (EncryptedDocumentException ex) {
			throw new ExcelParserException("암호화된 파일입니다.", ex);
		} catch (InvalidCellValueException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ExcelParserException(ex);
		}

	}

	private static void validateAnnotation(Class<?> clazz) {

		ExcelSheet annotation = clazz.getAnnotation(ExcelSheet.class);

		if (annotation == null) {
			throw new ExcelParserException("ExcelSheet 어노테이션이 존재하지 않습니다.");
		}
		if (annotation.sheetNumber() <= 0) {
			throw new ExcelParserException("시작 값이 0보다 작을 수 없습니다.");
		}
		if (annotation.start() <= 0) {
			throw new ExcelParserException("시작 값이 0보다 작을 수  수 없습니다.");
		}
		if (annotation.end() < -1) {
			throw new ExcelParserException("종료 값이 -1보다 작을 수 없습니다.");
		}

	}

	private static void validateExtension(String fileName) {
		String extension = FilenameUtils.getExtension(fileName);
		if (!EXCEL_EXTENSIONS.contains(extension)) {
			throw new ExcelParserException("지원하지 않는 확장자입니다.");
		}
	}

	public static <T> ExcelResult<T> doParsing(Workbook workbook, Class<T> tClass) {

		Sheet sheet = validateAndGetSheet(workbook, tClass);

		int totalRowCount = sheet.getLastRowNum();
		ExcelSheet excelSheet = tClass.getAnnotation(ExcelSheet.class);
		int start = getStart(excelSheet);
		int end = getEnd(excelSheet);
		int rowCount = (end == -1) ? totalRowCount : end;

		Field[] fields = tClass.getDeclaredFields();

		List<T> successData = new ArrayList<>();
		List<Error> errors = new ArrayList<>();

		for (int i = start; i <= rowCount; i++) {
			Row row = sheet.getRow(i);
			T instance = createNewInstanceAndFillData(row, tClass, fields, errors);
			// null값이 들어오면 실패로 인식한다.
			if (instance == null) {
				continue;
			}

			successData.add(instance);
		}

		return ExcelResult.<T>builder()
				.sheetName(sheet.getSheetName())
				.headerNames(getHeaderNames(fields))
				.successData(successData)
				.errors(errors)
				.build();
	}

	private static int getStart(ExcelSheet annotation) {
		return annotation.start() - STANDARD_NUMBER;

	}

	private static int getEnd(ExcelSheet annotation) {
		return annotation.end() - STANDARD_NUMBER;
	}

	private static int getSheetNumber(ExcelSheet annotation) {
		return annotation.sheetNumber() - STANDARD_NUMBER;
	}

	private static int getColumnOrder(ExcelColumn annotation) {
		return annotation.order() - STANDARD_NUMBER;
	}

	private static List<String> getHeaderNames(Field[] fields) {
		String[] columnArr = new String[fields.length];

		for (Field field : fields) {
			ExcelColumn columnAnno = field.getAnnotation(ExcelColumn.class);
			if (columnAnno != null) {
				int index = getColumnOrder(columnAnno);
				columnArr[index] = columnAnno.headerName();
			}
		}

		return List.of(columnArr);
	}


	private static Sheet validateAndGetSheet(Workbook workbook, Class<?> clazz) {
		ExcelSheet annotation = clazz.getAnnotation(ExcelSheet.class);
		Sheet sheet = workbook.getSheetAt(getSheetNumber(annotation));
		if (sheet == null) {
			throw new ExcelParserException("선택하신 Sheet가 존재하지 않습니다.");
		}
		return sheet;
	}


	private static <T> T createNewInstance(Class<T> tClass) {
		try {
			Constructor<T> constructor = tClass.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
		} catch (Exception e) {
			throw new ExcelParserException("Exception occurred while instantiating the class " + tClass.getName());
		}
	}

	private static String getDatePattern(Field field) {
		Date dateAnno = field.getAnnotation(Date.class);
		return dateAnno == null ? "" : dateAnno.pattern();
	}

	private static <T> T createNewInstanceAndFillData(Row row, Class<T> tClass, Field[] fields, List<Error> errors) {

		T instance = createNewInstance(tClass);

		boolean isAllEmpty = true; // Row에서 모든 값이 비어있으면 null을 리턴해서 성공으로 치지 않는다.
		boolean failure = false; // Row에서 하나라도 예외가 터지면 null을 리턴해서 성공으로 치지 않는다.

		for (Field field : fields) {

			ExcelColumn columnAnno = field.getAnnotation(ExcelColumn.class);

			if (columnAnno == null) {
				continue;
			}

			Cell cell = row.getCell(getColumnOrder(columnAnno));
			String value = FormatHelper.toString(cell);

			try {

				Object o = FormatHelper.toFieldType(value, field.getType(), getDatePattern(field));
				setFieldData(instance, field, o);

				boolean hasText = StringUtils.hasText(value);

				if (columnAnno.required() && !hasText) {
					throw new InvalidCellValueException("필수값입니다.");
				}

				if (hasText) {
					isAllEmpty = false;
				}

			} catch (InvalidCellValueException ex) {

				failure = true;

				String message = columnAnno.message();
				String exceptionMessage = ex.getMessage();

				String cause = (StringUtils.hasText(message))
						? exceptionMessage + " " + message
						: exceptionMessage;

				IntBinaryOperator sumFn = Integer::sum;

				Error error = Error.builder()
						.row(sumFn.applyAsInt(row.getRowNum(), STANDARD_NUMBER))
						.column(sumFn.applyAsInt(cell.getColumnIndex(), STANDARD_NUMBER))
						.value(value)
						.cause(cause)
						.build();

				errors.add(error);
			}

		}

		if (isAllEmpty || failure) {
			return null;
		}

		return instance;
	}

	private static <T> void setFieldData(T instance, Field field, Object value) {
		try {
			field.setAccessible(true);
			field.set(instance, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new InvalidCellValueException("타입이 일치하지 않습니다.");
		}
	}


}