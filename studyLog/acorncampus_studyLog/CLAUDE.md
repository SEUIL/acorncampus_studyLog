# 나만의 학습 기록 블로그 플랫폼

> 최종 업데이트: 2026-05-11

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
├── service/      # 비즈니스 로직 (전원 구현 완료)
├── dao/          # DB 쿼리 (PreparedStatement, Oracle SQL 완전 구현)
├── dto/          # 데이터 전달 객체
├── filter/       # EncodingFilter, LoginCheckFilter, JandiFilter
└── util/         # DBUtil, BCryptUtil, MailUtil
```

## 구현 완료 현황 (2026-05-11 기준)

### Controller (13개 — 전원 완료)
| 클래스 | URL 패턴 | 역할 | 상태 |
|--------|---------|------|------|
| MainController | `/` | 메인 페이지 | ✅ |
| UserController | `/user/*` | 로그인·가입·비번변경·탈퇴 | ✅ |
| MypageController | `/l_check/user/*` | 마이페이지·프로필 수정(파일업로드) | ✅ |
| PostController | `/post/*`, `/l_check/post/*` | 게시글 CRUD + 이미지 업로드 | ✅ |
| CommentController | `/l_check/comment/*` | 댓글·대댓글 AJAX | ✅ |
| TagController | `/tag/*` | 태그 클라우드 (사용빈도·최신·이름순 정렬) | ✅ |
| SeriesController | `/series/*`, `/l_check/series/*` | 시리즈 CRUD | ✅ |
| SearchController | `/search.do` | 키워드 검색 | ✅ |
| LikeController | `/l_check/like/*` | 좋아요·싫어요 AJAX | ✅ |
| ReportController | `/l_check/report/*` | 신고 AJAX | ✅ |
| AdminController | `/admin/*` | 관리자 (회원·글·댓글·신고·태그) AJAX 리팩토링 완료 | ✅ |
| PasswordResetController | `/user/pwd-reset/*` | 이메일 토큰 기반 비밀번호 재설정 | ✅ |
| AiController | `/l_check/ai/*` | AI 글쓰기 보조 JSON API, 백엔드 구현 완료 | ✅ |

### Service (9개 — 전원 완료)
| 클래스 | 상태 | 비고 |
|--------|------|------|
| UserService | ✅ | login, register, 프로필수정, 비번변경, 탈퇴, ban/unban/forceDelete |
| PostService | ✅ | CRUD, 검색, 이미지저장, 관리자 기능 |
| CommentService | ✅ | 트리 조립, CRUD, 관리자 기능 |
| SeriesService | ✅ | CRUD, 입력값 검증 포함 |
| LikeService | ✅ | 게시글/댓글 좋아요·싫어요 상태머신 |
| ReportService | ✅ | 신고 접수(중복방지), 관리자 처리 |
| TagService | ✅ | 태그 클라우드, 관리자 정렬(사용빈도·최신·이름순) |
| PasswordResetService | ✅ | SecureRandom 토큰, BCrypt 해싱, 30분 만료, 재사용 방지 |
| AiWritingService | ✅ | OpenAI 글쓰기 보조 검증, 쿨다운, 프롬프트 라우팅, 사용 로그 기록 |

### DAO / DTO
- DAO 9개: UserDao, PostDao, CommentDao, TagDao, SeriesDao, LikeDao, ReportDao, PasswordResetDao, AiUsageLogDao (Oracle SQL 완전 구현)
- DTO 10개: UserDto(세션용), UserDetailDto, PostDto, CommentDto, TagDto, SeriesDto, ReportDto, PageDto, PasswordResetTokenDto, AiUsageLogDto

### JSP Views
| 경로 | 상태 | 비고                                         |
|------|------|--------------------------------------------|
| `views/main.jsp` | ✅ | 커뮤니티 메인                                    |
| `views/common/header.jsp` | ✅ | 네비게이션, 로그인 상태                              |
| `views/common/sideBar.jsp` | ✅ | 사이드바 (테마 유지 포함)                            |
| `views/user/login.jsp` | ✅ | index.jsp authMode=login                   |
| `views/user/register.jsp` | ✅ | index.jsp authMode=register                |
| `views/user/password.jsp` | ✅ | 비밀번호 변경                                    |
| `views/user/mypage.jsp` | ✅ | 마이페이지                                      |
| `views/user/update.jsp` | ✅ | 프로필 수정                                     |
| `views/user/public_profile.jsp` | ✅ | 다른 유저 공개 프로필 (PR #66)                       |
| `views/user/forgot_password.jsp` | ✅ | 비밀번호 찾기 이메일 입력                              |
| `views/user/reset_password.jsp` | ✅ | 이메일 토큰으로 새 비밀번호 설정                          |
| `views/post/list.jsp` | ✅ | 게시글 목록                                     |
| `views/post/detail.jsp` | ✅ | 게시글 상세 + 댓글                                |
| `views/post/write.jsp` | ✅ | 작성/수정 통합 폼                                 |
| `views/series/list.jsp` | ✅ | 시리즈 목록                                     |
| `views/series/detail.jsp` | ✅ | 시리즈 상세                                     |
| `views/series/write.jsp` | ✅ | 시리즈 작성/수정 통합                               |
| `views/search/result.jsp` | ✅ | 검색 결과                                      |
| `views/admin/main.jsp` | ✅ | 관리자 대시보드                                   |
| `views/admin/user/list.jsp` | ✅ | 회원 관리                                      |
| `views/admin/post/list.jsp` | ✅ | 게시글 관리                                     |
| `views/admin/report/list.jsp` | ✅ | 신고 관리                                      |
| `views/admin/tag/list.jsp` | ✅ | 태그 관리                                      |
| `views/admin/comment/list.jsp` | ✅ | 관리자 댓글 관리 — AJAX DELETE (PR #62, KKH)      |

### CSS 아키텍처 (3계층, 리팩토링 완료)
```
resources/css/
├── global_theme.css        # CSS 변수, 다크/라이트 테마
├── style.css               # 진입점
├── components/             # 재사용 컴포넌트
│   ├── button.css, form.css, layout.css, typography.css
│   ├── ui.css, table.css, tabs.css, series.css, jandi.css
└── pages/                  # 페이지별
    ├── admin/              # admin_main, post_list, report_list, tag_list, user_list
    ├── auth/               # index, login, register
    ├── common/             # sidebar
    ├── community/          # community_main
    ├── post/               # post_detail, post_list, post_write
    ├── series/             # series_detail, series_list
    ├── user/               # profile_update, public_profile
    └── workspace/          # workspace_main
```

## JS 리소스
- `resources/js/main.js` — 공통 JS (테마 전환 등)
- `resources/js/interactions.js` — 스크롤 Reveal 애니메이션, 좋아요 파티클 버스트 (PR #66)

### 기타
- `src/main/resources/schema.sql` — Oracle DDL (9테이블 + 6시퀀스 + 인덱스)
- `docs/분업/분업_가이드.md` — 팀원 온보딩 문서
- `src/test/java/com/acorncampus_studylog/dao/DaoIntegrationTest.java` — DAO 통합 테스트 (JUnit 5, 실 DB, 22개 케이스)

## 테스트 실행
```bash
mvn test -Dtest=DaoIntegrationTest
```
- 전제조건: Oracle testdb 실행 중, `blog` 계정 + schema.sql 테이블 생성 완료
- `@AfterAll`에서 테스트 데이터 자동 하드 삭제 → 반복 실행 가능

### AI 글쓰기 보조 백엔드 테스트
```bash
mvnw.cmd -q -DskipTests compile
mvnw.cmd -q -Dtest=Ai*Test test
```
- 기본 테스트는 OpenAI 네트워크 호출 없이 실행된다.
- 실제 OpenAI 스모크 테스트는 `src/main/resources/openai.properties`를 로컬에 만든 뒤 `mvnw.cmd -q -Dtest=OpenAiRealApiTest -Dopenai.realTest=true test`로만 실행한다.
- Windows PowerShell에서는 점이 포함된 Maven 속성을 따옴표로 감싸 `"-Dopenai.realTest=true"`처럼 실행한다.

## AI 글쓰기 보조 현재 상태
- 백엔드는 `/l_check/ai/assist.do` POST JSON API까지 구현 완료. 프론트 모달, 단축키 연결, 미리보기 적용 UI는 사용자 승인 전까지 구현 완료로 보지 않는다.
- 단축키 요구사항은 `Ctrl+Space`이며, 프론트 작업 시 기존 Milkdown 편집기 흐름과 초안 보존 규칙을 따라야 한다.
- 설정 파일은 `src/main/resources/openai.properties`이며 `.gitignore`에 등록되어 있다. 저장소에는 `openai.properties.example`만 둔다.
- 제한값: `draftText` 3,000자, `customPrompt` 500자, 요청 본문 8KB, OpenAI 출력 600 tokens, 사용자별 15초 쿨다운.
- 요청 작업: `IMPROVE`, `SUMMARY`, `EXPAND`, `TITLE`, `TAGS`, `CUSTOM`.
- 자세한 설정과 실행 순서는 `docs/AI_글쓰기_도우미_런북.md`를 확인한다.

## 알려진 이슈 및 수정 이력
| 파일 | 내용 | 심각도 |
|------|------|--------|
| `LikeController`, `CommentController`, `ReportController` | JSON을 Gson 대신 `String.format()` 문자열 조립 → 에러 메시지에 `"` 포함 시 JSON 깨질 수 있음 | MEDIUM |
| `PostController.handleDetail()` | `LikeService`를 필드가 아닌 메서드 내 지역 변수로 생성 — line 113: `LikeService likeService = new LikeService()` | MEDIUM |
| `PasswordResetService.generateToken()` | `SecureRandom` 매 호출마다 새 인스턴스 생성 → `static final` 필드로 승격 권장 | MEDIUM |
| `PasswordResetService.requestReset()` | 메일 발송 실패 시 DB 저장 토큰이 고아로 남음 → catch 후 토큰 무효화 필요 | MEDIUM |
| `PasswordResetController` | 비밀번호 최대 길이(BCrypt 72바이트) 검증 없음 / 구현 완료 메서드에 TODO 주석 잔류 | LOW |
| `JandiFilter` Javadoc | 클래스 설명 "24주(168일)" vs 실제 상수 `DISPLAY_DAYS = 84`(12주) 불일치 | LOW |
| `JandiFilter` catch 블록 | 예외 catch 시 로깅 없음 → `Logger.warning()` 추가 권장 | LOW |
| `admin/comment/list.jsp` AJAX | `response.ok` 체크 없음 — HTTP 4xx/5xx가 `.catch()`로 분기되지 않음 | LOW |
| `LikeController` | `likeType` 파라미터 "L"/"D" 외 값 입력 검증 없음 | LOW |
| `UserService` | TODO 주석이 이미 구현된 코드 위에 남아있어 혼란 야기 — 정리 필요 | LOW |
| `views/admin/comment/list.jsp` | 파일 누락 — AdminController 참조 오류 | 완료 (PR #62, KKH) |
| `PasswordResetController/Service` | 이메일 기반 비밀번호 재설정 기능 추가 | 완료 (PR #65, 진희) |
| `views/user/public_profile.jsp` | 다른 유저 공개 프로필 페이지 추가 | 완료 (PR #66, JCJ) |
| `TagService` | 관리자 태그 정렬 파라미터(사용빈도·최신·이름순) 추가 | 완료 (PR #67, KKH) |
| `JandiFilter` | 사이드바 잔디 기여도 필터 추가, web.xml 등록 | 완료 (JCJ) |
| `PostDao.java` | `mapRow`에서 `series_id`, `updated_at` 컬럼 → try/catch로 수정 완료 | 완료 |
| `pom.xml` | `maven-surefire-plugin 3.2.5` 추가 (JUnit 5 mvn test 실행에 필요) | 완료 |
| `AdminController` (건희) | ban/unban/강제삭제/신고처리 AJAX 방식으로 리팩토링 완료 | 완료 |
| CSS | 단일 파일에서 3계층 아키텍처로 리팩토링 완료 | 완료 |

## 핵심 규칙
- DB 쿼리: `PreparedStatement` 만 사용, `Statement` 금지 (SQL Injection 방지)
- XSS 방지: JSP 출력 시 `<c:out>` 또는 `fn:escapeXml()` 사용
- 비밀번호: 반드시 `BCryptUtil.hash()` 후 DB 저장, 평문 저장 금지
- DB 자원: 사용 후 반드시 `DBUtil.close(conn, pstmt, rs)` 호출

## URL 패턴
| 패턴 | 설명 |
|------|------|
| `/user/*` | 로그인 불필요 (로그인, 회원가입, 로그아웃) |
| `/user/pwd-reset/*` | 로그인 불필요 (비밀번호 재설정 이메일 요청 · 토큰 검증) |
| `/l_check/*` | 로그인 필수 (LoginCheckFilter 자동 적용) |
| `/admin/*` | ADMIN 역할 필수 (LoginCheckFilter `/admin/*` 매핑 추가됨) |

## 필터 실행 순서 (web.xml)
```
1. EncodingFilter   → UTF-8 인코딩 (모든 요청)
2. JandiFilter      → 로그인 유저의 잔디 데이터 주입 (정적 리소스·비로그인 자동 통과)
3. LoginCheckFilter → /l_check/*, /admin/* 접근 제어
```

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
