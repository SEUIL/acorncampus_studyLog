# Backend 구조 가이드 — acorncampus_studyLog

> Java EE 8 MVC2 패턴 | Oracle 19c | 세션 기반 인증

---

## 패키지 트리

```
com.acorncampus_studylog
├── controller/       ← @WebServlet, URL 라우팅
│   ├── MainController.java        / → 메인 페이지
│   ├── UserController.java        /user/* → 로그인/가입/비번변경
│   ├── MypageController.java      /l_check/user/* → 마이페이지/프로필
│   ├── PostController.java        /post/*, /l_check/post/* → 게시글 CRUD
│   ├── CommentController.java     /l_check/comment/* → 댓글 AJAX
│   ├── TagController.java         /tag/* → 태그 클라우드
│   ├── SeriesController.java      /series/*, /l_check/series/* → 시리즈
│   ├── SearchController.java      /search.do → 키워드 검색
│   ├── LikeController.java        /l_check/like/* → 좋아요 AJAX
│   ├── ReportController.java      /l_check/report/* → 신고 AJAX
│   └── AdminController.java       /admin/* → 관리자 전체
│
├── service/          ← 비즈니스 로직 (팀원이 TODO 채워넣는 곳)
│   ├── UserService.java
│   ├── PostService.java
│   ├── CommentService.java
│   ├── TagService.java
│   ├── SeriesService.java
│   ├── LikeService.java
│   └── ReportService.java
│
├── dao/              ← SQL 쿼리 (PreparedStatement, 완전 구현됨)
│   ├── UserDao.java
│   ├── PostDao.java
│   ├── CommentDao.java
│   ├── TagDao.java
│   ├── SeriesDao.java
│   ├── LikeDao.java
│   └── ReportDao.java
│
├── dto/              ← 데이터 전송 객체
│   ├── UserDto.java          ← 세션 저장용 (수정 금지)
│   ├── UserDetailDto.java    ← DB 조회/수정용 (비밀번호 포함)
│   ├── PostDto.java
│   ├── CommentDto.java
│   ├── TagDto.java
│   ├── SeriesDto.java
│   ├── ReportDto.java
│   └── PageDto.java          ← 페이지네이션 헬퍼
│
├── filter/
│   ├── EncodingFilter.java   ← 모든 요청 UTF-8
│   └── LoginCheckFilter.java ← /l_check/*, /admin/* 인증/권한
│
└── util/
    ├── DBUtil.java       ← getConnection(), close()
    └── BCryptUtil.java   ← hash(), check()
```

---

## URL → Controller → Service → DAO 흐름

| URL | Controller | Service | DAO |
|-----|-----------|---------|-----|
| `GET /` | MainController | PostService, TagService | PostDao, TagDao |
| `POST /user/login.do` | UserController | UserService | UserDao |
| `POST /user/reg.do` | UserController | UserService | UserDao |
| `GET /post/list.do` | PostController | PostService | PostDao |
| `GET /post/detail.do?id=N` | PostController | PostService, CommentService | PostDao, CommentDao, TagDao |
| `POST /post/write.do` | PostController | PostService, TagService | PostDao, TagDao |
| `POST /l_check/comment/write.do` | CommentController | CommentService | CommentDao |
| `POST /l_check/like/post.do` | LikeController | LikeService | LikeDao |
| `GET /search.do?q=...` | SearchController | PostService | PostDao |
| `GET /admin/main.do` | AdminController | UserService, PostService, ReportService | UserDao, PostDao, ReportDao |

---

## 공통 코딩 규칙

### 1. DB 쿼리 — PreparedStatement만 사용
```java
// GOOD
pstmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
pstmt.setString(1, email);

// BAD (절대 금지 — SQL Injection 위험)
stmt.executeQuery("SELECT * FROM users WHERE email = '" + email + "'");
```

### 2. 비밀번호 — 반드시 BCrypt
```java
// 회원가입: 저장 전 해시
String hashed = BCryptUtil.hash(rawPassword);

// 로그인: 검증
boolean ok = BCryptUtil.check(rawPassword, hashedFromDb);
```

