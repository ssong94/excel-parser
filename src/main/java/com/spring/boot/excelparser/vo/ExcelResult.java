package com.spring.boot.excelparser.vo;


import java.util.List;
import lombok.Builder;

@Builder
public record ExcelResult<T>(String sheetName,
                             List<String> headerNames,
                             List<T> successData,
                             List<Error> errors)
{ }
