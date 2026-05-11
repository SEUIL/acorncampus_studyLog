<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%-- ${post}가 있으면 수정 모드, 없으면 생성 모드 --%>
    <title>스터디로그 - 게시글 <c:choose><c:when test="${not empty post}">수정</c:when><c:otherwise>작성</c:otherwise></c:choose></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/ui.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/post/post_write.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/milkdown.css">
</head>
<body>
<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sideBar.jsp">
        <jsp:param name="activeMenu" value="mypage"/>
    </jsp:include>

    <main class="main-content">
        <div class="top-bar">
            <a class="breadcrumb" href="${pageContext.request.contextPath}/l_check/user/mypage.do">
                <i class="fa-solid fa-arrow-left"></i> 돌아가기
            </a>
            <button type="button" class="btn btn-outline ai-open-button" id="aiOpenButton">
                <i class="fa-solid fa-wand-magic-sparkles"></i> AI 도우미
                <span class="ai-shortcut">Ctrl + Space</span>
            </button>
        </div>

        <%-- 에러 메시지 표시 (유효성 검사 실패 시) --%>
        <c:if test="${not empty errorMsg}">
            <div class="error-banner" style="color:#ef5350; padding: 10px 0; font-size: 14px;">
                <i class="fa-solid fa-circle-exclamation"></i> <c:out value="${errorMsg}"/>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="error-banner" style="color:#ef5350; padding: 10px 0; font-size: 14px;">
                <i class="fa-solid fa-circle-exclamation"></i> <c:out value="${param.error}"/>
            </div>
        </c:if>

        <%-- 생성: POST /l_check/post/write.do
             수정: POST /l_check/post/update.do (postId hidden으로 전달) --%>
        <form class="write-container"
              action="${pageContext.request.contextPath}/l_check/post/${not empty post ? 'update' : 'write'}.do"
              method="post">

            <%-- 수정 모드일 때만 postId를 hidden으로 전달 --%>
            <c:if test="${not empty post}">
                <input type="hidden" name="postId" value="${post.postId}">
            </c:if>

            <div class="title-area">
                <%-- 수정 모드: 기존 제목 / 생성 모드: param.title (에러 복귀 시 입력값 유지) --%>
                <input type="text" name="title" class="title-input"
                       placeholder="게시글 제목을 입력하세요"
                       value="<c:out value='${not empty post ? post.title : param.title}'/>"
                       required>
            </div>

            <div class="meta-area">
                <div>
                    <%-- 수정 모드: tagStr (컨트롤러에서 List<TagDto> → 쉼표 문자열로 변환해서 전달)
                         생성 모드: param.tags (에러 복귀 시 입력값 유지) --%>
                    <input type="text" name="tags" class="tag-input"
                           placeholder="태그를 쉼표로 구분해 입력하세요. 예) java, oracle, jsp"
                           value="<c:out value='${not empty post ? tagStr : param.tags}'/>">
                    <div class="help-text" style="margin-top: 8px;">최대 5개 태그 권장</div>
                </div>
                <div>
                    <select name="seriesId" class="series-select">
                        <option value="">시리즈 선택 안 함</option>
                        <c:forEach var="series" items="${seriesList}">
                            <%-- 수정 모드: 기존 seriesId와 일치하면 selected
                                 생성 모드: URL 파라미터 seriesId와 일치하면 selected (시리즈 상세에서 "새 글 추가" 클릭 시) --%>
                            <option value="${series.seriesId}"
                                ${(not empty post and post.seriesId eq series.seriesId)
                                  or (empty post and param.seriesId eq series.seriesId) ? 'selected' : ''}>
                                <c:out value="${series.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="editor-area">
                <div id="editor"></div>
                <input type="hidden" name="content" id="contentHidden">
                <input type="hidden" name="thumbnailUrl" id="thumbnailUrlHidden">
            </div>

            <div class="write-footer">
                <div></div><%-- 이미지 업로드는 에디터 툴바에서 처리 --%>

                <div class="footer-right">
                    <div class="visibility-toggle">
                        <span id="toggleText">비공개</span>
                        <label class="switch">
                            <%-- 수정 모드: 기존 isPublic 값 반영
                                 생성 모드: 기본 공개(checked), 에러 복귀 시 param.isPublic 유지 --%>
                            <%-- 체크 해제 시에도 비공개 값이 서버로 전송되도록 기본값을 둔다. --%>
                            <input type="hidden" id="isPublicValue" name="isPublic"
                                   value="${(not empty post and post.isPublic eq 'Y')
                                     or (empty post and (empty param.isPublic or param.isPublic eq 'Y')) ? 'Y' : 'N'}">
                            <input type="checkbox" id="publicToggle" value="Y"
                                   ${(not empty post and post.isPublic eq 'Y')
                                     or (empty post and (empty param.isPublic or param.isPublic eq 'Y')) ? 'checked' : ''}>
                            <span class="slider"></span>
                        </label>
                        <span id="toggleTextRight" style="color: var(--text-main);">공개</span>
                    </div>
                    <button type="submit" class="btn btn-primary" style="padding: 10px 24px; font-size: 15px;">
                        <c:choose>
                            <c:when test="${not empty post}">수정 완료</c:when>
                            <c:otherwise>작성 완료</c:otherwise>
                        </c:choose>
                    </button>
                </div>
            </div>
        </form>

        <div class="ai-assist-modal" id="aiAssistModal" hidden aria-hidden="true">
            <div class="ai-assist-dialog" role="dialog" aria-modal="true" aria-labelledby="aiAssistTitle" tabindex="-1">
                <div class="ai-assist-header">
                    <div>
                        <p class="ai-assist-eyebrow">AI 글쓰기 도우미</p>
                        <h2 id="aiAssistTitle">글쓰기 도우미</h2>
                        <p class="ai-assist-desc">미리보기를 먼저 확인한 뒤 원하는 결과만 적용합니다.</p>
                    </div>
                    <button type="button" class="icon-btn ai-assist-close" id="aiCloseButton" aria-label="AI 도우미 닫기">
                        <i class="fa-solid fa-xmark"></i>
                    </button>
                </div>

                <div class="ai-assist-body">
                    <section class="ai-assist-panel" aria-label="AI 작업 선택">
                        <div class="ai-assist-section-title">작업 선택</div>
                        <div class="ai-action-grid" role="group" aria-label="AI 작업">
                            <button type="button" class="ai-action-card is-selected" data-ai-action="IMPROVE">
                                <span>문장 다듬기</span><small>톤과 흐름 정리</small>
                            </button>
                            <button type="button" class="ai-action-card" data-ai-action="SUMMARY">
                                <span>요약</span><small>핵심만 압축</small>
                            </button>
                            <button type="button" class="ai-action-card" data-ai-action="EXPAND">
                                <span>늘려쓰기</span><small>설명과 예시 보강</small>
                            </button>
                            <button type="button" class="ai-action-card" data-ai-action="TITLE">
                                <span>제목 추천</span><small>제목에 적용</small>
                            </button>
                            <button type="button" class="ai-action-card" data-ai-action="TAGS">
                                <span>태그 추천</span><small>태그에 적용</small>
                            </button>
                            <button type="button" class="ai-action-card" data-ai-action="CUSTOM">
                                <span>직접 요청</span><small>프롬프트 사용</small>
                            </button>
                        </div>

                        <label class="ai-custom-label" for="aiCustomPrompt">직접 요청</label>
                        <textarea id="aiCustomPrompt" class="ai-custom-prompt" maxlength="500" rows="4" placeholder="직접 요청을 선택한 뒤 원하는 수정 방향을 입력하세요."></textarea>
                        <div class="ai-assist-meta">
                            <span id="aiDraftCount">초안 0 / 3000</span>
                            <span id="aiPromptCount">요청 0 / 500</span>
                        </div>
                    </section>

                    <section class="ai-assist-panel ai-preview-panel" aria-label="AI 결과 미리보기">
                        <div class="ai-preview-heading">
                            <div>
                                <div class="ai-assist-section-title">미리보기</div>
                                <p id="ai미리보기Hint">실행을 누르면 결과가 먼저 표시됩니다.</p>
                            </div>
                            <span class="tag" id="aiActionBadge">문장 다듬기</span>
                        </div>
                        <textarea id="ai미리보기" class="ai-preview-output" readonly placeholder="AI 결과는 자동 적용되지 않습니다."></textarea>
                        <div class="ai-assist-status" id="aiAssistStatus" role="status" aria-live="polite"></div>
                    </section>
                </div>

                <div class="ai-assist-footer">
                    <button type="button" class="btn btn-outline" id="aiCancelButton">취소</button>
                    <div class="ai-assist-actions">
                        <button type="button" class="btn btn-outline" id="aiRunButton">실행</button>
                        <button type="button" class="btn btn-primary" id="aiApplyButton" disabled>적용</button>
                    </div>
                </div>
            </div>
        </div>
    </main>
