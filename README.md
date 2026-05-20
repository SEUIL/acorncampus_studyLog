# StudyLog - 나만의 학습 기록 블로그 플랫폼

> Java Servlet MVC2와 Oracle DB 기반으로 구현한 학습 기록 블로그 플랫폼입니다. 글 작성, 시리즈 관리, 태그 검색, 댓글/좋아요/신고, 관리자 운영 기능에 AI 글쓰기 도우미를 결합해 개인 학습 기록을 지속적으로 축적하고 공유할 수 있도록 설계했습니다.

## 프로젝트 개요

StudyLog는 개발자와 학습자가 공부한 내용을 Markdown 기반 게시글로 정리하고, 태그와 시리즈로 체계화할 수 있는 커뮤니티형 기록 서비스입니다. 단순 CRUD 게시판을 넘어 세션 기반 인증, 관리자 운영 도구, 신고 처리, 비밀번호 재설정, OpenAI 기반 글쓰기 보조 기능까지 포함한 Java EE 웹 애플리케이션입니다.

| 항목 | 내용 |
|---|---|
| 프로젝트 유형 | 팀 사이드 프로젝트 / MVC2 웹 애플리케이션 |
| 현재 상태 | 2026.05 기준 주요 기능 구현 완료 |
| 핵심 목적 | 학습 기록 작성, 분류, 공유, 커뮤니티 상호작용 지원 |
| Backend | Java 17, Java EE 8 Servlet/JSP, Maven |
| Frontend | JSP, JSTL, 계층형 CSS, Vanilla JS, Milkdown Editor |
| Database | Oracle 19c, JDBC, PreparedStatement |
| 인증 | Session 기반 인증, BCrypt 비밀번호 해싱 |
| AI | OpenAI API 기반 글쓰기 도우미 |

## 팀 구성 및 역할

팀원별 담당 업무는 `docs/분업/분업_가이드.md`의 초기 분업 기준을 바탕으로 정리했습니다.

| 이름 | 담당 영역 | 주요 역할 |
|---|---|---|
| 철진 | 게시글 / 검색 / 태그 / 공통 JSP | 게시글·태그·검색 관련 Service/Controller 구현, 커뮤니티·게시글 목록·시리즈 목록 JSP 변환, 공통 header/footer 구성 |
| 현겸 | 댓글 / 좋아요 / 신고 | 댓글 트리 구조, 게시글·댓글 좋아요/싫어요, 신고 접수 AJAX 흐름, 게시글 상세·작성 화면 JSP 연결 |
| 건희 | 시리즈 / 관리자 | 시리즈 CRUD, 관리자 대시보드와 회원·게시글·신고·태그 관리 화면, 관리자 처리 AJAX 흐름 구현 |
| 진희 | 회원 기능 | 로그인·회원가입, 마이페이지·프로필 수정, 비밀번호 변경/탈퇴 등 회원 Service/Controller와 사용자 JSP 구현 |
| 스일(팀장) | AI 기능 / 통합 관리 | AI 글쓰기 도우미 개발, 팀원 PR 코드 리뷰, 공통 오류 해결 지원, 최종 통합 및 배포 관리 |

## 주요 기능

### 회원 및 인증

- 이메일/닉네임 기반 회원가입과 로그인
- BCrypt 기반 비밀번호 해싱
- 세션 기반 로그인 상태 관리
- 마이페이지, 프로필 수정, 공개 프로필 조회
- 이메일 토큰 기반 비밀번호 재설정
- 관리자 권한과 일반 사용자 권한 분리

### 학습 기록 게시글

- Markdown 기반 게시글 작성/수정/삭제
- Milkdown Editor 기반 작성 화면
- 게시글 목록, 상세 조회, 조회수 증가
- 썸네일 및 이미지 업로드
- 공개/비공개 게시글 처리
- 제목/본문 기반 검색과 페이지네이션

### 태그와 시리즈

- 게시글 태그 등록 및 태그별 글 조회
- 태그 클라우드와 태그 검색
- 사용 빈도, 최신순, 이름순 태그 정렬
- 시리즈 생성/수정/삭제
- 시리즈 상세 페이지에서 연관 게시글 관리

### 커뮤니티 상호작용

- 댓글과 대댓글 작성/수정/삭제
- 게시글/댓글 좋아요 및 싫어요
- 중복 신고 방지 기반 신고 접수
- 신고 대상, 처리 상태, 처리자 정보를 포함한 신고 관리

### 관리자 기능

- 관리자 대시보드
- 회원 관리: 차단, 해제, 강제 삭제
- 게시글 관리
- 댓글 관리
- 신고 접수 및 처리
- 태그 관리
- `/admin/*` 요청에 대한 관리자 권한 필터링

### AI 글쓰기 도우미

- 게시글 작성/수정 화면에서 `Ctrl+Space`로 AI 도우미 실행
- OpenAI API 서버 사이드 호출
- 지원 작업: 문장 개선, 요약, 확장, 제목 추천, 태그 추천, 사용자 지정 요청
- AI 결과를 바로 덮어쓰지 않고 미리보기 후 Apply 방식으로 반영
- 사용자별 15초 쿨다운
- `ai_usage_log` 테이블 기반 사용 로그 기록
- 요청 크기, 초안 길이, 출력 토큰 제한 적용

