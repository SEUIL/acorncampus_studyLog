<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>스터디로그 관리자 - 회원 관리</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/form.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/table.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/admin/admin_user_list.css">
</head>
<body>
<div class="dashboard-wrapper">
    <aside class="sidebar">
        <a class="brand-logo" href="${pageContext.request.contextPath}/admin/main.do"><i class="fa-solid fa-user-shield"></i> 스터디로그 Admin</a>
        <ul class="nav-menu" style="margin-top: 30px;">
            <li onclick="location.href='${pageContext.request.contextPath}/admin/main.do'"><i class="fa-solid fa-chart-pie"></i> 대시보드 메인</li>
            <li class="active" onclick="location.href='${pageContext.request.contextPath}/admin/user/list.do'"><i class="fa-solid fa-users"></i> 회원 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/post/list.do'"><i class="fa-solid fa-file-lines"></i> 게시글 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/report/list.do'"><i class="fa-solid fa-triangle-exclamation"></i> 신고 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/tag/list.do'"><i class="fa-solid fa-tags"></i> 태그 관리</li>
        </ul>
    </aside>

    <main class="main-content">
        <div class="admin-header">
            <h1><i class="fa-solid fa-users"></i> 회원 리스트</h1>
        </div>

        <div class="stats-banner">
            <div class="stat-item">전체 회원수 <span><c:out value="${totalUserCount}"/></span></div>
            <div class="stat-item">정상 <span><c:out value="${activeUserCount}"/></span></div>
            <div class="stat-item" style="color: #ef5350;">정지 <span><c:out value="${bannedUserCount}"/></span></div>
            <div class="stat-item">탈퇴 <span><c:out value="${deletedUserCount}"/></span></div>
        </div>

        <div class="big-graph"><c:out value="${empty userStatsGraphLabel ? '그래프 및 통계' : userStatsGraphLabel}"/></div>

        <form class="controls-bar" action="${pageContext.request.contextPath}/admin/user/list.do" method="get">
            <div class="controls-left">
                <input type="text" name="keyword" class="admin-input" placeholder="이름, 이메일, ID 검색" style="width: 250px;" value="<c:out value='${param.keyword}'/>">
                <button class="btn-sm" style="background: var(--text-main); color: var(--bg-card);">검색</button>
            </div>
            <div class="controls-right">
                <select class="admin-select" name="status">
                    <option value="">상태 : 전체</option>
                    <option value="banned" ${param.status eq 'banned' ? 'selected' : ''}>상태 : 정지</option>
                    <option value="active" ${param.status eq 'active' ? 'selected' : ''}>상태 : 정상</option>
                </select>
            </div>
        </form>

        <c:set var="userItems" value="${not empty userList ? userList : users}" />
        <div class="admin-table-wrapper">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>회원ID</th><th>아이디</th><th>이메일</th><th>권한</th><th>상태</th><th>관리</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty userItems}">
                        <c:forEach var="user" items="${userItems}">
                            <tr>
                                <td><c:out value="${user.userId}"/></td>
                                <td><c:out value="${user.username}"/></td>
                                <td><c:out value="${user.email}"/></td>
                                <td><c:out value="${user.role}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${user.isBanned eq 'Y'}">정지</c:when>
                                        <c:otherwise>정상</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="action-group">
                                        <c:choose>
                                            <c:when test="${user.isBanned eq 'Y'}">
                                                <form action="${pageContext.request.contextPath}/admin/user/unban.do" method="post">
                                                    <input type="hidden" name="userId" value="${user.userId}">
                                                    <button type="submit" class="btn-sm">정지 해제</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <form action="${pageContext.request.contextPath}/admin/user/ban.do" method="post">
                                                    <input type="hidden" name="userId" value="${user.userId}">
                                                    <button type="submit" class="btn-sm">계정 정지</button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>
                                        <form action="${pageContext.request.contextPath}/admin/user/delete.do" method="post">
                                            <input type="hidden" name="userId" value="${user.userId}">
                                            <button type="submit" class="btn-sm btn-danger">회원 삭제</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="6">조회된 회원이 없습니다.</td></tr>
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

