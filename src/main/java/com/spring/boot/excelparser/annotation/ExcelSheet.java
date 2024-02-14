package com.spring.boot.excelparser.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ExcelSheet {

	int start();

	/**
	 * -1이면 모든 Row를 읽는다.
	 * @return
	 */
	int end() default -1;

	int sheetNumber() default 0;


	ParseType parseType() default ParseType.ROW;

	enum ParseType {
		ROW,
		COLUMN
	}

}
