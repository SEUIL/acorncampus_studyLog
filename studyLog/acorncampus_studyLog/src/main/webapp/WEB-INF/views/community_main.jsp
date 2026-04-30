<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>스터디로그 - 커뮤니티</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/community/community_main.css">
</head>
<body>
<div class="dashboard-wrapper">
    <aside class="sidebar">
        <a class="brand-logo" href="${pageContext.request.contextPath}/">
            <i class="fa-solid fa-book-open"></i> 스터디로그
        </a>

        <div class="grass-section">
            <span class="grass-title">Yearly Contributions</span>
            <div class="grass-grid">
                <%-- TODO: 실제 기여도 데이터로 동적 렌더링 --%>
                <div class="grass-node level-4"></div><div class="grass-node level-2"></div><div class="grass-node"></div><div class="grass-node level-1"></div><div class="grass-node level-3"></div><div class="grass-node"></div><div class="grass-node level-2"></div>
                <div class="grass-node level-1"></div><div class="grass-node level-4"></div><div class="grass-node"></div><div class="grass-node level-2"></div><div class="grass-node level-3"></div><div class="grass-node level-1"></div><div class="grass-node"></div>
                <div class="grass-node level-4"></div><div class="grass-node level-2"></div><div class="grass-node"></div><div class="grass-node level-1"></div><div class="grass-node level-3"></div><div class="grass-node"></div><div class="grass-node level-2"></div>
            </div>
        </div>

        <div class="profile-section">
            <div class="profile-avatar">
                <i class="fa-solid fa-user"></i>
            </div>
            <div class="profile-info">
                <h2><c:out value="${loginUser.username}"/></h2>
                <p>Learning &amp; Recording...</p>
            </div>
        </div>

        <ul class="nav-menu">
            <li onclick="location.href='${pageContext.request.contextPath}/l_check/user/mypage.do'">
                <i class="fa-solid fa-layer-group"></i> 내 시리즈
            </li>
            <li class="active"><i class="fa-solid fa-globe"></i> 커뮤니티 탐색</li>
            <c:if test="${loginUser.role eq 'ADMIN'}">
                <li class="nav-admin" onclick="location.href='${pageContext.request.contextPath}/admin/main.do'">
                    <i class="fa-solid fa-user-shield"></i> 관리자 모드
                </li>
            </c:if>
        </ul>

        <div class="logout-btn">
            <button class="btn btn-outline" style="width: 100%; font-size: 12px;"
                    onclick="document.body.classList.toggle('dark-theme')">
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
                <a class="breadcrumb" href="${pageContext.request.contextPath}/l_check/user/mypage.do">
                    <i class="fa-solid fa-house"></i> Home <span style="color: var(--border-color);">/</span> 커뮤니티
                </a>
                <button class="btn btn-outline" style="padding: 6px 12px; font-size: 13px;"
                        onclick="location.href='${pageContext.request.contextPath}/l_check/user/mypage.do'">
                    <i class="fa-solid fa-desktop"></i> 내 작업 공간으로
                </button>
            </div>
            <div class="nav-right">
                <button class="icon-btn" title="설정"
                        onclick="location.href='${pageContext.request.contextPath}/l_check/user/update.do'">
                    <i class="fa-solid fa-gear"></i>
                </button>
            </div>
        </div>

        <div class="community-top-section">
            <div class="popular-section">
                <div class="section-header">
                    <h2><i class="fa-solid fa-fire" style="color: #FF7043;"></i> 이번 주 인기 게시글</h2>
                    <button class="btn btn-outline" style="padding: 4px 10px; font-size: 12px;"
                            onclick="location.href='${pageContext.request.contextPath}/post/list.do'">
                        전체 게시판 보기 <i class="fa-solid fa-arrow-right"></i>
                    </button>
                </div>
                <div class="popular-grid">
                    <c:forEach var="post" items="${popularPosts}">
                        <a class="popular-card" href="${pageContext.request.contextPath}/post/detail.do?id=${post.postId}">
                            <h3><c:out value="${post.title}"/></h3>
                            <div class="popular-meta">
                                <span>by <c:out value="${post.authorName}"/></span>
                                <div class="stats">
                                    <span><i class="fa-solid fa-heart"></i> <c:out value="${post.likeCount}"/></span>
                                    <span><i class="fa-solid fa-comment"></i> <c:out value="${post.commentCount}"/></span>
                                </div>
                            </div>
                        </a>
                    </c:forEach>
                </div>
            </div>

            <div class="search-sidebar">
                <div class="section-header">
                    <h2><i class="fa-solid fa-magnifying-glass"></i> 통합 검색</h2>
                </div>
                <form class="search-panel" action="${pageContext.request.contextPath}/search.do" method="get">
                    <div class="search-input-wrapper">
                        <i class="fa-solid fa-align-left"></i>
                        <input type="text" name="q" placeholder="게시글 제목 검색"
                               value="<c:out value='${param.q}'/>">
                    </div>
                    <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 5px;">검색하기</button>
                </form>
            </div>
        </div>

        <div class="community-feed">
            <div class="feed-header">
                <h3>주목받는 인기 시리즈</h3>
                <button class="btn btn-outline" style="padding: 4px 10px; font-size: 12px;"
                        onclick="location.href='${pageContext.request.contextPath}/series/list.do'">
                    전체 시리즈 보기 <i class="fa-solid fa-arrow-right"></i>
                </button>
            </div>
            <div class="series-grid" style="padding: 20px;">
                <c:forEach var="series" items="${popularSeries}">
                    <a class="series-card" href="${pageContext.request.contextPath}/series/detail.do?id=${series.seriesId}">
                        <div class="series-info">
                            <h3><c:out value="${series.name}"/></h3>
                            <p><c:out value="${series.description}"/></p>
                        </div>
                        <div class="series-meta">
                            <span><i class="fa-solid fa-circle-user"></i> <c:out value="${series.authorName}"/></span>
                            <span><i class="fa-regular fa-file-lines"></i> <c:out value="${series.postCount}"/> Posts</span>
                        </div>
                    </a>
                </c:forEach>
            </div>
        </div>
    </main>
</div>
</body>
</html>

