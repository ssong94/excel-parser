# excel-parser
엑셀 파서 (정확히는 시트 파서)

---
### ⚙️ 개발 환경
- spring boot 3.2.*
- jdk 17
- gradle
- poi 5.* 

---
### 📒 개발 하기 앞서서
1. xlsx, cvs 파일을 파싱할 수 있어야 한다.
2. 엑셀 정보를 가져올 수 있어야 한다.
   - 시트명
   - 헤더명 (컬럼명)
   - 파싱에 성공한 데이터
   - 에러정보
3. `@annotation` 기반으로 설정을 간단하게 해야 한다.
4. 객체의 참조 타입에 맞게 Format을 해줘야 한다. (Reflection API 활용)

---
### ✅ Todo
- [x] annotation 
- [x] exception
- [x] util (helper)
- [x] object 
  - ParsingResult
  - Error

--- 
### ⭐ 사용 방법
결과에는 여러 가지 정보가 포함되어 있다.
```java
void sheetParserTest() {
	
	ExcelResult<TestObject> result = ExcelParser.parse(mf, TestObject.class);

        String sheetName = result.sheetName(); // 시트명
        List<String> headerNames = result.headerNames(); // 컬럼명
        List<TestObject> successData = result.successData(); // 파싱에 성공한 데이터
        List<Error> errors = result.errors(); // 에러 정보
                                             /* detail
                                                1. row: 행 번호
                                                2. cell: 컬럼 번호
                                                3. value: 입력된 값
                                                4. cause: Exception 내용
                                              */
        
        
}
```

객체의 타입은 굉장히 엄격하다.

아래 클래스를 외 다른 타입은 예외처리함.
- Type
   - String
   - LocalDate
   - Integer
   - Double
   - Long
   - BigDecimal

null을 허용하기 위해 Wrapper 클래스를 사용하길 권고.

---

### 📚 Reference

- [How to Read Excel files in Java using Apache POI](https://www.callicoder.com/java-read-excel-file-apache-poi)
- [How to Write to an Excel file in Java using Apache POI](https://www.callicoder.com/java-write-excel-file-apache-poi)