## 기술적 특징

### Servlet MVC2 구조

`@WebServlet` Controller가 URL 라우팅과 View 포워딩을 담당하고, Service가 비즈니스 로직을 처리하며, DAO가 Oracle SQL을 직접 수행하는 전통적인 MVC2 구조입니다.

```text
Controller -> Service -> DAO -> Oracle DB
     |                       |
     v                       v
   JSP View              DTO / SQL
```

주요 패키지 구조는 다음과 같습니다.

```text
com.acorncampus_studylog
├── controller/   # @WebServlet 기반 URL 라우팅
├── service/      # 비즈니스 로직
├── dao/          # JDBC PreparedStatement 기반 Oracle SQL
├── dto/          # 요청/응답 및 화면 전달 데이터
├── filter/       # 인코딩, 인증/권한, 잔디 데이터 주입
└── util/         # DB, BCrypt, Mail, OpenAI 설정 유틸
```

### 인증과 권한 처리

- `EncodingFilter`: 모든 요청 UTF-8 처리
- `LoginCheckFilter`: `/l_check/*`, `/admin/*` 접근 제어
- `JandiFilter`: 로그인 사용자 대상 기여도 그래프 데이터 주입
- 관리자 페이지는 `ADMIN` 역할만 접근 가능하도록 분리

### 데이터베이스 설계

Oracle 19c 기준 DDL은 `studyLog/acorncampus_studyLog/src/main/resources/schema.sql`에 정리되어 있습니다.

주요 테이블은 다음과 같습니다.

| 테이블 | 역할 |
|---|---|
| `users` | 회원, 권한, 차단, 소프트 삭제 정보 |
| `posts` | 게시글 본문, 공개 여부, 조회수, 썸네일 |
| `series` | 사용자별 시리즈 |
| `tags`, `post_tags` | 태그와 게시글 다대다 관계 |
| `comments` | 댓글과 대댓글 |
| `post_likes`, `comment_likes` | 게시글/댓글 좋아요 및 싫어요 |
| `reports` | 게시글/댓글 신고와 처리 상태 |
| `password_reset_tokens` | 비밀번호 재설정 토큰 |
| `ai_usage_log` | AI 글쓰기 도우미 사용 로그 |

### CSS 구조화

CSS는 전역, 컴포넌트, 페이지 단위의 3계층 구조로 분리했습니다. 주요 구조는 다음과 같습니다.

```text
resources/css/
├── global_theme.css
├── style.css
├── components/
│   ├── button.css
│   ├── form.css
│   ├── layout.css
│   ├── table.css
│   ├── milkdown.css
│   ├── series.css
│   ├── ui.css
│   ├── typography.css
│   ├── tabs.css
│   └── jandi.css
└── pages/
    ├── admin/
    ├── auth/
    ├── community/
    ├── post/
    ├── series/
    ├── user/
    └── workspace/
```

중복 스타일을 컴포넌트로 분리하고, 각 JSP에서 필요한 CSS를 명시적으로 로드하는 방식으로 유지보수성을 높였습니다.

## 화면 구성

주요 JSP 화면은 다음과 같습니다.

| 영역 | 화면 |
|---|---|
| 인증 | 로그인, 회원가입, 비밀번호 찾기, 비밀번호 재설정 |
| 커뮤니티 | 메인, 게시글 목록, 게시글 상세, 검색 결과 |
| 작성 | 게시글 작성/수정, AI 글쓰기 도우미 모달 |
| 사용자 | 마이페이지, 프로필 수정, 공개 프로필 |
| 시리즈 | 시리즈 목록, 상세, 작성/수정 |
| 관리자 | 대시보드, 회원 관리, 게시글 관리, 댓글 관리, 신고 관리, 태그 관리 |

디자인 산출물과 HTML 프로토타입은 `docs/frontEndDesign/` 아래에 보관되어 있습니다.

## 프로젝트 구조

```text
acorncampus_studyLog/
├── README.md
├── docs/                                  # 기획서, ERD, 화면 설계, 발표 자료
└── studyLog/acorncampus_studyLog/
    ├── pom.xml                            # Maven WAR 프로젝트 설정
    ├── src/main/java/com/acorncampus_studylog/
    │   ├── controller/
    │   ├── service/
    │   ├── dao/
    │   ├── dto/
    │   ├── filter/
    │   └── util/
    ├── src/main/resources/
    │   └── schema.sql
    ├── src/main/webapp/
    │   ├── WEB-INF/web.xml
    │   ├── WEB-INF/views/
    │   └── resources/
    └── src/test/java/com/acorncampus_studylog/
```

## 실행 방법

### 사전 준비

- Java 17
- Maven Wrapper 사용 가능 환경
- Oracle 19c 또는 호환 Oracle DB
- Servlet 4.0 지원 WAS, 예: Apache Tomcat 9

### DB 설정

