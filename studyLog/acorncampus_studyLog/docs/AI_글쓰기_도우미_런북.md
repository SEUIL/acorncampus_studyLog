# AI 글쓰기 도우미 설정 및 테스트 런북

> 작성일: 2026-05-11
> 최종 업데이트: 2026-05-11
> 대상: AI 글쓰기 도우미 설정, 운영, 테스트를 맡는 팀원

## 현재 상태

AI 글쓰기 도우미는 백엔드, 프론트 연결, 실제 OpenAI 호출, 실제 DB 사용 로그 기록까지 완료된 상태다.

- `/l_check/ai/assist.do` POST JSON API 구현 완료
- 서버 사이드 OpenAI 호출 구현 완료
- `ai_usage_log` 기반 사용 로그와 15초 쿨다운 구현 완료
- 게시글 작성/수정 화면 `Ctrl+Space` AI 모달 구현 완료
- 미리보기 후 Apply 방식 구현 완료
- 실제 OpenAI API 스모크 테스트 통과
- 실제 DB `ai_usage_log` `SUCCESS` 기록 확인
- Tomcat/IDE 실행 환경에서 브라우저 UI 확인 완료

## 구현 구성

| 영역 | 파일 | 역할 |
|------|------|------|
| Controller | `src/main/java/com/acorncampus_studylog/controller/AiController.java` | `/l_check/ai/*` 라우팅 |
| Endpoint | `src/main/java/com/acorncampus_studylog/controller/AiAssistEndpoint.java` | 로그인 확인, 8KB 본문 제한, JSON 응답 |
| Service | `src/main/java/com/acorncampus_studylog/service/ai/AiWritingService.java` | 입력 검증, 15초 쿨다운, 프롬프트 생성, 사용 로그 처리 |
| OpenAI Client | `src/main/java/com/acorncampus_studylog/service/ai/OpenAiWritingClient.java` | Responses API 호출, 600 token 출력 제한 |
| Config | `src/main/java/com/acorncampus_studylog/util/OpenAiConfig.java` | `openai.properties` 로딩 |
| DAO | `src/main/java/com/acorncampus_studylog/dao/AiUsageLogDao.java` | `ai_usage_log` 저장과 최근 사용 조회 |
| Schema | `src/main/resources/schema.sql` | `ai_usage_log_seq`, `ai_usage_log`, `idx_ai_usage_user_requested` |
| JSP | `src/main/webapp/WEB-INF/views/post/write.jsp` | AI 모달, 단축키, 미리보기, Apply 처리 |
| CSS | `src/main/webapp/resources/css/pages/post/post_write.css` | post write 전용 AI 모달 스타일 |
| JS | `src/main/webapp/resources/js/milkdown-editor.js` | AI 결과 적용용 `setMarkdown()` 포함 |

## 로컬 설정

`src/main/resources/openai.properties.example`을 복사해 `src/main/resources/openai.properties`를 만든다.

```properties
openai.api.key=YOUR_OPENAI_API_KEY
openai.model=gpt-4o-mini
openai.timeout.seconds=25
```

실제 키는 로컬 `openai.properties`에만 넣는다. 이 파일은 `.gitignore`에 등록되어 있으므로 커밋하지 않는다. 문서, JSP, JS, HTML, 테스트 출력에도 실제 키를 적지 않는다.

`openai.model`은 서버에서만 읽는다. 클라이언트는 모델, temperature, token 값을 선택할 수 없다. 이 기능 범위에서는 `gpt-4o-mini`를 비용 대비 기본 권장 모델로 둔다.

## 제한값

| 항목 | 제한 | 위치 |
|------|------|------|
| 초안 본문 | `draftText` 최대 3,000자 | `AiWritingService.MAX_DRAFT_TEXT_CHARS` |
| 직접 요청 | `customPrompt` 최대 500자 | `AiWritingService.MAX_CUSTOM_PROMPT_CHARS` |
| 요청 본문 | 최대 8KB | `AiController.MAX_REQUEST_BODY_BYTES` |
| OpenAI 출력 | 최대 600 tokens | `OpenAiWritingClient.MAX_OUTPUT_TOKENS` |
| 쿨다운 | 사용자별 15초 | `AiWritingService.COOLDOWN_SECONDS` |

