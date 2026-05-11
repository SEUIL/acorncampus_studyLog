<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>스터디로그 - 게시글 상세</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/ui.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/post/post_detail.css">
</head>
<body>
<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sideBar.jsp">
        <jsp:param name="activeMenu" value="community"/>
    </jsp:include>

    <main class="main-content">
        <div class="top-bar">
            <a class="breadcrumb" href="<c:out value='${backUrl}'/>">
                <i class="fa-solid fa-arrow-left"></i> 돌아가기
            </a>
            <div class="top-actions">
                <c:if test="${not empty loginUser and (loginUser.userId eq post.userId or loginUser.role eq 'ADMIN')}">
                    <a class="action-btn" href="${pageContext.request.contextPath}/l_check/post/update.do?id=${post.postId}">
                        <i class="fa-solid fa-pen"></i> 수정
                    </a>
                    <form action="${pageContext.request.contextPath}/l_check/post/delete.do" method="post" style="display:inline;">
                        <input type="hidden" name="postId" value="${post.postId}">
                        <button type="submit" class="action-btn"><i class="fa-regular fa-trash-can"></i> 삭제</button>
                    </form>
                </c:if>
                <c:if test="${not empty loginUser}">
                    <button type="button" class="action-btn btn-report" onclick="toggleModal(true)">
                        <i class="fa-solid fa-triangle-exclamation"></i> 신고
                    </button>
                </c:if>
            </div>
        </div>

        <article class="post-container">
            <header class="post-header">
                <h1><c:out value="${post.title}"/></h1>
                <div class="post-meta">
                    <div class="meta-left">
                        <div class="author-info">
                            <span class="author-avatar"><i class="fa-solid fa-user" style="font-size: 11px;"></i></span>
                            <a href="${pageContext.request.contextPath}/user/profile.do?id=${post.userId}"
                               style="color:inherit; font-weight:600; text-decoration:none;"
                               onmouseover="this.style.color='var(--accent-color)'"
                               onmouseout="this.style.color='inherit'">
                                <c:out value="${post.authorName}"/>
                            </a>
                        </div>
                        <span><c:out value="${post.createdAt}"/></span>
                        <c:if test="${not empty post.seriesName}">
                            <span><i class="fa-solid fa-folder-open"></i> <c:out value="${post.seriesName}"/></span>
                        </c:if>
                    </div>
                    <div class="meta-right">
                        <span><i class="fa-regular fa-eye"></i> <c:out value="${post.viewCount}"/></span>
                        <span><i class="fa-regular fa-heart"></i> <c:out value="${post.likeCount}"/></span>
                        <span><i class="fa-regular fa-comment"></i> <c:out value="${post.commentCount}"/></span>
                    </div>
                </div>
                <c:if test="${not empty post.tagList}">
                    <div class="tag-list">
                        <c:forEach var="tag" items="${post.tagList}">
                            <%-- href URL 파라미터에 tag.name을 그대로 쓰면 XSS 가능 → fn:escapeXml 처리 --%>
                            <a class="tag-chip" href="${pageContext.request.contextPath}/tag/post.do?tag=${fn:escapeXml(tag.name)}">
                                #<c:out value="${tag.name}"/>
                            </a>
                        </c:forEach>
                    </div>
                </c:if>
            </header>

            <div class="post-body" id="viewer"></div>
            <textarea id="tui-content" hidden><c:out value="${post.content}" escapeXml="false"/></textarea>
            <div class="post-actions-bottom" style="text-align: center; margin-top: 40px; margin-bottom: 40px;">
                            <c:choose>
                                <c:when test="${not empty loginUser}">
                                    <button type="button" class="btn btn-outline ${myLike eq 'L' ? 'active' : ''}" onclick="togglePostLike(this, ${post.postId}, 'L')">
                                        <i class="fa-solid fa-thumbs-up"></i> 추천 <span id="like-count">${post.likeCount}</span>
                                    </button>
                                    <button type="button" class="btn btn-outline ${myLike eq 'D' ? 'active-dislike' : ''}" onclick="togglePostLike(this, ${post.postId}, 'D')" style="margin-left: 10px;">
                                        <i class="fa-solid fa-thumbs-down"></i> 비추천 <span id="dislike-count">${post.dislikeCount}</span>
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <p style="font-size: 13px; color: #888;">추천/비추천은 로그인 후 가능합니다.</p>
                                </c:otherwise>
                            </c:choose>
                        </div>
        </article>

        <section class="comments-section">
            <div class="comments-header">댓글 <c:out value="${fn:length(comments)}"/>개</div>

            <c:choose>
                <c:when test="${not empty loginUser}">
                   <!-- 기존 폼 부분 (수정) -->
                   <form id="commentForm" class="comment-input-box">
                       <input type="hidden" name="postId" value="${post.postId}">
                       <textarea class="comment-input" name="content" placeholder="댓글을 작성해 보세요..." required></textarea>
                       <button type="submit" class="btn btn-primary comment-submit">댓글 등록</button>
                   </form>
                </c:when>
                <c:otherwise>
                    <p class="empty-state">댓글 작성은 로그인 후 이용할 수 있습니다.</p>
                </c:otherwise>
            </c:choose>

            <ul class="comment-list">
                <c:choose>
                    <c:when test="${not empty comments}">
                        <c:forEach var="comment" items="${comments}">
                            <li class="comment-item" id="comment-${comment.commentId}">
                                <%-- 메타 (작성자 + 날짜) --%>
                                <div class="comment-meta">
                                    <span class="comment-author"><c:out value="${comment.authorName}"/></span>
                                    <span class="comment-date"><c:out value="${comment.createdAt}"/></span>
                                </div>

                                <%-- 본문 (수정 모드 전환 시 숨김) --%>
                                <div class="comment-content" id="comment-content-${comment.commentId}">
                                    <c:out value="${comment.content}"/>
                                </div>

                                <%-- 인라인 수정 폼 (기본 숨김) --%>
                                <c:if test="${not empty loginUser and loginUser.userId eq comment.userId}">
                                <div class="edit-form" id="edit-form-${comment.commentId}" style="display:none;">
                                    <textarea class="reply-textarea"
                                              id="edit-content-${comment.commentId}"><c:out value="${comment.content}"/></textarea>
                                    <div class="reply-form-actions">
                                        <button class="btn btn-primary btn-sm"
                                                onclick="submitEdit(${comment.commentId})">수정 완료</button>
                                        <button class="btn btn-outline btn-sm"
                                                onclick="cancelEdit(${comment.commentId})">취소</button>
                                    </div>
                                </div>
                                </c:if>

                                <%-- 액션 버튼 행 --%>
                                <div class="comment-actions">
                                    <%-- 답글 버튼 — 로그인한 사용자에게만 표시 --%>
                                    <c:if test="${not empty loginUser}">
                                        <button class="comment-action-btn"
                                                onclick="toggleReplyForm(${comment.commentId})">
                                            <i class="fa-regular fa-comment"></i> 답글
                                        </button>
                                    </c:if>
                                    <%-- 수정 / 삭제 — 본인 댓글 또는 ADMIN --%>
                                    <c:if test="${not empty loginUser and (loginUser.userId eq comment.userId or loginUser.role eq 'ADMIN')}">
                                        <button class="comment-action-btn"
                                                onclick="toggleEditForm(${comment.commentId})">수정</button>
                                        <button class="comment-action-btn delete-btn"
                                                onclick="deleteComment(${comment.commentId})">삭제</button>
                                    </c:if>
                                </div>

                                <%-- 인라인 답글 입력 폼 (기본 숨김) --%>
                                <c:if test="${not empty loginUser}">
                                <div class="reply-form" id="reply-form-${comment.commentId}" style="display:none;">
                                    <textarea class="reply-textarea"
                                              id="reply-content-${comment.commentId}"
                                              placeholder="답글을 작성해 주세요..."></textarea>
                                    <div class="reply-form-actions">
                                        <button class="btn btn-primary btn-sm"
                                                onclick="submitReply(${comment.commentId}, ${post.postId})">답글 등록</button>
                                        <button class="btn btn-outline btn-sm"
                                                onclick="toggleReplyForm(${comment.commentId})">취소</button>
                                    </div>
                                </div>
                                </c:if>

                                <%-- 기존 대댓글 목록 --%>
                                <c:if test="${not empty comment.replies}">
                                    <div class="reply-list">
                                        <c:forEach var="reply" items="${comment.replies}">
                                            <div class="reply-item" id="comment-${reply.commentId}">
                                                <div class="comment-meta">
                                                    <span class="comment-author"><c:out value="${reply.authorName}"/></span>
                                                    <span class="comment-date"><c:out value="${reply.createdAt}"/></span>
                                                </div>
                                                <div class="comment-content" id="comment-content-${reply.commentId}">
                                                    <c:out value="${reply.content}"/>
                                                </div>

                                                <%-- 대댓글 인라인 수정 폼 --%>
                                                <c:if test="${not empty loginUser and loginUser.userId eq reply.userId}">
                                                <div class="edit-form" id="edit-form-${reply.commentId}" style="display:none;">
                                                    <textarea class="reply-textarea"
                                                              id="edit-content-${reply.commentId}"><c:out value="${reply.content}"/></textarea>
                                                    <div class="reply-form-actions">
                                                        <button class="btn btn-primary btn-sm"
                                                                onclick="submitEdit(${reply.commentId})">수정 완료</button>
                                                        <button class="btn btn-outline btn-sm"
                                                                onclick="cancelEdit(${reply.commentId})">취소</button>
                                                    </div>
                                                </div>
                                                </c:if>

                                                <%-- 대댓글 액션 (수정/삭제만 — 답글에 답글 없음) --%>
                                                <c:if test="${not empty loginUser and (loginUser.userId eq reply.userId or loginUser.role eq 'ADMIN')}">
                                                <div class="comment-actions">
                                                    <button class="comment-action-btn"
                                                            onclick="toggleEditForm(${reply.commentId})">수정</button>
                                                    <button class="comment-action-btn delete-btn"
                                                            onclick="deleteComment(${reply.commentId})">삭제</button>
                                                </div>
                                                </c:if>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:if>
                            </li>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <li class="comment-item">
                            <div class="empty-state">아직 댓글이 없습니다.</div>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </section>
    </main>
