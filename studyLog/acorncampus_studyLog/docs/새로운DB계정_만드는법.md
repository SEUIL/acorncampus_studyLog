
1. 터미널을 관리자 권한으로 켜서 오라클 DB에 접속합니다 
```
sqlplus / as sysdba
```

2. 다음 명령어로 `blog` 계정을 생성하고 권한을 부여합니다 
```sql
-- 계정 생성
-- 계정이름 : blog , 비밀번호 : blog1234 로 통일
CREATE 계정이름 IDENTIFIED BY 비밀번호;

-- 계정 권한 부여
-- 계정이름 : blog 로 통일
GRANT CONNECT, RESOURCE TO 계정이름;

-- 테이블스페이스 용량 설정
-- 계정이름 : blog 로 통일
ALTER USER 계정이름 QUOTA UNLIMITED ON USERS;

```

3. SQL Developer로 접속해서 `blog` 계정으로 로그인합니다
- 사용자 이름 : blog
- 비밀번호 : blog1234
- 호스트 이름 : localhost
- 포트 번호 : 1521
- 서비스 이름 : testdb

4. 접속 후 `schema.sql` 파일을 실행해서 테이블과 초기 데이터를 생성합니다