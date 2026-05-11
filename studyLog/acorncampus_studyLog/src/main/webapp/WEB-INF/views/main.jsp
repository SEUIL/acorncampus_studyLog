<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
                        <c:url var="postDetailUrl" value="/post/detail.do">
                            <c:param name="id" value="${post.postId}" />
                            <c:param name="parentUrl" value="${pageContext.request.contextPath}/community.do" />
                        </c:url>
                        <%-- outer: div+onclick — inner에 프로필 <a>를 중첩 앵커 없이 넣기 위함 --%>
                        <div class="popular-card"
                             onclick="location.href='${postDetailUrl}'">

                            <%-- 썸네일 영역 --%>
                            <div class="popular-card-thumb">
                                <c:choose>
                                    <c:when test="${not empty post.thumbnailUrl}">
                                        <c:choose>
                                            <c:when test="${fn:startsWith(post.thumbnailUrl, 'http')}">
                                                <img src="<c:out value='${post.thumbnailUrl}'/>" alt="">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${pageContext.request.contextPath}<c:out value='${post.thumbnailUrl}'/>" alt="">
                                            </c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="popular-card-thumb-placeholder">
                                            <i class="fa-regular fa-image"></i>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <%-- 하단 바: 제목(좌) + 아바타(우) --%>
                            <div class="popular-card-foot">
                                <span class="popular-card-title"><c:out value="${post.title}"/></span>
                                <%-- 아바타: 본인 글이면 span(비클릭), 타인 글이면 a(프로필 이동) --%>
                                <%-- 아바타 내부 공통 이미지 조각 --%>
                                <c:set var="postAvatarImg">
                                    <c:choose>
                                        <c:when test="${not empty post.authorAvatarUrl}">
                                            <c:choose>
                                                <c:when test="${fn:startsWith(post.authorAvatarUrl, 'http')}">
                                                    <img src="<c:out value='${post.authorAvatarUrl}'/>" alt="">
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="${pageContext.request.contextPath}<c:out value='${post.authorAvatarUrl}'/>" alt="">
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>
                                            <i class="fa-solid fa-user"></i>
                                        </c:otherwise>
                                    </c:choose>
                                </c:set>
                                <c:choose>
                                    <c:when test="${not empty loginUser and loginUser.userId eq post.userId}">
                                        <%-- 본인 글: 클릭 불가 --%>
                                        <span class="card-avatar" title="내 글">${postAvatarImg}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <%-- 타인 글: 프로필 이동 (stopPropagation으로 카드 onclick 차단) --%>
                                        <a class="card-avatar"
                                           href="${pageContext.request.contextPath}/user/profile.do?id=${post.userId}"
                                           onclick="event.stopPropagation()"
                                           title="<c:out value='${post.authorName}'/>의 프로필 보기">${postAvatarImg}</a>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
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
                    <c:url var="seriesDetailUrl" value="/series/detail.do">
                        <c:param name="id" value="${series.seriesId}" />
                        <c:param name="parentUrl" value="${pageContext.request.contextPath}/community.do" />
                    </c:url>
                    <a class="series-card" href="${seriesDetailUrl}">
                        <div class="series-info">
                            <h3><c:out value="${series.name}"/></h3>
                            <p><c:out value="${series.description}"/></p>
                        </div>
                        <div class="series-meta">
                            <span class="series-author-info">
                                <%-- 시리즈 아바타 내부 공통 이미지 조각 --%>
                                <c:set var="seriesAvatarImg">
                                    <c:choose>
                                        <c:when test="${not empty series.authorAvatarUrl}">
                                            <c:choose>
                                                <c:when test="${fn:startsWith(series.authorAvatarUrl, 'http')}">
                                                    <img src="<c:out value='${series.authorAvatarUrl}'/>" alt="">
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="${pageContext.request.contextPath}<c:out value='${series.authorAvatarUrl}'/>" alt="">
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>
                                            <i class="fa-solid fa-user"></i>
                                        </c:otherwise>
                                    </c:choose>
                                </c:set>
                                <%-- 시리즈 카드 자체가 <a>이므로 중첩 앵커 불가 → 타인은 span+onclick 처리 --%>
                                <c:choose>
                                    <c:when test="${not empty loginUser and loginUser.userId eq series.userId}">
                                        <%-- 본인 시리즈: 클릭 불가 --%>
                                        <span class="card-avatar" title="내 시리즈">${seriesAvatarImg}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <%-- 타인 시리즈: span+onclick으로 프로필 이동 (중첩 앵커 방지) --%>
                                        <span class="card-avatar" style="cursor:pointer;"
                                              onclick="event.preventDefault(); event.stopPropagation(); location.href='${pageContext.request.contextPath}/user/profile.do?id=${series.userId}'"
                                              title="<c:out value='${series.authorName}'/>의 프로필 보기">${seriesAvatarImg}</span>
                                    </c:otherwise>
                                </c:choose>
                                <c:out value="${series.authorName}"/>
                            </span>
                            <span><i class="fa-regular fa-file-lines"></i> <c:out value="${series.postCount}"/> Posts</span>
                        </div>
                    </a>
                </c:forEach>
            </div>
        </div>
    </main>
</div>
<script>
    /* bfcache 대응 — 뒤로 가기로 복원된 페이지는 좋아요 수가 stale할 수 있으므로 재요청
       event.persisted: 브라우저가 bfcache에서 페이지를 복원했을 때 true */
    window.addEventListener('pageshow', function (e) {
        if (e.persisted) window.location.reload();
    });
</script>
</body>
</html>