### 3. DB 연결 — 사용 후 반드시 close
```java
Connection conn = null;
PreparedStatement pstmt = null;
ResultSet rs = null;
try {
    conn = DBUtil.getConnection();
    // ... 쿼리
} catch (SQLException e) {
    throw new RuntimeException("설명", e);
} finally {
    DBUtil.close(conn, pstmt, rs); // 항상 호출
}
```

### 4. 세션 처리
```java
// 로그인 시 저장
session.setAttribute("loginUser", userDetailDto.toSessionDto());

// 컨트롤러에서 조회
UserDto loginUser = (UserDto) session.getAttribute("loginUser");
if (loginUser == null) { resp.sendRedirect(contextPath + "/user/login.do"); return; }

// 로그아웃
session.invalidate();
```

### 5. JSP 출력 — XSS 방지
```jsp
<%-- 반드시 c:out 또는 fn:escapeXml 사용 --%>
<c:out value="${post.title}" />
<p>${fn:escapeXml(post.content)}</p>
```

### 6. AJAX 응답 — JSON 형식
```java
resp.setContentType("application/json; charset=UTF-8");
resp.getWriter().write("{\"status\":\"ok\"}");
```

---

## 필터 적용 범위

| URL 패턴 | 필터 | 동작 |
|---------|------|------|
| `/*` | EncodingFilter | 모든 요청 UTF-8 인코딩 |
| `/l_check/*` | LoginCheckFilter | 비로그인 → /user/login.do 리다이렉트 |
| `/admin/*` | LoginCheckFilter | 비로그인 → /user/login.do / 비관리자 → / 리다이렉트 |

---

## 페이지네이션 사용법

```java
// Service 코드 예시
public List<PostDto> getPostList(int pageNo) {
    int pageSize = 10;
    int totalCount = postDao.countAll();
    PageDto page = new PageDto(pageNo, pageSize, totalCount);
    return postDao.findAll(page.getOffset(), pageSize);
}

public PageDto getPostPage(int pageNo) {
    int totalCount = postDao.countAll();
    return new PageDto(pageNo, 10, totalCount);
}
```

```java
// Controller 코드 예시
int page = Integer.parseInt(req.getParameter("page") != null ? req.getParameter("page") : "1");
req.setAttribute("posts", postService.getPostList(page));
req.setAttribute("page",  postService.getPostPage(page));
req.getRequestDispatcher("/WEB-INF/views/post/list.jsp").forward(req, resp);
```

---

## DB 접속 정보

```
URL:  jdbc:oracle:thin:@localhost:1521:xe
User: blog
Pass: blog1234
```

> 초기 DB 세팅: `src/main/resources/schema.sql` 실행

---

## 작업 분담 가이드 (예시)

| 담당자 | 작업 범위 | 주요 파일 |
|--------|---------|---------|
| A | 회원 기능 | UserService, UserController, MypageController |
| B | 게시글 기능 | PostService, PostController |
| C | 댓글 + 좋아요/신고 | CommentService, LikeService, ReportService, CommentController, LikeController, ReportController |
| D | 태그 + 검색 + 시리즈 | TagService, SeriesService, TagController, SeriesController, SearchController |
| E | 관리자 | AdminController (UserService, PostService, CommentService, ReportService, TagService 재사용) |

> Service 메서드 내부의 `// TODO` 주석을 채워넣는 방식으로 작업

---

## View 경로 규칙

```
WEB-INF/views/
├── main.jsp                   ← 메인 페이지
├── user/
│   ├── login.jsp
│   ├── register.jsp
│   ├── mypage.jsp
│   ├── update.jsp
│   └── password.jsp
├── post/
│   ├── list.jsp
│   ├── detail.jsp
│   ├── write.jsp
│   └── update.jsp
├── tag/
│   ├── list.jsp               ← 태그 클라우드
│   └── posts.jsp              ← 태그별 게시글
├── series/
│   ├── list.jsp
│   ├── detail.jsp
│   ├── write.jsp
│   └── update.jsp
├── search/
│   └── result.jsp
├── admin/
│   ├── main.jsp               ← 대시보드
│   ├── user/list.jsp
│   ├── post/list.jsp
│   ├── comment/list.jsp
│   ├── report/list.jsp
│   └── tag/list.jsp
└── common/
    ├── header.jsp
    └── footer.jsp
```
