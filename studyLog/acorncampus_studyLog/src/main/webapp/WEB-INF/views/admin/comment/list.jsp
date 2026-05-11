<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>스터디로그 관리자 - 댓글 관리</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/common/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/form.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/table.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/admin/admin_report_list.css">
</head>
<body>
<div class="dashboard-wrapper">
    <aside class="sidebar">
        <a class="brand-logo" href="${pageContext.request.contextPath}/admin/main.do"><i class="fa-solid fa-user-shield"></i> 스터디로그 Admin</a>
        <ul class="nav-menu" style="margin-top: 30px;">
            <li onclick="location.href='${pageContext.request.contextPath}/admin/main.do'"><i class="fa-solid fa-chart-pie"></i> 대시보드 메인</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/user/list.do'"><i class="fa-solid fa-users"></i> 회원 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/post/list.do'"><i class="fa-solid fa-file-lines"></i> 게시글 관리</li>
            <li class="active" onclick="location.href='${pageContext.request.contextPath}/admin/comment/list.do'"><i class="fa-solid fa-comments"></i> 댓글 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/report/list.do'"><i class="fa-solid fa-triangle-exclamation"></i> 신고 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/tag/list.do'"><i class="fa-solid fa-tags"></i> 태그 관리</li>
        </ul>
    </aside>

    <main class="main-content">
        <div class="admin-header">
            <h1 style="background: var(--bg-card); padding: 15px 30px; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">댓글 목록</h1>
        </div>

        <c:set var="commentItems" value="${not empty commentList ? commentList : comments}" />
        <div class="admin-table-wrapper">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>댓글ID</th>
                    <th>게시글ID</th>
                    <th>작성자</th>
                    <th>내용</th>
                    <th>작성일</th>
                    <th>관리</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty commentItems}">
                        <c:forEach var="comment" items="${commentItems}">
                            <tr>
                                <td><c:out value="${comment.commentId}"/></td>
                                <td>
                                    <a class="post-link" href="${pageContext.request.contextPath}/post/detail.do?id=${comment.postId}">
                                        <c:out value="${comment.postId}"/>
                                    </a>
                                </td>
                                <td><c:out value="${comment.authorName}"/></td>
                                <td class="reason-box" style="text-align:left;"><c:out value="${comment.content}"/></td>
                                <td><c:out value="${comment.createdAt}"/></td>
                                <td>
                                    <form action="${pageContext.request.contextPath}/admin/comment/delete.do" method="post" data-ajax-form="true">
                                        <input type="hidden" name="commentId" value="${comment.commentId}">
                                        <button type="submit" class="btn-sm btn-danger">삭제</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="6">댓글 내역이 없습니다.</td></tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </main>
</div>
<script src="${pageContext.request.contextPath}/resources/js/page-transition.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        document.querySelectorAll('form[data-ajax-form="true"]').forEach(function(form) {
            form.addEventListener('submit', function(e) {
                e.preventDefault();

                const params = new URLSearchParams(new FormData(form));

                fetch(form.action, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: params
                })
                .then(response => response.json())
                .then(data => {
                    alert(data.message);
                    if (data.status === 'ok') {
                        window.location.reload();
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('서버 통신 중 오류가 발생했습니다.');
                });
            });
        });
    });
</script>
</body>
</html>
