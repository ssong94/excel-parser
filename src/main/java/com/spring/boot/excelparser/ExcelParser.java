package com.spring.boot.excelparser;

import com.spring.boot.excelparser.exception.ExcelParserException;
import com.spring.boot.excelparser.exception.InvalidCellValueException;
import com.spring.boot.excelparser.helper.FormatHelper;
import com.spring.boot.excelparser.vo.ClassInfo;
import com.spring.boot.excelparser.vo.Error;
import com.spring.boot.excelparser.vo.ExcelResult;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
public final class ExcelParser {

	private static final List<String> EXCEL_EXTENSIONS = List.of("xlsx", "cvs");

	public static <T> ExcelResult<T> parse(MultipartFile mf, Class<T> clazz) {

		validateExtension(mf.getOriginalFilename());
		ClassInfo<T> classInfo = ClassInfo.from(clazz);

		try (Workbook workbook = WorkbookFactory.create(mf.getInputStream())) {
			return doParsing(workbook, classInfo);
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

	private static void validateExtension(String fileName) {
		String extension = FilenameUtils.getExtension(fileName);
		if (!EXCEL_EXTENSIONS.contains(extension)) {
			throw new ExcelParserException("지원하지 않는 확장자입니다.");
		}
	}

	public static <T> ExcelResult<T> doParsing(Workbook workbook, ClassInfo<T> classInfo) {

		Sheet sheet = validateAndGetSheet(workbook, classInfo.getSheetNumber());

		int lastRowNum = sheet.getLastRowNum();
		int annotationEndNum = classInfo.getEnd();
		int rowCount = (annotationEndNum == -1) ? lastRowNum : annotationEndNum;

		List<T> successData = new ArrayList<>();
		List<Error> errors = new ArrayList<>();

		int start = classInfo.getStart();
		for (int i = start; i <= rowCount; i++) {
			Row row = sheet.getRow(i);
			T instance = createNewInstanceAndFillData(row, classInfo, errors);
			// null값이 들어오면 실패로 인식한다.
			if (instance == null) {
				continue;
			}

			successData.add(instance);
		}

		return ExcelResult.<T>builder()
				.sheetName(sheet.getSheetName())
				.headerNames(classInfo.getHeaderNames())
				.successData(successData)
				.errors(errors)
				.build();
	}


	private static Sheet validateAndGetSheet(Workbook workbook, int sheetNumber) {
		return Optional.ofNullable(workbook.getSheetAt(sheetNumber))
				.orElseThrow(() -> new ExcelParserException("선택하신 Sheet가 존재하지 않습니다."));
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

	private static <T> void setFieldData(T instance, Field field, Object value) {
		try {
			field.setAccessible(true);
			field.set(instance, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new InvalidCellValueException("타입이 일치하지 않습니다.");
		}
	}


	private static <T> T createNewInstanceAndFillData(Row row, ClassInfo<T> classInfo, List<Error> errors) {

		T instance = createNewInstance(classInfo.getTClass());

		boolean isAllEmpty = true; // Row에서 모든 값이 비어있으면 null을 리턴해서 성공으로 치지 않는다.
		boolean failure = false; // Row에서 하나라도 예외가 터지면 null을 리턴해서 성공으로 치지 않는다.

		for (Field field : classInfo.getFields()) {

			int fieldOrder = classInfo.getColumnOrder(field);
			Cell cell = row.getCell(fieldOrder);
			String value = FormatHelper.toString(cell);

			try {

				Object o = FormatHelper.toFieldType(value, field.getType(), classInfo.getDatePattern(field));
				setFieldData(instance, field, o);

				boolean hasText = StringUtils.hasText(value);

				if (classInfo.required(field) && !hasText) {
					throw new InvalidCellValueException("필수값입니다.");
				}

				if (hasText) {
					isAllEmpty = false;
				}

			} catch (InvalidCellValueException ex) {

				failure = true;

				String message = classInfo.getMessage(field);
				String exceptionMessage = ex.getMessage();

				String cause = (StringUtils.hasText(message))
						? exceptionMessage + " " + message
						: exceptionMessage;

				IntBinaryOperator sumFn = Integer::sum;
				int standard_number = ClassInfo.STANDARD_NUMBER;

				Error error = Error.builder()
						.row(sumFn.applyAsInt(row.getRowNum(), standard_number))
						.column(sumFn.applyAsInt(cell.getColumnIndex(), standard_number))
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


}