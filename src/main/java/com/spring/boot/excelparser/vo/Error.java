package com.spring.boot.excelparser.vo;

import lombok.Builder;

@Builder
public record Error(int row,
                    int column,
                    String cause,
                    String value)
{ }