</div>

<div class="modal-overlay" id="reportModal">
    <div class="modal-container">
        <div class="modal-header">
            <h3>게시글 신고하기</h3>
            <button type="button" class="btn-close" onclick="toggleModal(false)"><i class="fa-solid fa-xmark"></i></button>
        </div>
        <!-- 🟢 폼 action 제거, id 추가, 카테고리 name 속성 복구 -->
        <form id="reportPostForm">
            <div class="modal-body">
                <input type="hidden" name="postId" value="${post.postId}">
                <label for="reportCategory">신고 사유 카테고리</label>
                <select class="modal-select" id="reportCategory" name="category" required>
                    <option value="">사유를 선택해 주세요</option>
                    <option value="스팸/광고">스팸/광고</option>
                    <option value="욕설/비방">욕설/비방</option>
                    <option value="저작권 침해">저작권 침해</option>
                    <option value="기타">기타</option>
                </select>
                <label for="reportReason">상세 사유</label>
                <textarea class="modal-textarea" id="reportReason" name="reason" placeholder="관리자가 확인할 수 있도록 신고 사유를 입력해 주세요." required></textarea>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline" onclick="toggleModal(false)">취소</button>
                <button type="submit" class="btn btn-primary" style="background-color: #ef5350; border:none; color:white;">신고 접수</button>
            </div>
        </form>
    </div>
