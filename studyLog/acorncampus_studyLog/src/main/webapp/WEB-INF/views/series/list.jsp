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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/series/series_list.css">
</head>
<body>
<div class="dashboard-wrapper">
    <aside class="sidebar">
        <a class="brand-logo" href="${pageContext.request.contextPath}/">
            <i class="fa-solid fa-book-open"></i> 스터디로그
        </a>
        <div class="profile-section" style="margin-top:20px;">
            <div class="profile-avatar"><i class="fa-solid fa-user"></i></div>
            <div class="profile-info">
                <h2><c:out value="${loginUser.username}"/></h2>
                <p>Learning &amp; Recording...</p>
            </div>
        </div>
        <ul class="nav-menu" style="margin-top: 30px;">
            <li onclick="location.href='${pageContext.request.contextPath}/l_check/user/mypage.do'"><i class="fa-solid fa-layer-group"></i> 내 시리즈</li>
            <li class="active"><i class="fa-solid fa-globe"></i> 커뮤니티 탐색</li>
        </ul>
        <div style="margin-top: auto; border-top: 1px solid var(--border-color); padding-top: 20px;">
            <button class="btn btn-outline" style="width: 100%; font-size: 13px; margin-bottom:10px;" onclick="document.body.classList.toggle('dark-theme')">
                <i class="fa-solid fa-moon"></i> 테마 변경
            </button>
            <div style="margin-top: 15px; font-size: 13px; font-weight: 600; color: var(--text-sub); cursor: pointer; text-align: center;"
                 onclick="location.href='${pageContext.request.contextPath}/user/logout.do'">
                <i class="fa-solid fa-arrow-right-from-bracket"></i> 로그아웃
            </div>
        </div>
    </aside>

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
            <a class="board-tab" href="${pageContext.request.contextPath}/post/list.do">게시글</a>
            <a class="board-tab active" href="${pageContext.request.contextPath}/series/list.do">시리즈</a>
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

