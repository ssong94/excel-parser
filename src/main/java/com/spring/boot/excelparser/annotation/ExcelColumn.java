package com.spring.boot.excelparser.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ExcelColumn {

	int order();

	boolean required() default false;

	String message() default "";

	String headerName() default "";


}
