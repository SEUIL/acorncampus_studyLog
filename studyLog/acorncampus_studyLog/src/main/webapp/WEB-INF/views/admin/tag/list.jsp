<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>스터디로그 관리자 - 태그 관리</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/admin/admin_tag_list.css">
</head>
<body>
<div class="dashboard-wrapper">
    <aside class="sidebar">
        <a class="brand-logo" href="${pageContext.request.contextPath}/admin/main.do"><i class="fa-solid fa-user-shield"></i> 스터디로그 Admin</a>
        <ul class="nav-menu" style="margin-top: 30px;">
            <li onclick="location.href='${pageContext.request.contextPath}/admin/main.do'"><i class="fa-solid fa-chart-pie"></i> 대시보드 메인</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/user/list.do'"><i class="fa-solid fa-users"></i> 회원 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/post/list.do'"><i class="fa-solid fa-file-lines"></i> 게시글 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/report/list.do'"><i class="fa-solid fa-triangle-exclamation"></i> 신고 관리</li>
            <li class="active" onclick="location.href='${pageContext.request.contextPath}/admin/tag/list.do'"><i class="fa-solid fa-tags"></i> 태그 관리</li>
        </ul>
    </aside>

    <main class="main-content">
        <div class="admin-header">
            <h1>태그 관리</h1>
            <button class="btn-sm" style="padding: 10px 20px; font-size: 14px;">유해 태그 관리</button>
        </div>

        <div class="controls-bar">
            <div class="tag-tabs">
                <div class="tag-tab active">사용빈도순</div>
                <div class="tag-tab">최신 등록</div>
                <div class="tag-tab">이름순</div>
            </div>
            <form action="${pageContext.request.contextPath}/admin/tag/list.do" method="get" style="display:flex; gap:10px;">
                <input type="text" name="keyword" class="admin-input" placeholder="검색" value="<c:out value='${param.keyword}'/>">
                <select class="admin-select" name="sort">
                    <option value="count">사용빈도</option>
                    <option value="latest">최신순</option>
                    <option value="name">이름순</option>
                </select>
            </form>
        </div>

        <c:set var="tagItems" value="${not empty tagList ? tagList : tags}" />
        <div class="admin-table-wrapper">
            <table class="admin-table">
                <thead>
                <tr>
                    <th style="width: 35%;">태그 이름</th>
                    <th style="width: 35%;">사용 횟수</th>
                    <th style="width: 30%;">관리</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty tagItems}">
                        <c:forEach var="tag" items="${tagItems}">
                            <tr>
                                <td>#<c:out value="${tag.name}"/></td>
                                <td><c:out value="${tag.postCount}"/>회</td>
                                <td>
                                    <form action="${pageContext.request.contextPath}/admin/tag/delete.do" method="post" style="display:inline;">
                                        <input type="hidden" name="tagId" value="${tag.tagId}">
                                        <button type="submit" class="btn-sm btn-danger">삭제</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="3">등록된 태그가 없습니다.</td></tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </main>
</div>
</body>
</html>

