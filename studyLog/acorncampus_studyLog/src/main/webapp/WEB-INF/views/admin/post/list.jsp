<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>스터디로그 관리자 - 게시글 관리</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/admin/admin_post_list.css">
</head>
<body>
<div class="dashboard-wrapper">
    <aside class="sidebar">
        <a class="brand-logo" href="${pageContext.request.contextPath}/admin/main.do"><i class="fa-solid fa-user-shield"></i> 스터디로그 Admin</a>
        <ul class="nav-menu" style="margin-top: 30px;">
            <li onclick="location.href='${pageContext.request.contextPath}/admin/main.do'"><i class="fa-solid fa-chart-pie"></i> 대시보드 메인</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/user/list.do'"><i class="fa-solid fa-users"></i> 회원 관리</li>
            <li class="active" onclick="location.href='${pageContext.request.contextPath}/admin/post/list.do'"><i class="fa-solid fa-file-lines"></i> 게시글 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/report/list.do'"><i class="fa-solid fa-triangle-exclamation"></i> 신고 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/tag/list.do'"><i class="fa-solid fa-tags"></i> 태그 관리</li>
        </ul>
    </aside>
    <main class="main-content">
        <div class="admin-header">
            <h1><i class="fa-solid fa-file-lines"></i> 게시글 및 시리즈 관리</h1>
            <p class="text-sub" style="margin-top: 10px;">커뮤니티 화면을 기반으로 관리자 전용 제어 액션을 추가한 뷰입니다.</p>
        </div>

        <div class="board-tabs">
            <div class="board-tab active">게시글 관리</div>
        </div>

        <form class="controls-bar" action="${pageContext.request.contextPath}/admin/post/list.do" method="get">
            <div class="controls-left">
                <input type="text" name="keyword" class="admin-input" placeholder="제목, 작성자 검색" style="width: 300px;" value="<c:out value='${param.keyword}'/>">
                <button class="btn-sm" style="background: var(--text-main); color: var(--bg-card);">검색</button>
            </div>
            <div class="controls-right">
                <select class="admin-select" name="status">
                    <option value="">상태 : 전체</option>
                    <option value="Y" ${param.status eq 'Y' ? 'selected' : ''}>상태 : 공개</option>
                    <option value="N" ${param.status eq 'N' ? 'selected' : ''}>상태 : 비공개</option>
                </select>
            </div>
        </form>

        <c:set var="postItems" value="${not empty postList ? postList : posts}" />
        <div class="admin-table-wrapper">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>글번호</th><th>시리즈 분류</th><th>제목</th><th>작성자</th><th>작성일</th><th>조회수</th><th>상태</th><th>관리자 기능</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty postItems}">
                        <c:forEach var="post" items="${postItems}">
                            <tr>
                                <td><c:out value="${post.postId}"/></td>
                                <td><c:out value="${empty post.seriesName ? '자유게시판' : post.seriesName}"/></td>
                                <td style="text-align:left; font-weight: 600;">
                                    <a class="post-link" href="${pageContext.request.contextPath}/post/detail.do?id=${post.postId}">
                                        <c:out value="${post.title}"/>
                                    </a>
                                </td>
                                <td><c:out value="${post.authorName}"/></td>
                                <td><c:out value="${post.createdAt}"/></td>
                                <td><c:out value="${post.viewCount}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${post.isPublic eq 'Y'}"><span class="tag">공개</span></c:when>
                                        <c:otherwise><span class="tag">비공개</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <form action="${pageContext.request.contextPath}/admin/post/delete.do" method="post" style="display:inline;">
                                        <input type="hidden" name="postId" value="${post.postId}">
                                        <button type="submit" class="btn-sm btn-danger">강제 삭제</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="8">조회된 게시글이 없습니다.</td></tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </main>
</div>
<%-- 관리자 화면 표시용 전환 스크립트 --%>
<script src="${pageContext.request.contextPath}/resources/js/page-transition.js"></script>
</body>
</html>

