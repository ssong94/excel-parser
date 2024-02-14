package com.spring.boot.excelparser.vo;

import com.spring.boot.excelparser.annotation.Date;
import com.spring.boot.excelparser.annotation.ExcelColumn;
import com.spring.boot.excelparser.annotation.ExcelSheet;
import java.time.LocalDate;
import lombok.Getter;

@Getter
@ExcelSheet(start = 15, sheetNumber = 1, end = 30)
public class TestSheet {

	@ExcelColumn(order = 1, headerName = "1번 컬럼", required = true, message = "패턴을 ~~ 형태로 입력해 주세요.")
	private String field1;

	@ExcelColumn(order = 2, headerName = "2번 컬럼", required = true, message = "~~")
	private String field2;

	@ExcelColumn(order = 3, headerName = "3번 컬럼")
	private String field3;

	@Date
	@ExcelColumn(order = 4, headerName = "4번 컬럼", required = true, message = "ex) 2024-01-01 형태로 입력해 주세요.")
	private LocalDate field4;




}
