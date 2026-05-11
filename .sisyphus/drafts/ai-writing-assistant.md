# Draft: AI Writing Assistant

## Requirements (confirmed)
- "openai api를 사용 (키 준비되어있음),(기능에 맞는 최적 모델은 너가 추천)"
- "게시물에 글쓰기를 할때 ctal + space를 누르면 ai 모달창이 뜸"
- "모달창에 글을 쓰고 해당되는 행동을 ai를 통해 하게 만들껀데"
- "너무 복잡하거나 양이 많아 토큰을 많이먹는 글을 요청할수도있잖아, 그래서 사용자가 ai 기능을 사용할때 어떤방법으로 제한적으로 사용하게 만들지가 고민"
- Latest develop was pulled after the plan was generated; teammate changes must be reviewed and the plan updated before execution.
- Frontend changes must follow `studyLog/acorncampus_studyLog/src/main/webapp/resources/css/CSS_ARCHITECTURE.md`.
- Before any frontend implementation starts, user confirmation is required.

## Technical Decisions
- Usage limit policy: composite server-side limits selected — input length cap + output token cap + 15-second cooldown only; no hourly/daily/global count quotas.
- Availability: all logged-in users; no admin/beta restriction because this is not intended for deployment.
- AI actions: 5 fixed actions + custom prompt; fixed actions are 문장 다듬기, 요약, 늘려쓰기, 제목 추천, 태그 추천.
- AI result insertion: preview first, then user explicitly applies the result.
- Usage storage: DB-backed usage log selected.
- OpenAI call location: server-side only; never expose API key in JSP/JS.
- API: OpenAI Responses API via official Java SDK preferred; direct HTTP only if SDK compatibility blocks implementation.
- Model recommendation: start with `gpt-5.4-mini` for Korean writing/editing/summarization; fallback to a currently available mini text model if account/model access differs.
- Practical default limits to validate: draft text 3,000 chars, custom prompt 500 chars, request body 8 KB, output 600 tokens, cooldown 15s, 8/hour/user, 20/day/user, 100/day global.
- Final limits: draft text 3,000 chars, custom prompt 500 chars, request body 8 KB, output 600 tokens, cooldown 15s. No hourly, daily, or global request-count limits.
- Quota storage recommendation: DB-backed `ai_usage_log`, not session-only/in-memory, so refreshes/restarts/new browsers do not bypass cost controls.
- API key storage: ignored `src/main/resources/openai.properties`, with `.gitignore` update required.
- Test strategy: include real OpenAI API test; plan should make it opt-in/manual or property-gated to avoid accidental cost during normal `mvn test`.
- Frontend implementation gate: executor must stop before modifying JSP/CSS/JS and get explicit user approval.
- Backend-first execution requested: user reported PR #76 merged and compile succeeds; git verification confirmed HEAD contains `8714d01` via merge commit `0d7bd07`.

## Research Findings
- Superseded initial finding: early read saw textarea-style assumptions, but latest code uses Milkdown mounted at `#editor` with hidden `#contentHidden` and fallback `#fallback-textarea`.
- `pom.xml`: Gson already exists for JSON; no OpenAI dependency currently confirmed in direct read.
- `.gitignore`: `src/main/resources/mail.properties` is ignored for SMTP credentials; OpenAI secret should follow server-side ignored/env pattern, never client/JSP.
- `DaoIntegrationTest`: JUnit 5 integration test exists but depends on real Oracle testdb/blog/schema setup.
- Explore agent: no Toast UI usage found anywhere; `write.jsp` fields at lines 45-123 and inline script at 128-147; content textarea at 89-91.
- Latest direct check after develop update: `post/write.jsp` now loads `resources/css/components/milkdown.css` at line 17, so editor assumptions require revalidation before execution.
- PR #76 fetched: `origin/develop` is `8714d01 Merge pull request #76 from SEUIL/feature/JCJ/front`; current branch was still at PR #75 locally when checked, so implementation must sync before coding.
- PR #76 changed post/series list search/sort mapping and `ui.css`; it did not change `post/write.jsp`, `milkdown-editor.js`, `milkdown-slash.js`, or `components/milkdown.css`.
- Current editor architecture: `post/write.jsp` mounts Milkdown at `#editor`, stores submit value in `#contentHidden`, initializes from `#milkdown-init`, imports `getMarkdown()`, and has fallback `#fallback-textarea`.
- CSS architecture doc requires 3-layer CSS order: `global_theme.css` → components → page CSS, page-specific CSS under `resources/css/pages/{category}/`, and no duplicate component class definitions.
- Explore agent: existing modal/fetch patterns are in `post/detail.jsp`, admin list JSPs; `PostController` upload uses Gson JSON response at lines 372-419.
- Explore agent: session key is `loginUser`; `/l_check/*` protected by `LoginCheckFilter`; AI endpoint should live under `/l_check/ai/*`.
- Librarian: OpenAI recommends Responses API for new work; official Java SDK supports server-side env-based config, retries, timeouts.
- Oracle: for cost boundary, use DB `ai_usage_log` with PENDING/SUCCESS/FAILED status and count PENDING/SUCCESS for cooldown/double-click abuse.

## Open Questions
- Confirm exact fixed action list for v1.
- Confirm whether AI output should replace the editor content immediately, insert at cursor/end, or preview with Apply/Cancel.
- Confirm whether to create a persistent usage-log DB table/schema migration or use a simpler session-only limiter despite weaker protection.
- RESOLVED: test strategy includes real API test, but should be guarded so normal tests do not spend tokens accidentally.
- RESOLVED: secret storage via ignored `openai.properties`.
- Pending latest-code revalidation from explore agents before final handoff.

## Scope Boundaries
- INCLUDE: Post write page Ctrl+Space shortcut, AI modal UX, backend OpenAI proxy endpoint, usage limits, API key safety, error handling.
- EXCLUDE: Direct frontend OpenAI calls, unlimited requests, exposing the API key to client code.
