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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/post/post_detail.css">
</head>
<body>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/sideBar.jsp" %>
<div class="dashboard-wrapper">
    <aside class="sidebar">
        <a class="brand-logo" href="${pageContext.request.contextPath}/">
            <i class="fa-solid fa-book-open"></i> 스터디로그
        </a>
        <ul class="nav-menu" style="margin-top: 30px;">
            <li onclick="location.href='${pageContext.request.contextPath}/l_check/user/mypage.do'"><i class="fa-solid fa-layer-group"></i> 내 시리즈</li>
            <li onclick="location.href='${pageContext.request.contextPath}/post/list.do'"><i class="fa-solid fa-globe"></i> 커뮤니티 탐색</li>
        </ul>
    </aside>

    <main class="main-content">
        <div class="top-bar">
            <a class="breadcrumb" href="${pageContext.request.contextPath}/post/list.do">
                <i class="fa-solid fa-arrow-left"></i> 목록으로 돌아가기
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
                            <c:out value="${post.authorName}"/>
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
                            <a class="tag-chip" href="${pageContext.request.contextPath}/tag/post.do?tag=${tag.name}">
                                #<c:out value="${tag.name}"/>
                            </a>
                        </c:forEach>
                    </div>
                </c:if>
            </header>

            <div class="post-body">
                <c:out value="${post.content}" escapeXml="false"/>
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
                            <li class="comment-item">
                                <div class="comment-meta">
                                    <span class="comment-author"><c:out value="${comment.authorName}"/></span>
                                    <span class="comment-date"><c:out value="${comment.createdAt}"/></span>
                                </div>
                                <div class="comment-content"><c:out value="${comment.content}"/></div>

                                <c:if test="${not empty comment.replies}">
                                    <div class="reply-list">
                                        <c:forEach var="reply" items="${comment.replies}">
                                            <div class="reply-item">
                                                <div class="comment-meta">
                                                    <span class="comment-author"><c:out value="${reply.authorName}"/></span>
                                                    <span class="comment-date"><c:out value="${reply.createdAt}"/></span>
                                                </div>
                                                <div class="comment-content"><c:out value="${reply.content}"/></div>
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
        <form action="${pageContext.request.contextPath}/l_check/report/post.do" method="post">
            <div class="modal-body">
                <input type="hidden" name="postId" value="${post.postId}">
                <label for="reportCategory">신고 사유 카테고리</label>
                <select class="modal-select" id="reportCategory">
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
    // 기존에 있던 모달 함수
    function toggleModal(show) {
        document.getElementById('reportModal').style.display = show ? 'flex' : 'none';
    }

    // ── 여기부터 댓글 AJAX 로직 추가 ──

    document.addEventListener('DOMContentLoaded', function() {
        const commentForm = document.getElementById('commentForm');

        // 폼이 존재할 때만 이벤트 리스너 등록 (비로그인 상태 방어)
        if (commentForm) {
            commentForm.addEventListener('submit', function(e) {
                // 1. 폼의 기본 동작(페이지 강제 새로고침/이동) 방지
                e.preventDefault();

                // 2. 입력된 데이터 가져오기
                const postId = commentForm.querySelector('input[name="postId"]').value;
                const content = commentForm.querySelector('textarea[name="content"]').value;

                // 3. Controller(req.getParameter)가 읽을 수 있도록 Form 데이터 형식으로 변환
                const params = new URLSearchParams();
                params.append('postId', postId);
                params.append('content', content);

                // 4. AJAX(fetch) 전송
                fetch('${pageContext.request.contextPath}/l_check/comment/write.do', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded' // 폼 데이터 명시
                    },
                    body: params
                })
                .then(response => response.json()) // Controller가 보낸 JSON 파싱
                .then(data => {
                    if (data.status === 'ok') {
                        // 성공 시 화면을 새로고침하여 등록된 댓글이 보이도록 함
                        window.location.reload();
                    } else {
                        // 실패 시 Controller에서 보낸 에러 메시지 팝업
                        alert('댓글 등록 실패: ' + data.message);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('서버 통신 중 오류가 발생했습니다.');
                });
            });
        }
    });
</script>
</body>
</html>

