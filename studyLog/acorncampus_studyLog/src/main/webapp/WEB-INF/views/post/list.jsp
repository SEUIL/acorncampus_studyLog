<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>스터디로그 - 커뮤니티 게시글</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/form.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/table.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/tabs.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/ui.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/post/post_list.css">
</head>
<body>
<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sideBar.jsp">
        <jsp:param name="activeMenu" value="community"/>
    </jsp:include>

    <main class="main-content">
        <div class="top-bar">
            <div class="nav-left">
                <a class="breadcrumb" href="${pageContext.request.contextPath}/community.do">
                    <i class="fa-solid fa-house"></i> 커뮤니티 <span style="color: var(--border-color);">/</span> 전체 게시글
                </a>
            </div>
            <button class="icon-btn" title="설정"><i class="fa-solid fa-gear"></i></button>
        </div>

        <div class="page-header">
            <h1><i class="fa-solid fa-list"></i> 전체 게시글</h1>
        </div>

        <div class="board-tabs">
            <a class="board-tab active" href="${pageContext.request.contextPath}/post/list.do?keyword=${param.keyword}">게시글</a>
            <a class="board-tab" href="${pageContext.request.contextPath}/series/list.do?keyword=${param.keyword}">시리즈</a>
        </div>

        <form class="controls-bar" action="${pageContext.request.contextPath}/post/list.do" method="get">
            <div class="controls-left">
                <input type="text" name="keyword" class="admin-input" placeholder="제목, 작성자, 태그 검색" style="width: 300px;" value="<c:out value='${param.keyword}'/>">
                <button class="btn-sm" style="background: var(--text-main); color: var(--bg-card);">검색</button>
            </div>
            <div class="controls-right">
                <select class="admin-select" name="sort">
                    <option value="latest" ${param.sort eq 'latest' ? 'selected' : ''}>정렬 : 최신순</option>
                    <option value="views" ${param.sort eq 'views' ? 'selected' : ''}>정렬 : 조회수</option>
                    <option value="likes" ${param.sort eq 'likes' ? 'selected' : ''}>정렬 : 좋아요순</option>
                </select>
            </div>
        </form>

        <c:set var="postItems" value="${not empty postList ? postList : posts}" />
        <div class="admin-table-wrapper">
            <table class="admin-table">
                <thead>
                <tr>
                    <th style="width: 80px;">글번호</th>
                    <th style="width: 150px;">분류/시리즈</th>
                    <th>제목</th>
                    <th style="width: 120px;">작성자</th>
                    <th style="width: 140px;">작성일</th>
                    <th style="width: 80px;">조회수</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty postItems}">
                        <c:forEach var="post" items="${postItems}">
                            <tr>
                                <td><c:out value="${post.postId}"/></td>
                                <td><c:out value="${empty post.seriesName ? '자유게시판' : post.seriesName}"/></td>
                                <td style="text-align:left;">
                                    <c:url var="postDetailUrl" value="/post/detail.do">
                                        <c:param name="id" value="${post.postId}" />
                                        <c:param name="parentUrl" value="${currentUrl}" />
                                    </c:url>
                                    <a class="post-link" href="${postDetailUrl}">
                                        <c:out value="${post.title}"/>
                                    </a>
                                </td>
                                <td><c:out value="${post.authorName}"/></td>
                                <td><c:out value="${post.createdAt}"/></td>
                                <td><c:out value="${post.viewCount}"/></td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="6" class="empty-row">조건에 맞는 게시글이 없습니다.</td>
                        </tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </main>
</div>
</body>
</html>

