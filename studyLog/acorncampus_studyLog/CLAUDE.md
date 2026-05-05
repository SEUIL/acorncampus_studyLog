# 나만의 학습 기록 블로그 플랫폼

## 기술 스택
- Java 17 / Java EE 8 (javax.*)
- Servlet 기반 MVC2 패턴
- Oracle 19c (ojdbc8)
- 세션 기반 인증
- JSTL 1.2, Toast UI Editor

## 패키지 구조
```
com.acorncampus_studylog
├── controller/   # @WebServlet 서블릿 — URL 라우팅 및 View 포워딩
├── service/      # 비즈니스 로직 (TODO 채워넣는 곳)
├── dao/          # DB 쿼리 (PreparedStatement, Oracle SQL 완전 구현)
├── dto/          # 데이터 전달 객체
├── filter/       # EncodingFilter, LoginCheckFilter
└── util/         # DBUtil, BCryptUtil
```

## 구현된 클래스 목록

### Controller (11개)
| 클래스 | URL 패턴 | 역할 |
|--------|---------|------|
| MainController | `/` | 메인 페이지 |
| UserController | `/user/*` | 로그인·가입·비번변경 |
| MypageController | `/l_check/user/*` | 마이페이지·프로필 수정 |
| PostController | `/post/*`, `/l_check/post/*` | 게시글 CRUD + 이미지 업로드 |
| CommentController | `/l_check/comment/*` | 댓글·대댓글 AJAX |
| TagController | `/tag/*` | 태그 클라우드 |
| SeriesController | `/series/*`, `/l_check/series/*` | 시리즈 CRUD |
| SearchController | `/search.do` | 키워드 검색 |
| LikeController | `/l_check/like/*` | 좋아요·싫어요 AJAX |
| ReportController | `/l_check/report/*` | 신고 AJAX |
| AdminController | `/admin/*` | 관리자 (회원·글·댓글·신고·태그) |

### Service / DAO / DTO
- Service 7개: UserService, PostService, CommentService, TagService, SeriesService, LikeService, ReportService
- DAO 7개: UserDao, PostDao, CommentDao, TagDao, SeriesDao, LikeDao, ReportDao (Oracle SQL 완전 구현)
- DTO 8개: UserDto(기존), UserDetailDto, PostDto, CommentDto, TagDto, SeriesDto, ReportDto, PageDto

### 기타
- `src/main/resources/schema.sql` — Oracle DDL (9테이블 + 6시퀀스 + 인덱스)
- `BACKEND.md` — 팀원 온보딩 문서 (URL 흐름, 코딩 규칙, 작업 분담 가이드)
- `src/test/java/com/acorncampus_studylog/dao/DaoIntegrationTest.java` — DAO·DTO·schema 통합 검증 테스트 (JUnit 5, 실 DB 연결, 22개 케이스)

## 테스트 실행
```bash
mvn test -Dtest=DaoIntegrationTest
```
- 전제조건: Oracle testdb 실행 중, `blog` 계정 + schema.sql 테이블 생성 완료
- `@AfterAll`에서 테스트 데이터 자동 하드 삭제 → 반복 실행 가능

## 알려진 수정 이력
| 파일 | 내용 |
|------|------|
| `PostDao.java` | `mapRow`에서 `series_id`, `updated_at` 컬럼을 try/catch 없이 읽어 `search`·`findByTag` 호출 시 "부적합한 열 이름" 오류 발생 → try/catch로 수정 완료 |
| `pom.xml` | `maven-surefire-plugin 3.2.5` 추가 (JUnit 5 mvn test 실행에 필요) |

## 핵심 규칙
- DB 쿼리: `PreparedStatement` 만 사용, `Statement` 금지 (SQL Injection 방지)
- XSS 방지: JSP 출력 시 `<c:out>` 또는 `fn:escapeXml()` 사용
- 비밀번호: 반드시 `BCryptUtil.hash()` 후 DB 저장, 평문 저장 금지
- DB 자원: 사용 후 반드시 `DBUtil.close(conn, pstmt, rs)` 호출

## URL 패턴
| 패턴 | 설명 |
|------|------|
| `/user/*` | 로그인 불필요 (로그인, 회원가입, 로그아웃) |
| `/l_check/*` | 로그인 필수 (LoginCheckFilter 자동 적용) |
| `/admin/*` | ADMIN 역할 필수 (LoginCheckFilter `/admin/*` 매핑 추가됨) |

## 세션
- 키: `loginUser` / 타입: `UserDto`
- 타임아웃: 30분
- 로그인: `session.setAttribute("loginUser", userDto)`
- 로그아웃: `session.invalidate()`

## DB 접속 정보
- URL: `jdbc:oracle:thin:@localhost:1521:testdb`
- USER: `blog` / PASS: `blog1234`
- 연결: `DBUtil.getConnection()`

## 공통 JSP
```jsp
<%@ include file="/WEB-INF/views/common/header.jsp" %>
    <!-- 본문 -->
<%@ include file="/WEB-INF/views/common/footer.jsp" %>
```
- `pageTitle` 속성 설정 시 탭 제목 변경: `request.setAttribute("pageTitle", "글 목록")`
- CSS: `resources/css/style.css` / JS: `resources/js/main.js`
- 업로드 이미지: `resources/upload/`

## AJAX JSON 응답 작성법 (Gson 사용)

AJAX 응답이 필요한 Controller (LikeController, CommentController, ReportController, UserController 이메일 중복확인)는
아래 패턴을 그대로 복사해서 사용하면 됩니다.

### 1. 의존성 확인
`pom.xml`에 Gson이 이미 추가되어 있습니다. import만 하면 됩니다.
```java
import com.google.gson.Gson;
import java.util.LinkedHashMap;
import java.util.Map;
```

### 2. 성공 응답 예시 (좋아요 토글 결과)
```java
private void handleLikePost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
    UserDto loginUser = (UserDto) req.getSession(false).getAttribute("loginUser");
    long postId = Long.parseLong(req.getParameter("postId"));
    String likeType = req.getParameter("likeType"); // "L" or "D"

    Map<String, Object> result = likeService.togglePostLike(loginUser.getUserId(), postId, likeType);

    resp.setContentType("application/json; charset=UTF-8");
    resp.getWriter().write(new Gson().toJson(result));
}
```

### 3. 단순 성공/실패 응답 예시 (신고, 이메일 중복확인)
```java
// 성공
Map<String, Object> result = new LinkedHashMap<>();
result.put("status", "ok");
result.put("message", "신고가 접수되었습니다.");

// 실패 (중복 신고)
result.put("status", "error");
result.put("message", "이미 신고한 게시글입니다.");

resp.setContentType("application/json; charset=UTF-8");
resp.getWriter().write(new Gson().toJson(result));
```

### 4. 불린값 응답 예시 (이메일 중복확인)
```java
// {"available": true} 또는 {"available": false}
boolean available = userService.checkEmailAvailable(email);

Map<String, Object> result = new LinkedHashMap<>();
result.put("available", available);

resp.setContentType("application/json; charset=UTF-8");
resp.getWriter().write(new Gson().toJson(result));
```

### 5. 핵심 규칙
- `resp.setContentType("application/json; charset=UTF-8")` — **반드시 write 전에** 호출
- Map 키는 프론트 JS에서 읽는 이름과 정확히 일치해야 함
- Service가 `Map<String, Object>`를 반환하면 Controller에서 그대로 `Gson().toJson()` 통과시키면 됨

---

## View 경로
```
WEB-INF/views/
├── user/     post/     comment/
├── tag/      series/   search/
├── admin/    common/   (header.jsp, footer.jsp)
```
