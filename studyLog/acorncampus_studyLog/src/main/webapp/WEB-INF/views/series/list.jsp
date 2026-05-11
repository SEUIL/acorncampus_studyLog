<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>스터디로그 - 커뮤니티 시리즈</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/form.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/tabs.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/series.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/ui.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/series/series_list.css">
</head>
<body>
<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sideBar.jsp">
        <jsp:param name="activeMenu" value="community"/>
    </jsp:include>

    <main class="main-content">
        <div class="top-bar">
            <div class="nav-left">
                <a class="breadcrumb" href="${pageContext.request.contextPath}/">
                    <i class="fa-solid fa-house"></i> 커뮤니티 <span style="color: var(--border-color);">/</span> 전체 시리즈
                </a>
            </div>
            <button class="icon-btn" title="설정"><i class="fa-solid fa-gear"></i></button>
        </div>

        <div class="page-header">
            <h1><i class="fa-solid fa-list"></i> 전체 시리즈</h1>
        </div>

        <div class="board-tabs">
            <a class="board-tab" href="${pageContext.request.contextPath}/post/list.do?keyword=${param.keyword}">게시글</a>
            <a class="board-tab active" href="${pageContext.request.contextPath}/series/list.do?keyword=${param.keyword}">시리즈</a>
        </div>

        <form class="controls-bar" action="${pageContext.request.contextPath}/series/list.do" method="get">
            <div class="controls-left">
                <input type="text" name="keyword" class="admin-input" placeholder="시리즈명, 작성자, 태그 검색" style="width: 300px;" value="<c:out value='${param.keyword}'/>">
                <button class="btn-sm" style="background: var(--text-main); color: var(--bg-card);">검색</button>
            </div>
            <div class="controls-right">
                <select class="admin-select" name="sort">
                    <option value="latest" ${param.sort eq 'latest' ? 'selected' : ''}>정렬 : 최신순</option>
                    <option value="popular" ${param.sort eq 'popular' ? 'selected' : ''}>정렬 : 인기순</option>
                    <option value="posts" ${param.sort eq 'posts' ? 'selected' : ''}>정렬 : 글 많은 순</option>
                </select>
            </div>
        </form>

        <c:set var="seriesItems" value="${not empty seriesList ? seriesList : series}" />
        <c:choose>
            <c:when test="${not empty seriesItems}">
                <div class="series-grid">
                    <c:forEach var="item" items="${seriesItems}">
                        <a class="series-card" href="${pageContext.request.contextPath}/series/detail.do?id=${item.seriesId}">
                            <div class="series-info">
                                <h3><c:out value="${item.name}"/></h3>
                                <p><c:out value="${item.description}"/></p>
                            </div>
                            <div class="series-meta">
                                <span class="series-author"><i class="fa-solid fa-circle-user"></i> <c:out value="${item.authorName}"/></span>
                                <div class="series-stats">
                                    <span><i class="fa-regular fa-file-lines"></i> <c:out value="${item.postCount}"/></span>
                                </div>
                            </div>
                        </a>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">노출할 시리즈가 없습니다.</div>
            </c:otherwise>
        </c:choose>
    </main>
</div>
</body>
</html>

