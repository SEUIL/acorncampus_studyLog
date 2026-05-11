# AI Writing Assistant for Post Editor

## TL;DR
> **Summary**: Add a server-side OpenAI writing assistant to the Milkdown-based post write page, opened with `Ctrl+Space`, with fixed actions plus custom prompt and preview-before-apply UX. The implementation must cap input/output size, enforce a 15-second per-user cooldown, keep the API key server-side in an ignored properties file, and include an opt-in real API smoke test.
> **Deliverables**:
> - `/l_check/ai/assist.do` authenticated JSON endpoint
> - OpenAI client/config/service layer using server-side `openai.properties`
> - `ai_usage_log` Oracle table + DAO/DTO/service usage tracking
> - Post write AI modal, shortcut, preview, and apply behavior
> - Mock/logic tests plus guarded real OpenAI smoke test
> **Effort**: Medium
> **Parallel**: YES - 3 waves
> **Critical Path**: Sync latest `origin/develop` (#76) → Task 1 → Task 2 → Task 3 → Task 4 → Task 5 → Task 6 → Task 7

## Context
### Original Request
- Use OpenAI API; API key is already prepared.
- Recommend the best model for this feature.
- On post writing, pressing `Ctrl+Space` opens an AI modal.
- User writes in the modal and selects an action; AI performs the requested writing assistance.
- Main concern: users may request complex or large text that consumes too many tokens, so the feature needs restrictive usage controls.

### Interview Summary
- Availability: all logged-in users, because this project is not planned for deployment.
- AI actions: 5 fixed actions plus custom prompt.
  - `IMPROVE`: 문장 다듬기
  - `SUMMARY`: 요약
  - `EXPAND`: 늘려쓰기
  - `TITLE`: 제목 추천
  - `TAGS`: 태그 추천
  - `CUSTOM`: 직접 요청
- Result behavior: AI output is shown in a preview area first; user must explicitly click Apply before any field changes.
- Usage limits: `draftText` max 3,000 characters, `customPrompt` max 500 characters, request body max 8 KB, OpenAI output max 600 tokens, cooldown 15 seconds per user.
- No hourly, daily, monthly, or global request-count quotas.
- Usage storage: DB-backed `ai_usage_log` for cooldown, status, and audit.
- API key storage: ignored `src/main/resources/openai.properties`, with `.gitignore` update.
- Tests: include real OpenAI API smoke test, but make it opt-in/property-gated so normal test runs never spend tokens accidentally.
- Latest develop safety check: before execution, re-check current editor/frontend state because teammates changed post editor code after initial planning.
- Frontend owner constraint: all frontend changes must follow `studyLog/acorncampus_studyLog/src/main/webapp/resources/css/CSS_ARCHITECTURE.md`, and executor must get explicit user approval before modifying JSP/CSS/JS.
- PR #76 latest check: `origin/develop` is `8714d01 Merge pull request #76 from SEUIL/feature/JCJ/front`; current working branch was still at PR #75 when this plan was updated. PR #76 changes post/series list search/sort mapping and `ui.css`, not Milkdown files, but implementer must sync it before coding.

### Metis Review (gaps addressed)
- Add explicit JSON `401` handling in the AI servlet even though `/l_check/*` is filter-protected, because AJAX redirects are poor UX.
- Cooldown counts recent `PENDING` and `SUCCESS` rows to prevent double-submit abuse; `FAILED` counts only when the OpenAI call was actually attempted.
- OpenAI failures never modify the draft; modal shows an error and preserves textarea content.
- `TITLE` and `TAGS` actions return structured/plain preview; they must not auto-fill fields before Apply.
- Normal Maven tests must not call OpenAI unless an explicit system property is supplied.
- Keep scope to post write only; do not retrofit series/profile editors.

## Work Objectives
### Core Objective
Implement a safe, authenticated AI writing assistant for the Milkdown-based `post/write.jsp` that uses OpenAI server-side and prevents oversized requests while preserving the user's draft unless they explicitly apply the AI result.

### Deliverables
- `openai.properties` loader and `.gitignore` protection.
- OpenAI client wrapper and service/prompt builder.
- Usage-log schema, DTO, DAO, and cooldown service methods.
- `AiController` under `/l_check/ai/*` returning Gson JSON.
- AI modal UI and JS behavior on `post/write.jsp`.
- Tests for validation/cooldown/prompt behavior and opt-in real API smoke coverage.

### Definition of Done (verifiable conditions with commands)
- `studyLog/acorncampus_studyLog/mvnw.cmd -q -DskipTests compile` succeeds.
- Normal tests do not call OpenAI: `studyLog/acorncampus_studyLog/mvnw.cmd -q -Dtest=Ai*Test test` succeeds without a real key.
- Real API smoke test is opt-in only: `studyLog/acorncampus_studyLog/mvnw.cmd -q -Dtest=OpenAiRealApiTest -Dopenai.realTest=true test` calls OpenAI only when `openai.properties` exists and the property is set.
- Browser QA: logged-in user opens write page, presses `Ctrl+Space`, runs each fixed action, sees preview, clicks Apply, and observes the expected field update.
- Oversized input/custom prompt returns JSON `400`; cooldown returns JSON `429`; missing login returns JSON `401`.

### Must Have
- Server-side API key only; no key in JSP, JS, HTML, or committed source.
- Fixed server-side model and token cap; client cannot choose model, temperature, or tokens.
- Gson JSON responses for all AI endpoint success/error responses.
- Existing session key `loginUser` remains the source of user identity.
- AI modal must be keyboard accessible and preserve the user's draft on all errors.
- Any frontend implementation task must pause before editing frontend files and ask the user for explicit approval, referencing the exact files to be changed.
- Frontend CSS must follow the 3-layer architecture: `global_theme.css` for global variables/base only, `resources/css/components/` for reusable components, and `resources/css/pages/post/` for post-page-only styles.
- Before any implementation, sync with `origin/develop` at or after commit `8714d01` and re-run compile so PR #76 mapping changes are included.

### Must NOT Have (guardrails, AI slop patterns, scope boundaries)
- Do not call OpenAI directly from browser JavaScript.
- Do not add hourly/daily/global request-count quotas; only 15-second cooldown plus input/output caps.
- Do not auto-apply AI results.
- Do not introduce Toast UI Editor; current post editor uses Milkdown (`milkdown-editor.js`, `milkdown-slash.js`, `components/milkdown.css`).
- Do not read/write AI content through stale textarea-only assumptions; use Milkdown helper APIs and preserve fallback textarea behavior.
- Do not modify frontend files without user approval after presenting the intended frontend file list.
- Do not store full prompts, full drafts, or AI responses in `ai_usage_log`.
- Do not make normal `mvn test` depend on API keys, network, or token spend.

## Verification Strategy
> ZERO HUMAN INTERVENTION - all verification is agent-executed.
- Test decision: tests-after + JUnit 5; real API test is opt-in/property-gated.
- QA policy: Every task has agent-executed scenarios.
- Evidence: `.sisyphus/evidence/task-{N}-{slug}.{ext}`

## Execution Strategy
### Pre-Execution Sync Gate
Before starting Task 1, the executor must update the working branch to include `origin/develop` at or after `8714d01 Merge pull request #76 from SEUIL/feature/JCJ/front`. Verify with:
- `git log -1 --oneline origin/develop` shows `8714d01` or newer.
- Current branch contains `8714d01` after merge/rebase.
- `studyLog/acorncampus_studyLog/mvnw.cmd -q -DskipTests compile` succeeds after resolving any conflicts.

Current session note: user reported PR #76 has been merged into the current branch and compile succeeds. Read-only git verification confirmed current HEAD `0d7bd07 Merge branch 'develop' into feature/seuil/ai` contains `8714d01`.

PR #76 impact summary for this plan:
- Changed files include `PostController.java`, `SeriesController.java`, `PostDao.java`, `SeriesDao.java`, `PostService.java`, `SeriesService.java`, `header.jsp`, `post/detail.jsp`, `post/list.jsp`, `series/list.jsp`, and `resources/css/components/ui.css`.
- It does not change `post/write.jsp`, `milkdown-editor.js`, `milkdown-slash.js`, or `components/milkdown.css`, so the AI editor integration remains Milkdown-based.
- Avoid modifying PR #76 search/sort/list mapping while implementing AI; keep AI backend in a new `AiController` and AI-specific service/DAO classes.
- PR #76 routing/navigation QA to preserve: `/community.do` breadcrumb target, keyword-aware post/series list tabs, series-aware post detail back link, and `.breadcrumb` styling for non-clickable `<span>` breadcrumbs.

### Parallel Execution Waves
> Target: 5-8 tasks per wave. <3 per wave (except final) = under-splitting.
> Extract shared dependencies as Wave-1 tasks for max parallelism.

Backend-first execution override requested by user:
- Run backend Tasks 1-5 first: config/dependency → schema/DAO/DTO → OpenAI client → AI service/limits → AI servlet endpoint.
- Run backend portions of Task 7 after Task 5: config tests, DAO/service tests, controller tests, and guarded real API smoke test.
- Do not start Task 6 frontend implementation until the executor presents the exact JSP/CSS/JS file list and receives explicit user approval.

Wave 1: Task 1 config/dependency, Task 2 schema/DAO/DTO
Wave 2: Task 3 OpenAI client, Task 4 AI domain service/cooldown, Task 5 servlet endpoint
Wave 3: Task 7 backend tests/real API smoke, then STOP for frontend approval before Task 6
Wave 4: Task 6 frontend completion after approval, Task 8 documentation/operational notes

### Dependency Matrix (full, all tasks)
| Task | Depends On | Blocks |
|------|------------|--------|
| 1 Config and dependencies | None | 3, 7, 8 |
| 2 Usage log persistence | None | 4, 5, 7 |
| 3 OpenAI client wrapper | 1 | 4, 7 |
| 4 AI domain service | 2, 3 | 5, 7 |
| 5 AI servlet endpoint | 2, 4 | 6, 7 |
| 6 Post write modal/frontend | 5 and explicit user frontend approval | 7 |
| 7 Tests and real API smoke | Backend portion: 1-5; full UI QA: 6 | 8 |
| 8 Docs and runbook updates | 1-7 | Final verification |

### Agent Dispatch Summary (wave → task count → categories)
| Wave | Task Count | Recommended Categories |
|------|------------|------------------------|
| 1 | 2 | quick, unspecified-high |
| 2 | 3 | unspecified-high, deep |
| 3 | 1 | unspecified-high |
| 4 | 2 | visual-engineering, writing |

## TODOs
> Implementation + Test = ONE task. Never separate.
> EVERY task MUST have: Agent Profile + Parallelization + QA Scenarios.

- [x] 1. Configure OpenAI Secret Loading and Dependencies

  **What to do**: Add OpenAI integration configuration without exposing secrets. Update `studyLog/acorncampus_studyLog/.gitignore` to ignore `src/main/resources/openai.properties`. Add a non-secret example file only if needed, named `src/main/resources/openai.properties.example`, containing placeholder keys only. Add the official OpenAI Java SDK dependency to `pom.xml` if compatible with Java 17 and this WAR project; if dependency resolution fails or servlet packaging conflicts arise, use Java 17 `java.net.http.HttpClient` with Gson instead. Implement `OpenAiConfig` in `com.acorncampus_studylog.util` or `service.ai` that reads `src/main/resources/openai.properties` from classpath with keys: `openai.api.key`, `openai.model`, `openai.timeout.seconds`. Defaults: model `gpt-5.4-mini`, timeout 25 seconds. Missing API key must fail with a clear server-side exception and client-safe error message.
  **Must NOT do**: Do not commit real `openai.properties`; do not read API key from request parameters; do not put the key into JSP/JS; do not edit `mail.properties` for OpenAI.

  **Recommended Agent Profile**:
  - Category: `quick` - Reason: localized config/dependency setup with clear constraints.
  - Skills: `[]` - No specialized skill required.
  - Omitted: [`visual-engineering`] - No UI work here.

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 3, 7, 8 | Blocked By: None

  **References** (executor has NO interview context - be exhaustive):
  - Pattern: `studyLog/acorncampus_studyLog/.gitignore:55-56` - existing ignored secret pattern for `mail.properties`.
  - Pattern: `studyLog/acorncampus_studyLog/pom.xml:62-67` - Gson dependency already exists for JSON.
  - Pattern: `studyLog/acorncampus_studyLog/pom.xml:76-88` - JUnit dependency style.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/resources/mail.properties` - ignored local secret file pattern; do not expose values.
  - External: `https://developers.openai.com/api/docs/libraries` - official library guidance.
  - External: `https://github.com/openai/openai-java` - official Java SDK.

  **Acceptance Criteria** (agent-executable only):
  - [ ] `git check-ignore -q "studyLog/acorncampus_studyLog/src/main/resources/openai.properties"` exits `0`.
  - [ ] No real API key appears in tracked files: `git grep -n "sk-" -- studyLog/acorncampus_studyLog` returns no matches.
  - [ ] `studyLog/acorncampus_studyLog/mvnw.cmd -q -DskipTests compile` succeeds.

  **QA Scenarios** (MANDATORY - task incomplete without these):
  ```
  Scenario: Secret file is ignored
    Tool: Bash
    Steps: Run `git check-ignore -q "studyLog/acorncampus_studyLog/src/main/resources/openai.properties"; if ($LASTEXITCODE -eq 0) { "ignored" } else { exit 1 }` from repo root.
    Expected: Command prints `ignored` and exits 0.
    Evidence: .sisyphus/evidence/task-1-secret-ignore.txt

  Scenario: Missing key fails safely
    Tool: Bash
    Steps: Temporarily run config-loading unit test without `openai.properties` or with placeholder-only example file.
    Expected: Test confirms server-side config throws a clear exception without leaking secrets to stdout.
    Evidence: .sisyphus/evidence/task-1-missing-key.txt
  ```

  **Commit**: NO | Message: `feat(ai): configure OpenAI secret loading` | Files: [`pom.xml`, `.gitignore`, `src/main/resources/openai.properties.example`, config class]

- [x] 2. Add AI Usage Log Persistence

  **What to do**: Add Oracle DDL for AI usage logging in `src/main/resources/schema.sql`. Add sequence `ai_usage_log_seq` and table `ai_usage_log` with fields: `usage_id NUMBER PRIMARY KEY`, `user_id NUMBER NOT NULL`, `action VARCHAR2(20) NOT NULL`, `status VARCHAR2(20) NOT NULL`, `input_chars NUMBER NOT NULL`, `custom_prompt_chars NUMBER DEFAULT 0 NOT NULL`, `max_output_tokens NUMBER NOT NULL`, `requested_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL`, `completed_at TIMESTAMP`, `error_code VARCHAR2(50)`, FK to `users(user_id)`, check constraints for action (`IMPROVE`, `SUMMARY`, `EXPAND`, `TITLE`, `TAGS`, `CUSTOM`) and status (`PENDING`, `SUCCESS`, `FAILED`). Add index on `(user_id, requested_at)`. Add `AiUsageLogDto` and `AiUsageLogDao` with methods: `insertPending(userId, action, inputChars, customPromptChars, maxOutputTokens)`, `markSuccess(usageId)`, `markFailed(usageId, errorCode)`, `findRecentPendingOrSuccess(userId, seconds)`. Use `PreparedStatement` and `DBUtil.close` style.
  **Must NOT do**: Do not store full draft text, custom prompt body, AI output, or API key in DB. Do not add count quotas.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: schema + DAO consistency and SQL correctness.
  - Skills: `[]` - No special skill available.
  - Omitted: [`database-reviewer`] - Not available as category; final review covers DB.

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 4, 5, 7 | Blocked By: None

  **References**:
  - Pattern: `studyLog/acorncampus_studyLog/src/main/resources/schema.sql:8-14` - existing sequence declarations.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/resources/schema.sql:17-32` - table constraints style.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/java/com/acorncampus_studylog/util/DBUtil.java:26-35` - connection/close pattern.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/java/com/acorncampus_studylog/dao/PasswordResetDao.java:18-33` - insert DAO pattern.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/java/com/acorncampus_studylog/dao/PasswordResetDao.java:42-71` - query DAO pattern.

  **Acceptance Criteria**:
  - [ ] `schema.sql` contains `ai_usage_log_seq`, `ai_usage_log`, FK to `users(user_id)`, action/status check constraints, and `(user_id, requested_at)` index.
  - [ ] `AiUsageLogDao` uses only `PreparedStatement`, never `Statement` or string-concatenated SQL input.
  - [ ] `studyLog/acorncampus_studyLog/mvnw.cmd -q -DskipTests compile` succeeds.

  **QA Scenarios**:
  ```
  Scenario: DAO cooldown lookup finds recent pending request
    Tool: Bash
    Steps: Run targeted DAO/service test that inserts a PENDING row for a test user and queries `findRecentPendingOrSuccess(userId, 15)`.
    Expected: Test returns the row and calculates remaining cooldown > 0.
    Evidence: .sisyphus/evidence/task-2-cooldown-dao.txt

  Scenario: DAO excludes old/failed rows from cooldown
    Tool: Bash
    Steps: Run targeted test with one old SUCCESS row and one FAILED row where OpenAI was not attempted.
    Expected: Cooldown lookup returns empty or remaining seconds 0.
    Evidence: .sisyphus/evidence/task-2-cooldown-exclusion.txt
  ```

  **Commit**: NO | Message: `feat(ai): add usage log persistence` | Files: [`schema.sql`, `AiUsageLogDto.java`, `AiUsageLogDao.java`, tests]

- [x] 3. Implement OpenAI Client Wrapper

  **What to do**: Create a small server-side client wrapper, e.g. `OpenAiClient` or `OpenAiWritingClient`, that accepts a prepared prompt and returns generated text. Use Responses API with the official Java SDK if dependency works; otherwise use Java 17 `HttpClient` and Gson to call the Responses API directly. Server fixes model and max output tokens; callers cannot override them from request parameters. Use timeout 25 seconds. Use max output tokens exactly `600`. Default temperature should be conservative (`0.4`) if supported by the chosen API/model; otherwise omit unsupported parameters rather than forcing incompatible options. Normalize OpenAI errors into application exceptions with safe codes: `OPENAI_CONFIG_MISSING`, `OPENAI_TIMEOUT`, `OPENAI_RATE_LIMIT`, `OPENAI_SERVER_ERROR`, `OPENAI_BAD_RESPONSE`.
  **Must NOT do**: Do not stream in v1; do not expose raw OpenAI exception bodies to client; do not support model selection from UI.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: external API integration and error handling.
  - Skills: `[]` - No relevant skill loaded.
  - Omitted: [`visual-engineering`] - No UI work.

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 4, 7 | Blocked By: 1

  **References**:
  - Pattern: `studyLog/acorncampus_studyLog/pom.xml:62-67` - Gson available for direct HTTP JSON if SDK fallback is needed.
  - External: `https://developers.openai.com/api/reference/responses` - Responses API.
  - External: `https://developers.openai.com/api/docs/models/gpt-5.4-mini` - preferred mini model.
  - External: `https://developers.openai.com/api/docs/pricing` - cost awareness.
  - External: `https://github.com/openai/openai-java` - Java SDK retries/timeouts guidance.

  **Acceptance Criteria**:
  - [ ] Client compiles and can be unit-tested with fake transport/fake client without network.
  - [ ] Max output tokens is fixed at 600 in server code.
  - [ ] OpenAI API key is read only from server-side config.
  - [ ] `studyLog/acorncampus_studyLog/mvnw.cmd -q -DskipTests compile` succeeds.

  **QA Scenarios**:
  ```
  Scenario: Fake OpenAI success
    Tool: Bash
    Steps: Run unit test using fake OpenAI transport that returns `다듬은 문장입니다.`.
    Expected: Client wrapper returns the generated text exactly and records no raw secret/output logs.
    Evidence: .sisyphus/evidence/task-3-fake-openai-success.txt

  Scenario: OpenAI timeout maps to safe error
    Tool: Bash
    Steps: Run unit test with fake timeout exception.
    Expected: Application exception code is `OPENAI_TIMEOUT`; client-safe message does not include API key or raw request body.
    Evidence: .sisyphus/evidence/task-3-openai-timeout.txt
  ```

  **Commit**: NO | Message: `feat(ai): add OpenAI client wrapper` | Files: [`OpenAiConfig.java`, `OpenAiWritingClient.java`, tests]

- [x] 4. Implement AI Writing Domain Service and Limits

  **What to do**: Add service layer, e.g. `AiWritingService`, that validates request fields, checks cooldown, inserts `PENDING` usage log, builds prompts, calls the OpenAI client, and marks usage `SUCCESS`/`FAILED`. Enforce exact limits: request body max 8 KB at controller boundary; `draftText` max 3,000 Java characters after trim; `customPrompt` max 500 Java characters after trim; output max 600 tokens via client; cooldown 15 seconds per user. Do not add request-count quotas. Build action-specific Korean prompts with strict output instructions: `IMPROVE` returns improved text only; `SUMMARY` returns concise Korean summary only; `EXPAND` expands while preserving meaning; `TITLE` returns 3-5 title candidates; `TAGS` returns comma-separated tags; `CUSTOM` follows user's instruction but refuses secrets, illegal content, credential extraction, or instructions to ignore limits.
  **Must NOT do**: Do not accept unknown actions; do not trust client-provided userId/model/maxTokens; do not store prompt bodies in usage log.

  **Recommended Agent Profile**:
  - Category: `deep` - Reason: policy/validation/error flow has multiple edge cases and cost controls.
  - Skills: `[]` - No special skill available.
  - Omitted: [`visual-engineering`] - Backend domain only.

  **Parallelization**: Can Parallel: NO | Wave 2 | Blocks: 5, 7 | Blocked By: 2, 3

  **References**:
  - Pattern: `studyLog/acorncampus_studyLog/src/main/java/com/acorncampus_studylog/service/PasswordResetService.java:48-71` - service orchestrates DAO + utility operation.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/java/com/acorncampus_studylog/service/SeriesService.java` - validation style for user-facing errors if needed.
  - API/Type: `UserDto` session user from `com.acorncampus_studylog.dto.UserDto` - use user id from session only.
  - Research: Oracle recommendation - count recent `PENDING` and `SUCCESS` rows for cooldown to prevent double-submit.

  **Acceptance Criteria**:
  - [ ] Oversized `draftText` returns validation error before OpenAI client is called.
  - [ ] Oversized `customPrompt` returns validation error before OpenAI client is called.
  - [ ] Cooldown hit returns remaining seconds and does not call OpenAI.
  - [ ] PENDING row is marked SUCCESS on success and FAILED on exceptions.
  - [ ] `studyLog/acorncampus_studyLog/mvnw.cmd -q -Dtest=AiWritingServiceTest test` succeeds.

  **QA Scenarios**:
  ```
  Scenario: Happy path improve action
    Tool: Bash
    Steps: Run service test with fake OpenAI client for action `IMPROVE`, draft text under 3,000 chars, no cooldown row.
    Expected: Service returns preview text, inserts PENDING, then marks SUCCESS.
    Evidence: .sisyphus/evidence/task-4-service-happy.txt

  Scenario: Cooldown blocks double-submit
    Tool: Bash
    Steps: Run service test with a recent PENDING usage row for same user and call service again within 15 seconds.
    Expected: Service returns/throws cooldown error with HTTP-equivalent 429 metadata; fake OpenAI client call count remains 0.
    Evidence: .sisyphus/evidence/task-4-service-cooldown.txt
  ```

  **Commit**: NO | Message: `feat(ai): add writing service limits` | Files: [`AiWritingService.java`, request/response DTOs, tests]

- [x] 5. Add Authenticated AI Servlet Endpoint

  **What to do**: Add `AiController` under `com.acorncampus_studylog.controller` with `@WebServlet("/l_check/ai/*")` and route `POST /l_check/ai/assist.do`. Read JSON request body with a hard 8 KB maximum before parsing. Use Gson request DTO fields: `action`, `draftText`, `customPrompt`. Get login user from `session.getAttribute("loginUser")`; if missing, return JSON `401` even though filter should protect it. Call `AiWritingService`; map validation errors to `400`, cooldown to `429`, OpenAI/config/server errors to safe `500` or `503`. Response success JSON: `{ "status":"ok", "action":"...", "result":"...", "usageId":123 }`. Error JSON: `{ "status":"error", "code":"...", "message":"...", "retryAfterSeconds":15 }` when relevant. Use Gson for all JSON.
  **Must NOT do**: Do not use `String.format` JSON; do not redirect HTML login page for AJAX; do not accept GET for generation; do not log prompt body.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: servlet routing, JSON parsing, auth, HTTP status handling.
  - Skills: `[]` - No special skill available.
  - Omitted: [`visual-engineering`] - No frontend work.

  **Parallelization**: Can Parallel: NO | Wave 2 | Blocks: 6, 7 | Blocked By: 4

  **References**:
  - Pattern: `studyLog/acorncampus_studyLog/src/main/java/com/acorncampus_studylog/controller/PostController.java:365-419` - Gson JSON upload response style.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/java/com/acorncampus_studylog/controller/PostController.java:425-430` - session helper convention.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/webapp/WEB-INF/web.xml:31-43` - `/l_check/*` protected by `LoginCheckFilter`.
  - Known issue guardrail: `CommentController`, `LikeController`, `ReportController` use manual JSON; do not copy that pattern.

  **Acceptance Criteria**:
  - [ ] `POST /l_check/ai/assist.do` returns JSON for success and all errors.
  - [ ] Missing/invalid login returns status `401` JSON.
  - [ ] Request body above 8 KB returns status `400` before service/OpenAI call.
  - [ ] Cooldown returns status `429` with `retryAfterSeconds`.
  - [ ] `git grep -n "String.format" -- studyLog/acorncampus_studyLog/src/main/java/com/acorncampus_studylog/controller/AiController.java` returns no matches.

  **QA Scenarios**:
  ```
  Scenario: Valid AJAX request gets preview
    Tool: Bash
    Steps: Run controller-level test or local HTTP request with logged-in session test harness, action `SUMMARY`, short draft.
    Expected: HTTP 200 JSON contains `status=ok`, `action=SUMMARY`, non-empty `result`.
    Evidence: .sisyphus/evidence/task-5-endpoint-success.json

  Scenario: Oversized body rejected
    Tool: Bash
    Steps: Send a POST body over 8 KB to `/l_check/ai/assist.do` in test harness.
    Expected: HTTP 400 JSON with code like `REQUEST_TOO_LARGE`; OpenAI fake client not called.
    Evidence: .sisyphus/evidence/task-5-endpoint-oversize.json
  ```

  **Commit**: NO | Message: `feat(ai): add writing assistant endpoint` | Files: [`AiController.java`, controller tests]

- [x] 6. Build Post Write AI Modal and Frontend Integration

  **What to do**: First stop and ask the user for explicit frontend approval before editing any JSP/CSS/JS files. Present the intended frontend file list: `post/write.jsp`, a new AI modal JS module if used, and AI modal CSS location. After approval, update the Milkdown-based `src/main/webapp/WEB-INF/views/post/write.jsp` and scoped CSS/JS to add an AI assistant modal opened by `Ctrl+Space` while on the post write page. Current editor state: `write.jsp` mounts Milkdown at `#editor`, stores submitted markdown in hidden `#contentHidden`, initializes from hidden `#milkdown-init`, imports `initEditor/getMarkdown` from `milkdown-editor.js`, and falls back to `#fallback-textarea` if Milkdown init fails. AI Run must read current draft through `getMarkdown()` when Milkdown is active and through `#fallback-textarea.value` when fallback is active. Add or extend a Milkdown helper for Apply behavior: safest v1 default is replace full editor content with the AI result after preview Apply; if exact Milkdown set-markdown API is uncertain, implement a documented fallback that updates `#contentHidden`/fallback textarea and prompts user to save, but prefer a real Milkdown helper that updates both editor view and internal `_markdown`. Modal must include: action selector/buttons for 5 fixed actions, custom prompt textarea, run button, loading state, preview area, Apply button, Cancel/Close button, cooldown/error message area. The Run button sends `fetch` POST to `${contextPath}/l_check/ai/assist.do` with JSON. Apply behavior: for `TITLE`, offer to apply selected line to `.title-input`; for `TAGS`, offer to apply comma-separated tags to `.tag-input`; for text actions (`IMPROVE`, `SUMMARY`, `EXPAND`, `CUSTOM`), replace the editor content only after explicit Apply. Preserve original content until Apply. Disable duplicate Run while a request is in flight. Handle `401`, `400`, `429`, `500/503` with readable Korean messages.
  **Must NOT do**: Do not edit frontend files before user approval; do not auto-run on page load; do not auto-apply; do not bind Ctrl+Space globally outside this page; do not add Toast UI; do not expose API config in JS; do not broadly modify `post_write.css` because it is shared by post and series write pages.

  **Recommended Agent Profile**:
  - Category: `visual-engineering` - Reason: modal UX, keyboard shortcut, loading/error states, accessibility.
  - Skills: `[]` - No extra skill required.
  - Omitted: [`deep`] - UI behavior is straightforward once endpoint is defined.

  **Parallelization**: Can Parallel: NO for implementation | Wave 4 after backend and explicit frontend approval | Blocks: full UI QA in 7 | Blocked By: 5 and user frontend approval

  **References**:
  - Pattern: `studyLog/acorncampus_studyLog/src/main/webapp/WEB-INF/views/post/write.jsp:10-17` - CSS load order includes `global_theme.css`, components, page CSS, and `components/milkdown.css`.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/webapp/WEB-INF/views/post/write.jsp:46-48` - form action and write/update mode.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/webapp/WEB-INF/views/post/write.jsp:88-91` - Milkdown mount `#editor` and hidden `#contentHidden`.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/webapp/WEB-INF/views/post/write.jsp:146-188` - Milkdown initialization, fallback textarea, and submit sync flow.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/webapp/resources/js/milkdown-editor.js:25-41` - `initEditor` creates Milkdown and stores `_editor`.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/webapp/resources/js/milkdown-editor.js:63-66` - `getMarkdown()` current content API.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/webapp/resources/js/milkdown-editor.js:68-76` - example helper using `_editor.action` and `editorViewCtx`.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/webapp/resources/js/milkdown-slash.js:27-56` - editor event binding style.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/webapp/WEB-INF/views/post/detail.jsp:244-323` - existing modal + fetch AJAX pattern.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/webapp/resources/css/pages/post/post_write.css:1-5` - file is shared by post write and series write; avoid broad selectors.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/webapp/resources/css/components/milkdown.css:1-4` - component CSS uses global theme variables.
  - Pattern: `studyLog/acorncampus_studyLog/src/main/webapp/resources/css/CSS_ARCHITECTURE.md:55-76` - page CSS placement and JSP load-order rules.

  **Acceptance Criteria**:
  - [ ] Before frontend edits, executor presents exact frontend file list and obtains explicit user approval.
  - [ ] Pressing `Ctrl+Space` on post write page opens the AI modal and focuses the first interactive field/button.
  - [ ] Closing modal returns focus to the content textarea and preserves draft text.
  - [ ] AI Run reads Milkdown content via `getMarkdown()` when Milkdown is active and fallback textarea value when Milkdown failed.
  - [ ] Run button sends JSON only to `/l_check/ai/assist.do`.
  - [ ] Apply button is disabled until preview exists.
  - [ ] Apply updates Milkdown editor content and internal markdown state, or uses a documented fallback if Milkdown update API is unavailable.
  - [ ] Error responses show Korean messages and do not alter title/tags/content fields.
  - [ ] PR #76 navigation behavior is not regressed: `/community.do` breadcrumb still works, post/series keyword tabs preserve keyword, and post detail series back link remains series-aware.
  - [ ] `studyLog/acorncampus_studyLog/mvnw.cmd -q -DskipTests compile` succeeds.

  **QA Scenarios**:
  ```
  Scenario: Ctrl+Space opens modal and preview applies to content
    Tool: Playwright
    Steps: Login as test user, open `/l_check/post/write.do`, type `오늘 자바 서블릿을 공부했다.` into the Milkdown editor under `#editor`, press Ctrl+Space, choose `문장 다듬기`, click Run, wait for preview, click Apply.
    Expected: Modal opens, preview appears, Milkdown content changes only after Apply, and submitting the form syncs the changed markdown into `#contentHidden`.
    Evidence: .sisyphus/evidence/task-6-modal-apply.png

  Scenario: Cooldown error preserves draft
    Tool: Playwright
    Steps: Run AI once, immediately click Run again within 15 seconds.
    Expected: Modal shows cooldown message with remaining seconds; Milkdown editor content remains unchanged.
    Evidence: .sisyphus/evidence/task-6-cooldown-preserve.png

  Scenario: PR #76 navigation unaffected
    Tool: Playwright
    Steps: After AI modal changes, visit `/community.do`, `/post/list.do?keyword=java`, `/series/list.do?keyword=java`, and a post detail page that belongs to a series.
    Expected: Community route loads, keyword-aware list tabs preserve `keyword=java`, and post detail back link returns to the series detail/list behavior introduced by PR #76.
    Evidence: .sisyphus/evidence/task-6-pr76-navigation.png
  ```

  **Commit**: NO | Message: `feat(ai): add post writing assistant modal` | Files: [`write.jsp`, `milkdown-editor.js`, AI modal JS file, scoped AI modal CSS file or tightly scoped `post_write.css` section]

- [x] 7. Add Automated Tests and Opt-in Real API Smoke Test

  **What to do**: Add targeted JUnit 5 tests for config loading, prompt/action validation, limits, cooldown, and fake OpenAI client success/error paths. Add a real API smoke test class named `OpenAiRealApiTest` or similar that is disabled unless `-Dopenai.realTest=true` is supplied and `openai.properties` with a real key is present. Normal `mvn test` and `-Dtest=Ai*Test test` must not call the real API. If the existing `DaoIntegrationTest` requires Oracle and would interfere with normal runs, keep new tests targeted with `-Dtest=Ai*Test` acceptance commands rather than changing existing test policy. Ensure real smoke test uses a tiny prompt, e.g. Korean one-sentence rewrite, and max output <= 80 tokens for the test only if client supports per-test override through safe test configuration; otherwise keep production 600 token cap but tiny prompt.
  **Must NOT do**: Do not run real API tests automatically in CI/default Maven; do not print API key or full response bodies to logs; do not fail normal tests when key is absent.

  **Recommended Agent Profile**:
  - Category: `unspecified-high` - Reason: test isolation and real API guardrails.
  - Skills: `[]` - No extra skill required.
  - Omitted: [`visual-engineering`] - UI QA covered separately.

  **Parallelization**: Can Parallel: PARTIAL | Wave 3 for backend tests after Tasks 1-5; Wave 4 for UI QA after Task 6 | Blocks: 8 | Blocked By: backend tests need 1-5; UI QA needs 6

  **References**:
  - Pattern: `studyLog/acorncampus_studyLog/src/test/java/com/acorncampus_studylog/dao/DaoIntegrationTest.java:15-22` - current JUnit style and real DB dependency note.
  - Pattern: `studyLog/acorncampus_studyLog/pom.xml:98-102` - Surefire plugin exists.
  - External: `https://developers.openai.com/api/docs/guides/production-best-practices` - safe API usage.

  **Acceptance Criteria**:
  - [ ] `studyLog/acorncampus_studyLog/mvnw.cmd -q -Dtest=Ai*Test test` succeeds without real OpenAI key.
  - [ ] `studyLog/acorncampus_studyLog/mvnw.cmd -q -Dtest=OpenAiRealApiTest test` skips or aborts without API spend unless `-Dopenai.realTest=true` is present.
  - [ ] With valid `openai.properties`, `studyLog/acorncampus_studyLog/mvnw.cmd -q -Dtest=OpenAiRealApiTest -Dopenai.realTest=true test` succeeds and records evidence.

  **QA Scenarios**:
  ```
  Scenario: Normal tests do not spend tokens
    Tool: Bash
    Steps: Run `.\mvnw.cmd -q -Dtest=Ai*Test test` from `studyLog/acorncampus_studyLog` with no `openai.properties`.
    Expected: Tests pass or only config-missing tests assert safe failure; no network call occurs.
    Evidence: .sisyphus/evidence/task-7-no-real-api-default.txt

  Scenario: Explicit real API smoke test
    Tool: Bash
    Steps: With real ignored `openai.properties` present, run `.\mvnw.cmd -q -Dtest=OpenAiRealApiTest -Dopenai.realTest=true test`.
    Expected: Test calls OpenAI once with tiny Korean prompt and receives non-empty text.
    Evidence: .sisyphus/evidence/task-7-real-api-smoke.txt
  ```

  **Commit**: NO | Message: `test(ai): add assistant tests and real API smoke` | Files: [`src/test/java/...Ai*Test.java`, `OpenAiRealApiTest.java`]

- [x] 8. Update Project Documentation and Operational Notes

  **What to do**: Update project-facing documentation to describe the AI assistant without exposing secrets. Update `CLAUDE.md` known features/status and add a short docs note, e.g. `docs/AI_글쓰기_도우미_가이드.md`, with setup steps: create ignored `src/main/resources/openai.properties`, required keys, run compile, run opt-in real API smoke test, SQL DDL requirement, UI usage (`Ctrl+Space`). Include limits exactly: 3,000 chars draft, 500 chars custom prompt, 8 KB body, 600 output tokens, 15-second cooldown, no request-count quotas. Include troubleshooting for 401/400/429/503.
  **Must NOT do**: Do not include real API keys, pricing claims that may go stale, or instructions to commit ignored secret files.

  **Recommended Agent Profile**:
  - Category: `writing` - Reason: concise operational docs and project status update.
  - Skills: `[]` - No extra skill required.
  - Omitted: [`visual-engineering`] - No UI code here.

  **Parallelization**: Can Parallel: NO | Wave 4 | Blocks: Final verification | Blocked By: 1-7

  **References**:
  - Pattern: `studyLog/acorncampus_studyLog/CLAUDE.md:5-10` - project tech stack/status style.
  - Pattern: `studyLog/acorncampus_studyLog/CLAUDE.md:120-142` - known issues/completed items table style.
  - Pattern: `studyLog/acorncampus_studyLog/docs/비밀번호_복구_개발가이드.md:41-56` - ignored secret setup documentation style.
  - Pattern: `studyLog/acorncampus_studyLog/docs/현재_개발_상황_0511.md:156-158` - testing/status note style.

  **Acceptance Criteria**:
  - [ ] Documentation includes setup for ignored `openai.properties` with placeholder values only.
  - [ ] Documentation states normal tests do not call OpenAI and real smoke test requires `-Dopenai.realTest=true`.
  - [ ] Documentation lists exact limits and UI shortcut.
  - [ ] `git grep -n "sk-" -- studyLog/acorncampus_studyLog/docs studyLog/acorncampus_studyLog/CLAUDE.md` returns no matches.

  **QA Scenarios**:
  ```
  Scenario: Setup docs contain no secrets
    Tool: Bash
    Steps: Run `git grep -n "sk-" -- studyLog/acorncampus_studyLog/docs studyLog/acorncampus_studyLog/CLAUDE.md`.
    Expected: No matches.
    Evidence: .sisyphus/evidence/task-8-docs-no-secrets.txt

  Scenario: New developer can find AI setup steps
    Tool: Bash
    Steps: Search docs for `openai.properties`, `Ctrl+Space`, and `openai.realTest`.
    Expected: All three terms appear in the AI guide or CLAUDE.md.
    Evidence: .sisyphus/evidence/task-8-docs-findability.txt
  ```

  **Commit**: NO | Message: `docs(ai): document writing assistant setup` | Files: [`CLAUDE.md`, `docs/AI_글쓰기_도우미_가이드.md`]

## Final Verification Wave (MANDATORY — after ALL implementation tasks)
> 4 review agents run in PARALLEL. ALL must APPROVE. Present consolidated results to user and get explicit "okay" before completing.
> **Do NOT auto-proceed after verification. Wait for user's explicit approval before marking work complete.**
> **Never mark F1-F4 as checked before getting user's okay.** Rejection or user feedback -> fix -> re-run -> present again -> wait for okay.
- [x] F1. Plan Compliance Audit — oracle
- [x] F2. Code Quality Review — unspecified-high
- [x] F3. Real Manual QA — unspecified-high (+ playwright if UI)
- [x] F4. Scope Fidelity Check — deep

## Commit Strategy
- Do not commit automatically. The user has not explicitly requested commits.
- If the user later requests a commit, stage only relevant AI feature files and exclude real `openai.properties`.
- Suggested final commit message if requested: `feat(ai): add post writing assistant`.

## Success Criteria
- Logged-in users can press `Ctrl+Space` on post write and use AI actions safely.
- AI output never overwrites content before explicit Apply.
- Server enforces size/token/cooldown limits regardless of client behavior.
- API key remains untracked and server-side only.
- Normal tests avoid API cost; opt-in real API smoke test verifies live integration when explicitly run.
- PR #76 routing/search/navigation behavior remains intact after AI changes.