추가 시간별, 일별, 월별, 전역 요청 수 제한은 없다. 현재 범위는 입력 크기 제한, 출력 제한, 사용자별 15초 쿨다운이다.

## API 동작

요청 경로는 로그인 필수인 `/l_check/ai/assist.do`다. `loginUser` 세션의 `UserDto`가 사용자 식별 기준이며, 클라이언트에서 userId를 보내지 않는다.

지원 작업은 다음 6개다.

| action | 설명 |
|--------|------|
| `IMPROVE` | 문장 다듬기 |
| `SUMMARY` | 요약 |
| `EXPAND` | 늘려쓰기 |
| `TITLE` | 제목 추천 |
| `TAGS` | 태그 추천 |
| `CUSTOM` | 직접 요청 |

`CUSTOM`은 `customPrompt`가 필수다. 모든 요청은 `draftText`가 필요하다.

## 프론트 동작

게시글 작성/수정 화면에서만 동작한다.

1. 로그인 후 게시글 작성/수정 페이지에 진입한다.
2. `Ctrl+Space`를 누르거나 `AI 도우미` 버튼을 누른다.
3. AI 작업을 선택한다.
4. `실행` 버튼을 누르면 `/l_check/ai/assist.do`로 JSON 요청을 보낸다.
5. 결과는 미리보기 영역에만 표시된다.
6. 사용자가 `Apply`를 눌렀을 때만 제목, 태그, 본문 중 해당 대상에 반영된다.

오류가 나거나 쿨다운이 걸려도 기존 초안은 변경하지 않는다. `TITLE`은 제목 입력값에, `TAGS`는 태그 입력값에, 나머지 작업은 Milkdown 본문에 적용한다.

## 테스트 명령

프로젝트 폴더는 `studyLog/acorncampus_studyLog`다.

```bash
mvnw.cmd -q -DskipTests compile
mvnw.cmd -q -Dtest=Ai*Test test
mvnw.cmd -q -DskipTests package
```

위 명령은 실제 OpenAI API를 호출하지 않는다. 로컬 키가 없어도 통과해야 한다.

실제 API 스모크 테스트는 명시적으로 켠 경우에만 실행한다.

```powershell
.\mvnw.cmd -q -Dtest=OpenAiRealApiTest "-Dopenai.realTest=true" test
```

Windows PowerShell에서는 점이 포함된 Maven 속성을 따옴표로 감싼다. 스모크 테스트 실행 조건은 두 가지다. `-Dopenai.realTest=true`가 있어야 하고, 무시되는 `src/main/resources/openai.properties`에 `openai.api.key`가 있어야 한다. 둘 중 하나라도 없으면 테스트는 호출 없이 skip된다.

## 실제 검증 기록

2026-05-11 기준 다음 검증을 완료했다.

| 검증 | 결과 |
|------|------|
| `OpenAiRealApiTest` 실제 OpenAI 호출 | 통과 |
| `Ai*Test` 회귀 테스트 | 통과 |
| `mvnw.cmd -q -DskipTests package` | 통과 |
| 실제 Oracle DB `ai_usage_log` 기록 | `SUCCESS` 행 생성 확인 |
| Tomcat/IDE 브라우저 UI 확인 | 완료 |

실제 DB 테스트에서는 `AiWritingService` 호출 후 `ai_usage_log`에 `PENDING`이 생성되고, OpenAI 호출 성공 뒤 `SUCCESS`로 갱신되는 흐름을 확인했다.

## 비밀값 취급

실제 API 키는 저장소에 넣지 않는다. 확인할 때는 docs와 CLAUDE.md에 OpenAI 키 접두어 형태의 문자열이 없는지 확인한다.

```bash
git grep -n "OPENAI_API_KEY" -- studyLog/acorncampus_studyLog/docs studyLog/acorncampus_studyLog/CLAUDE.md
```

실제 키 값이 문서에 노출되지 않았는지도 커밋 전 diff에서 함께 확인한다.