`schema.sql` 기준으로 로컬 Oracle 계정과 테이블을 생성합니다. 아래 계정과 권한은 로컬 실습용 예시이며, 운영 환경에서는 최소 권한 계정과 외부화된 설정을 사용해야 합니다.

```sql
CREATE USER blog IDENTIFIED BY blog1234;
GRANT CONNECT, RESOURCE, DBA TO blog;
```

그 다음 `studyLog/acorncampus_studyLog/src/main/resources/schema.sql`을 `blog` 계정으로 실행합니다.

애플리케이션의 기본 JDBC 접속 정보는 다음 값으로 구현되어 있습니다.

```text
jdbc:oracle:thin:@localhost:1521:testdb
user: blog
```

### 로컬 설정 파일

메일 발송과 OpenAI 연동을 사용하려면 로컬 전용 설정 파일이 필요합니다. 실제 자격증명은 저장소에 커밋하지 않습니다.

```text
studyLog/acorncampus_studyLog/src/main/resources/mail.properties
studyLog/acorncampus_studyLog/src/main/resources/openai.properties
```

메일 설정 예시는 다음과 같습니다.

```properties
mail.host=smtp.gmail.com
mail.port=587
mail.user=YOUR_MAIL_ACCOUNT
mail.password=YOUR_APP_PASSWORD
mail.from=YOUR_MAIL_ACCOUNT
mail.app.baseUrl=http://localhost:8080/acorncampus_studyLog
```

OpenAI 설정 예시는 다음과 같습니다.

```properties
openai.api.key=YOUR_OPENAI_API_KEY
openai.model=gpt-4o-mini
openai.timeout.seconds=25
```

### 빌드

Windows PowerShell 기준:

```bash
cd studyLog/acorncampus_studyLog
./mvnw.cmd -q -DskipTests package
```

테스트 제외 컴파일만 확인하려면 다음 명령을 사용합니다.

```bash
./mvnw.cmd -q -DskipTests compile
```

### 테스트

AI 글쓰기 도우미의 단위 테스트는 실제 OpenAI 네트워크 호출 없이 실행됩니다.

```bash
./mvnw.cmd -q "-Dtest=AiControllerTest,AiWritingServiceTest,OpenAiWritingClientTest" test
```

DAO 통합 테스트는 Oracle DB와 `schema.sql` 기반 테이블이 준비된 상태에서 실행합니다.

```bash
./mvnw.cmd -q -Dtest=DaoIntegrationTest test
```

실제 OpenAI API 스모크 테스트는 명시 플래그가 있을 때만 실행합니다.

```bash
./mvnw.cmd -q -Dtest=OpenAiRealApiTest "-Dopenai.realTest=true" test
```

## 포트폴리오 관점의 구현 포인트

- 프레임워크 자동화에 의존하지 않고 Servlet MVC2 흐름을 직접 설계해 HTTP 요청, 세션, 필터, JSP 포워딩 구조를 구현했습니다.
- DAO 계층에서 Oracle SQL과 `PreparedStatement`를 직접 사용해 CRUD, 검색, 페이지네이션, 신고/좋아요 상태 처리를 구현했습니다.
- 로그인 필수 URL과 관리자 URL을 필터 계층에서 분리해 인증/권한 책임을 중앙화했습니다.
- 글쓰기 경험 개선을 위해 Milkdown Editor와 OpenAI API를 결합하고, 미리보기 후 적용 방식으로 사용자 초안 손실을 방지했습니다.
- AI 사용 로그와 쿨다운을 DB에 기록해 기능 사용량 추적과 비용 제어의 기반을 마련했습니다.
- CSS를 전역/컴포넌트/페이지 계층으로 분리해 JSP 기반 프로젝트에서도 재사용 가능한 스타일 구조를 구성했습니다.
- 신고, 차단, 관리자 처리 기능을 포함해 실제 커뮤니티 운영에 필요한 관리 흐름을 구현했습니다.
- 팀 프로젝트 산출물 기준으로 기능별 문서, ERD, 화면 설계, Backend 구조 가이드를 함께 관리해 협업과 인수인계를 고려했습니다.

## 참고 문서

| 문서 | 내용 |
|---|---|
| `docs/notion/기획서...md` | 프로젝트 기획 배경과 요구사항 |
| `docs/notion/ERD...md` | ERD 및 테이블 설계 |
| `docs/frontEndDesign/` | 화면 설계와 HTML 프로토타입 |
| `studyLog/acorncampus_studyLog/docs/BACKEND구조_가이드.md` | Backend 구조와 URL 흐름 |
| `studyLog/acorncampus_studyLog/docs/AI_글쓰기_도우미_런북.md` | AI 글쓰기 도우미 설정 및 테스트 |
| `studyLog/acorncampus_studyLog/src/main/resources/schema.sql` | Oracle DDL |

## 현재 상태

2026년 5월 기준 문서와 코드상 Controller, Service, DAO, JSP 주요 기능 구현이 완료된 상태입니다. Maven 기반 build와 JUnit 5 테스트 구성이 포함되어 있으며, Tomcat 환경에서 WAR로 실행하는 Java EE 웹 프로젝트입니다.
