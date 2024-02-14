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
- [x] ParsingResult Object + Error Object

