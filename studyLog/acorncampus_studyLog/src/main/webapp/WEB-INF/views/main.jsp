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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/ui.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/series.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/community/community_main.css">
</head>
<body>
<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sideBar.jsp">
        <jsp:param name="activeMenu" value="community"/>
    </jsp:include>

    <main class="main-content">
        <jsp:include page="/WEB-INF/views/common/header.jsp">
            <jsp:param name="activeMenu" value="community"/>
        </jsp:include>

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
                        <input type="text" name="q" placeholder="키워드 또는 #태그명으로 검색"
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