</div>

<script>
    // 1. 모달 열기/닫기 함수
    function toggleModal(show) {
            document.getElementById('reportModal').style.display = show ? 'flex' : 'none';
        }

        // 2. 게시글 좋아요/싫어요 토글 AJAX (수정됨)
        function togglePostLike(btn, postId, likeType) {
            const params = new URLSearchParams();
            params.append('postId', postId);
            params.append('likeType', likeType);

            fetch('${pageContext.request.contextPath}/l_check/like/post.do', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params
            })
            .then(response => response.json())
            .then(data => {
                if (data.status === 'ok') {
                    // 이모지 버스트 애니메이션 (함수가 있을 경우)
                    if (typeof window.spawnLikeReaction === 'function') {
                        window.spawnLikeReaction(btn, likeType);
                    }

                    // 카운트 숫자 갱신
                    const container = btn.closest('.post-actions-bottom');
                    const likeCountEl = container.querySelector('#like-count');
                    const dislikeCountEl = container.querySelector('#dislike-count');

                    if (likeCountEl) likeCountEl.textContent = data.likeCount;
                    if (dislikeCountEl) dislikeCountEl.textContent = data.dislikeCount;

                    // 버튼 활성 상태 갱신 (수정된 부분: 인덱스로 정확히 타겟팅)
                    const buttons = container.querySelectorAll('.btn-outline');
                    const likeBtn = buttons[0];
                    const dislikeBtn = buttons[1];

                    if (likeBtn && dislikeBtn) {
                        likeBtn.classList.toggle('active', data.myLike === 'L');
                        dislikeBtn.classList.toggle('active-dislike', data.myLike === 'D');
                    }
                } else {
                    alert('오류 발생: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('서버 통신 중 오류가 발생했습니다.');
            });
        }

    /* ── 답글 폼 토글 ──────────────────────────────────────────── */
    function toggleReplyForm(commentId) {
        var form = document.getElementById('reply-form-' + commentId);
        if (!form) return;
        var isOpen = form.style.display !== 'none';
        form.style.display = isOpen ? 'none' : 'block';
        if (!isOpen) document.getElementById('reply-content-' + commentId).focus();
    }

    /* ── 답글 등록 AJAX ─────────────────────────────────────── */
    function submitReply(parentCommentId, postId) {
        var textarea = document.getElementById('reply-content-' + parentCommentId);
        var content  = textarea ? textarea.value.trim() : '';
        if (!content) { alert('답글 내용을 입력해 주세요.'); return; }

        var params = new URLSearchParams();
        params.append('postId', postId);
        params.append('parentCommentId', parentCommentId);
        params.append('content', content);

        fetch('${pageContext.request.contextPath}/l_check/comment/reply.do', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.status === 'ok') {
                window.location.reload();
            } else {
                alert('답글 등록 실패: ' + data.message);
            }
        })
        .catch(function() { alert('서버 통신 중 오류가 발생했습니다.'); });
    }

    /* ── 댓글 수정 폼 토글 ─────────────────────────────────── */
    function toggleEditForm(commentId) {
        var contentDiv = document.getElementById('comment-content-' + commentId);
        var editForm   = document.getElementById('edit-form-' + commentId);
        if (!contentDiv || !editForm) return;
        contentDiv.style.display = 'none';
        editForm.style.display   = 'block';
        document.getElementById('edit-content-' + commentId).focus();
    }

    function cancelEdit(commentId) {
        var contentDiv = document.getElementById('comment-content-' + commentId);
        var editForm   = document.getElementById('edit-form-' + commentId);
        if (contentDiv) contentDiv.style.display = '';
        if (editForm)   editForm.style.display   = 'none';
    }

    /* ── 댓글 수정 완료 AJAX ────────────────────────────────── */
    function submitEdit(commentId) {
        var textarea = document.getElementById('edit-content-' + commentId);
        var content  = textarea ? textarea.value.trim() : '';
        if (!content) { alert('내용을 입력해 주세요.'); return; }

        var params = new URLSearchParams();
        params.append('commentId', commentId);
        params.append('content', content);

        fetch('${pageContext.request.contextPath}/l_check/comment/update.do', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.status === 'ok') {
                window.location.reload();
            } else {
                alert('수정 실패: ' + data.message);
            }
        })
        .catch(function() { alert('서버 통신 중 오류가 발생했습니다.'); });
    }

    /* ── 댓글 삭제 AJAX ────────────────────────────────────── */
    function deleteComment(commentId) {
        if (!confirm('댓글을 삭제하시겠습니까?')) return;

        var params = new URLSearchParams();
        params.append('commentId', commentId);

        fetch('${pageContext.request.contextPath}/l_check/comment/delete.do', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.status === 'ok') {
                window.location.reload();
            } else {
                alert('삭제 실패: ' + data.message);
            }
        })
        .catch(function() { alert('서버 통신 중 오류가 발생했습니다.'); });
    }

    document.addEventListener('DOMContentLoaded', function() {
        // 3. 댓글 등록 AJAX
        const commentForm = document.getElementById('commentForm');
        if (commentForm) {
            commentForm.addEventListener('submit', function(e) {
                e.preventDefault();
                const postId = commentForm.querySelector('input[name="postId"]').value;
                const content = commentForm.querySelector('textarea[name="content"]').value;
                const params = new URLSearchParams();
                params.append('postId', postId);
                params.append('content', content);

                fetch('${pageContext.request.contextPath}/l_check/comment/write.do', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: params
                })
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'ok') {
                        window.location.reload();
                    } else {
                        alert('댓글 등록 실패: ' + data.message);
                    }
                })
                .catch(error => alert('서버 통신 중 오류가 발생했습니다.'));
            });
        }

        // 4. 게시글 신고 AJAX (복구 완료!)
        const reportPostForm = document.getElementById('reportPostForm');
        if (reportPostForm) {
            reportPostForm.addEventListener('submit', function(e) {
                e.preventDefault();
                const postId = reportPostForm.querySelector('input[name="postId"]').value;
                const category = reportPostForm.querySelector('select[name="category"]').value;
                const reasonDetail = reportPostForm.querySelector('textarea[name="reason"]').value;
                const fullReason = "[" + category + "] " + reasonDetail;

                const params = new URLSearchParams();
                params.append('postId', postId);
                params.append('reason', fullReason);

                fetch('${pageContext.request.contextPath}/l_check/report/post.do', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: params
                })
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'ok') {
                        alert('신고가 정상적으로 접수되었습니다.');
                        toggleModal(false);
                        reportPostForm.reset();
                    } else {
                        alert('신고 실패: ' + data.message);
                    }
                })
                .catch(error => console.error('Error:', error));
            });
        }
    });
</script>

<script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/dompurify/dist/purify.min.js"></script>
<script>
    (function () {
        const md     = document.getElementById('tui-content').value
        const html   = DOMPurify.sanitize(marked.parse(md), { ADD_ATTR: ['download', 'target'] })
        const viewer = document.getElementById('viewer')
        viewer.innerHTML = html

        viewer.querySelectorAll('a[href*="/resources/upload/"]').forEach(a => {
            a.setAttribute('download', a.textContent.trim())
            a.setAttribute('target', '_blank')
            a.setAttribute('rel', 'noopener noreferrer')
            a.insertAdjacentHTML('afterbegin', '<span class="file-link-icon">📎</span> ')
        })
    })()
</script>
</body>
</html>