</div>

<script>
    const toggle = document.getElementById('publicToggle');
    const isPublicValue = document.getElementById('isPublicValue');
    const textLeft = document.getElementById('toggleText');
    const textRight = document.getElementById('toggleTextRight');

    function syncVisibilityText() {
        isPublicValue.value = toggle.checked ? 'Y' : 'N';
        if (toggle.checked) {
            textLeft.style.color = 'var(--text-sub)';
            textRight.style.color = 'var(--text-main)';
        } else {
            textLeft.style.color = 'var(--text-main)';
            textRight.style.color = 'var(--text-sub)';
        }
    }

    toggle.addEventListener('change', syncVisibilityText);
    syncVisibilityText();
</script>

<%-- 서버에서 내려온 초기 본문(마크다운)을 textarea에 보관 — JS가 꺼내서 에디터에 주입 --%>
<textarea id="milkdown-init" hidden><c:out value="${not empty post ? post.content : param.content}" escapeXml="false"/></textarea>

<script type="module">
    import { initEditor, getMarkdown, setMarkdown } from '${pageContext.request.contextPath}/resources/js/milkdown-editor.js';
    import { initSlashCommand }                  from '${pageContext.request.contextPath}/resources/js/milkdown-slash.js';

    const initialContent = document.getElementById('milkdown-init').value;
    const editorEl       = document.getElementById('editor');
    const form           = document.querySelector('form.write-container');
    const titleInput     = document.querySelector('.title-input');
    const tagInput       = document.querySelector('.tag-input');
    let   milkdownOk     = false;

    try {
        await initEditor(editorEl, initialContent);
        initSlashCommand(editorEl, '${pageContext.request.contextPath}');
        milkdownOk = true;
    } catch (err) {
        console.error('[Milkdown] initialization failed; using fallback textarea.', err);
        editorEl.innerHTML = '';
        const ta = document.createElement('textarea');
        ta.id        = 'fallback-textarea';
        ta.value     = initialContent;
        ta.style.cssText = [
            'width:100%', 'min-height:450px', 'padding:24px 30px',
            'border:none', 'outline:none', 'resize:vertical',
            'background:transparent', 'color:var(--text-main)',
            'font-family:inherit', 'font-size:16px', 'line-height:1.8'
        ].join(';');
        editorEl.appendChild(ta);
    }

    const AI_ACTIONS = {
        IMPROVE: '문장 다듬기',
        SUMMARY: '요약',
        EXPAND: '늘려쓰기',
        TITLE: '제목 추천',
        TAGS: '태그 추천',
        CUSTOM: '직접 요청'
    };
    const MAX_DRAFT_TEXT_CHARS = 3000;
    const MAX_CUSTOM_PROMPT_CHARS = 500;
    const SUCCESS_COOLDOWN_SECONDS = 15;
    const aiEndpoint = '${pageContext.request.contextPath}/l_check/ai/assist.do';

    const aiOpenButton = document.getElementById('aiOpenButton');
    const aiModal = document.getElementById('aiAssistModal');
    const aiDialog = aiModal.querySelector('.ai-assist-dialog');
    const aiCloseButton = document.getElementById('aiCloseButton');
    const aiCancelButton = document.getElementById('aiCancelButton');
    const aiRunButton = document.getElementById('aiRunButton');
    const aiApplyButton = document.getElementById('aiApplyButton');
    const ai미리보기 = document.getElementById('ai미리보기');
    const ai미리보기Hint = document.getElementById('ai미리보기Hint');
    const aiAssistStatus = document.getElementById('aiAssistStatus');
    const aiCustomPrompt = document.getElementById('aiCustomPrompt');
    const aiDraftCount = document.getElementById('aiDraftCount');
    const aiPromptCount = document.getElementById('aiPromptCount');
    const aiActionBadge = document.getElementById('aiActionBadge');
    const aiActionButtons = Array.from(document.querySelectorAll('[data-ai-action]'));

    let selectedAiAction = 'IMPROVE';
    let latestAiResult = null;
    let lastFocusedElement = null;
    let cooldownTimer = null;
    let cooldownUntil = 0;

    function getEditorMarkdown() {
        return milkdownOk
            ? getMarkdown()
            : (document.getElementById('fallback-textarea')?.value ?? '');
    }

    function applyEditorMarkdown(markdown) {
        if (milkdownOk) {
            setMarkdown(markdown);
            return;
        }
        const fallback = document.getElementById('fallback-textarea');
        if (fallback) fallback.value = markdown;
    }

    function resetAi미리보기(message) {
        latestAiResult = null;
        ai미리보기.value = '';
        ai미리보기Hint.textContent = message || '실행을 누르면 결과가 먼저 표시됩니다.';
        aiApplyButton.disabled = true;
    }

    function updateAiCounters() {
        const draftLength = getEditorMarkdown().trim().length;
        const promptLength = aiCustomPrompt.value.trim().length;
        aiDraftCount.textContent = '초안 ' + draftLength + ' / ' + MAX_DRAFT_TEXT_CHARS;
        aiPromptCount.textContent = '요청 ' + promptLength + ' / ' + MAX_CUSTOM_PROMPT_CHARS;
        aiDraftCount.classList.toggle('is-over-limit', draftLength > MAX_DRAFT_TEXT_CHARS);
        aiPromptCount.classList.toggle('is-over-limit', promptLength > MAX_CUSTOM_PROMPT_CHARS);
    }

    function setAiStatus(message, type) {
        aiAssistStatus.textContent = message || '';
        aiAssistStatus.dataset.state = type || '';
    }

    function setSelectedAiAction(action) {
        selectedAiAction = action;
        aiActionButtons.forEach(button => {
            const selected = button.dataset.aiAction === action;
            button.classList.toggle('is-selected', selected);
            button.setAttribute('aria-pressed', selected ? 'true' : 'false');
        });
        aiActionBadge.textContent = AI_ACTIONS[action];
        aiCustomPrompt.disabled = action !== 'CUSTOM';
        if (action !== 'CUSTOM') aiCustomPrompt.value = '';
        resetAi미리보기(AI_ACTIONS[action] + ' 결과가 미리보기에 먼저 표시됩니다.');
        setAiStatus('', '');
        updateAiCounters();
        if (action === 'CUSTOM') aiCustomPrompt.focus();
    }

    function focusInitialAiControl() {
        const selectedButton = aiActionButtons.find(button => button.dataset.aiAction === selectedAiAction);
        (selectedButton || aiRunButton).focus();
    }

    function openAiModal() {
        lastFocusedElement = document.activeElement;
        aiModal.hidden = false;
        aiModal.setAttribute('aria-hidden', 'false');
        document.body.classList.add('ai-assist-open');
        updateAiCounters();
        setTimeout(focusInitialAiControl, 0);
    }

    function closeAiModal() {
        aiModal.hidden = true;
        aiModal.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('ai-assist-open');
        setAiStatus('', '');
        if (lastFocusedElement && typeof lastFocusedElement.focus === 'function') {
            lastFocusedElement.focus();
        }
    }

    function getErrorMessage(data, response) {
        if (data && data.message) {
            if (data.retryAfterSeconds) {
                return data.message + ' (' + data.retryAfterSeconds + '초 남음)';
            }
            return data.message;
        }
        return 'AI 요청에 실패했습니다. HTTP 상태 ' + response.status;
    }

    function startCooldown(seconds) {
        if (!seconds || seconds < 1) return;
        cooldownUntil = Date.now() + seconds * 1000;
        if (cooldownTimer) clearInterval(cooldownTimer);
        cooldownTimer = setInterval(syncCooldownButton, 250);
        syncCooldownButton();
    }

    function syncCooldownButton() {
        const remaining = Math.ceil((cooldownUntil - Date.now()) / 1000);
        if (remaining > 0) {
            aiRunButton.disabled = true;
            aiRunButton.textContent = '실행 (' + remaining + '초)';
            return;
        }
        if (cooldownTimer) {
            clearInterval(cooldownTimer);
            cooldownTimer = null;
        }
        if (!aiRunButton.dataset.loading) aiRunButton.disabled = false;
        aiRunButton.textContent = '실행';
    }

    async function runAiAssist() {
        const draftText = getEditorMarkdown().trim();
        const customPrompt = aiCustomPrompt.value.trim();

        resetAi미리보기('요청 중에도 현재 초안은 변경되지 않습니다.');
        updateAiCounters();

        if (!draftText) {
            setAiStatus('AI 도우미를 사용하기 전에 초안을 작성해 주세요.', 'error');
            return;
        }
        if (draftText.length > MAX_DRAFT_TEXT_CHARS) {
            setAiStatus('초안은 3,000자 이하로 줄여 주세요.', 'error');
            return;
        }
        if (selectedAiAction === 'CUSTOM' && !customPrompt) {
            setAiStatus('직접 요청 내용을 먼저 입력해 주세요.', 'error');
            aiCustomPrompt.focus();
            return;
        }
        if (customPrompt.length > MAX_CUSTOM_PROMPT_CHARS) {
            setAiStatus('직접 요청은 500자 이하로 입력해 주세요.', 'error');
            return;
        }

        aiRunButton.dataset.loading = 'true';
        aiRunButton.disabled = true;
        aiRunButton.textContent = '실행 중...';
        setAiStatus('AI가 초안을 분석 중입니다.', 'loading');

        try {
            const response = await fetch(aiEndpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json;charset=UTF-8' },
                body: JSON.stringify({
                    action: selectedAiAction,
                    draftText: draftText,
                    customPrompt: selectedAiAction === 'CUSTOM' ? customPrompt : ''
                })
            });
            const data = await response.json().catch(() => null);

            if (!response.ok || !data || data.status !== 'ok') {
                const retryAfter = data && data.retryAfterSeconds ? data.retryAfterSeconds : 0;
                if (retryAfter) startCooldown(retryAfter);
                throw new Error(getErrorMessage(data, response));
            }

            latestAiResult = { action: data.action || selectedAiAction, result: data.result || '' };
            ai미리보기.value = latestAiResult.result;
            ai미리보기Hint.textContent = '적용을 누르면 ' + AI_ACTIONS[latestAiResult.action] + ' 결과가 반영됩니다.';
            aiApplyButton.disabled = !latestAiResult.result.trim();
            setAiStatus('미리보기가 준비되었습니다. 사용할 때만 적용을 눌러 주세요.', 'success');
            startCooldown(SUCCESS_COOLDOWN_SECONDS);
        } catch (err) {
            latestAiResult = null;
            aiApplyButton.disabled = true;
            setAiStatus(err.message || 'AI 요청에 실패했습니다. 초안은 변경되지 않았습니다.', 'error');
        } finally {
            delete aiRunButton.dataset.loading;
            syncCooldownButton();
        }
    }

    function firstMeaningfulLine(text) {
        return text.split(/\r?\n/).map(line => line.trim()).find(Boolean) || text.trim();
    }

    function normalizeTags(text) {
        return text
            .replace(/#/g, '')
            .split(/[\n,]/)
            .map(tag => tag.trim())
            .filter(Boolean)
            .slice(0, 5)
            .join(', ');
    }

    function applyAiResult() {
        if (!latestAiResult || !latestAiResult.result.trim()) return;

        if (latestAiResult.action === 'TITLE') {
            titleInput.value = firstMeaningfulLine(latestAiResult.result);
        } else if (latestAiResult.action === 'TAGS') {
            tagInput.value = normalizeTags(latestAiResult.result);
        } else {
            applyEditorMarkdown(latestAiResult.result);
        }

        setAiStatus('AI 결과를 적용했습니다.', 'success');
        closeAiModal();
    }

    aiOpenButton.addEventListener('click', openAiModal);
    aiCloseButton.addEventListener('click', closeAiModal);
    aiCancelButton.addEventListener('click', closeAiModal);
    aiRunButton.addEventListener('click', runAiAssist);
    aiApplyButton.addEventListener('click', applyAiResult);
    aiCustomPrompt.addEventListener('input', () => {
        resetAi미리보기('직접 요청이 변경되었습니다. 미리보기를 새로 보려면 다시 실행해 주세요.');
        updateAiCounters();
    });
    aiActionButtons.forEach(button => {
        button.addEventListener('click', () => setSelectedAiAction(button.dataset.aiAction));
    });

    aiModal.addEventListener('mousedown', event => {
        if (event.target === aiModal) closeAiModal();
    });

    document.addEventListener('keydown', event => {
        if (event.ctrlKey && event.code === 'Space') {
            event.preventDefault();
            openAiModal();
            return;
        }
        if (event.key === 'Escape' && !aiModal.hidden) {
            event.preventDefault();
            closeAiModal();
        }
    });

    form.addEventListener('submit', function (e) {
        const content = getEditorMarkdown().trim();

        if (!content) {
            e.preventDefault();
            alert('본문을 입력해 주세요.');
            return;
        }
        document.getElementById('contentHidden').value = content;
        const imgRegex = /!\[.*?\]\((.*?)\)/;
            const match = content.match(imgRegex);

            if (match && match[1]) {
                // 첫 번째로 매칭된 이미지의 URL을 히든 필드에 저장
                document.getElementById('thumbnailUrlHidden').value = match[1];
            } else {
                // 이미지가 없으면 빈 값 전송
                document.getElementById('thumbnailUrlHidden').value = "";
            }
        });

    setSelectedAiAction(selectedAiAction);
</script>
</body>
</html>
