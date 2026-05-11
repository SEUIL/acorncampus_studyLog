# AI 글쓰기 도우미 설정 및 테스트 런북

> 작성일: 2026-05-11  
> 대상: AI 글쓰기 도우미 백엔드 설정, 테스트, 다음 프론트 작업을 맡는 팀원

## 현재 상태

AI 글쓰기 도우미는 백엔드 우선 범위가 완료된 상태다. `/l_check/ai/assist.do` POST JSON API, OpenAI 설정 로더, 클라이언트, 서비스, 사용 로그 DAO, 테스트가 준비되어 있다.

프론트 모달과 화면 연결은 아직 완료로 표시하지 않는다. `Ctrl+Space`로 여는 모달, 미리보기, Apply 동작은 사용자 승인 후 JSP, CSS, JS 파일을 정해 진행해야 한다.

## 구현된 백엔드 구성

| 영역 | 파일 | 역할 |
|------|------|------|
| Controller | `src/main/java/com/acorncampus_studylog/controller/AiController.java` | `/l_check/ai/*` 라우팅 |
| Endpoint | `src/main/java/com/acorncampus_studylog/controller/AiAssistEndpoint.java` | 로그인 확인, 8KB 본문 제한, JSON 응답 |
| Service | `src/main/java/com/acorncampus_studylog/service/ai/AiWritingService.java` | 입력 검증, 15초 쿨다운, 프롬프트 생성, 사용 로그 처리 |
| OpenAI Client | `src/main/java/com/acorncampus_studylog/service/ai/OpenAiWritingClient.java` | Responses API 호출, 600 token 출력 제한 |
| Config | `src/main/java/com/acorncampus_studylog/util/OpenAiConfig.java` | `openai.properties` 로딩 |
| DAO | `src/main/java/com/acorncampus_studylog/dao/AiUsageLogDao.java` | `ai_usage_log` 저장과 최근 사용 조회 |
| Schema | `src/main/resources/schema.sql` | `ai_usage_log_seq`, `ai_usage_log`, `idx_ai_usage_user_requested` |

## 로컬 설정

`src/main/resources/openai.properties.example`을 복사해 `src/main/resources/openai.properties`를 만든다.

```properties
openai.api.key=YOUR_OPENAI_API_KEY
openai.model=gpt-5.4-mini
openai.timeout.seconds=25
```

실제 키는 로컬 `openai.properties`에만 넣는다. 이 파일은 `.gitignore`에 등록되어 있으므로 커밋하지 않는다. 문서, JSP, JS, HTML, 테스트 출력에도 실제 키를 적지 않는다.

`openai.model`과 `openai.timeout.seconds`는 비워 두면 기본값을 쓴다. 기본 모델은 `gpt-5.4-mini`, 기본 타임아웃은 25초다.

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

## 프론트 작업 전 확인

프론트는 아직 구현 완료 상태가 아니다. 작업 전 사용자에게 JSP, CSS, JS 수정 대상 파일을 보여 주고 승인을 받아야 한다.

요구사항은 다음 상태로 남아 있다.

| 항목 | 상태 |
|------|------|
| `Ctrl+Space` 단축키 | 미구현, 승인 후 연결 |
| AI 모달 | 미구현, 승인 후 작성 |
| 결과 미리보기 | 미구현, 승인 후 작성 |
| Apply 버튼으로 명시 적용 | 미구현, 승인 후 작성 |

프론트 구현 시 AI 결과를 자동 적용하지 않는다. 오류가 나도 사용자의 기존 초안은 보존해야 한다.

## 테스트 명령

프로젝트 폴더는 `studyLog/acorncampus_studyLog`다.

```bash
mvnw.cmd -q -DskipTests compile
mvnw.cmd -q -Dtest=Ai*Test test
```

위 명령은 실제 OpenAI API를 호출하지 않는다. 로컬 키가 없어도 통과해야 한다.

실제 API 스모크 테스트는 명시적으로 켠 경우에만 실행한다.

```bash
mvnw.cmd -q -Dtest=OpenAiRealApiTest -Dopenai.realTest=true test
```

Windows PowerShell에서는 점이 포함된 Maven 속성을 따옴표로 감싼다.

```powershell
.\mvnw.cmd -q -Dtest=OpenAiRealApiTest "-Dopenai.realTest=true" test
```

스모크 테스트 실행 조건은 두 가지다. `-Dopenai.realTest=true`가 있어야 하고, 무시되는 `src/main/resources/openai.properties`에 `openai.api.key`가 있어야 한다. 둘 중 하나라도 없으면 테스트는 호출 없이 skip된다.

## 비밀값 취급

실제 API 키는 저장소에 넣지 않는다. 확인할 때는 다음 명령으로 docs와 CLAUDE.md에 키 형태 문자열이 없는지 확인한다.

```bash
git grep -n "sk-" -- studyLog/acorncampus_studyLog/docs studyLog/acorncampus_studyLog/CLAUDE.md
```

결과가 없으면 문서에 OpenAI 키 패턴이 노출되지 않은 상태다.
