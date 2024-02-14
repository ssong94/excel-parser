package com.spring.boot.excelparser;

import com.spring.boot.excelparser.vo.Error;
import com.spring.boot.excelparser.vo.ExcelResult;
import com.spring.boot.excelparser.vo.TestSheet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
class ExcelExcelParserApplicationTests {

	@Test
	void contextLoads() {
	}


	@Test
	void test() throws IOException {

		Path path = Paths.get("C:\\Users\\ssong94\\바탕 화면\\TestExcel.xlsx");
		String name = "file.xlsx";
		String originalFileName = "file.xlsx";
		String contentType = "application/vnd.ms-excel";
		byte[] content = null;
		try {
			content = Files.readAllBytes(path);
		} catch (final IOException e) {
		}
		MultipartFile mf = new MockMultipartFile(name,
				originalFileName, contentType, content);


		ExcelResult<TestSheet> result = ExcelParser.parse(mf, TestSheet.class);

		List<Error> errors = result.errors();
		List<String> headerNames = result.headerNames();
		String sheetName = result.sheetName();
		List<TestSheet> successData = result.successData();

		System.out.println("success");


	}


}
