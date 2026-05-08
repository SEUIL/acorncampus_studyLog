<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>스터디로그 - <c:out value="${targetUser.nickname}"/>의 프로필</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/ui.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/series.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/user/public_profile.css">
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

        <%-- ① 프로필 헤더 카드 --%>
        <div class="profile-header-card">
            <div class="profile-header-avatar">
                <c:choose>
                    <c:when test="${not empty targetUser.avatarUrl}">
                        <c:choose>
                            <c:when test="${fn:startsWith(targetUser.avatarUrl, 'http')}">
                                <img src="<c:out value='${targetUser.avatarUrl}'/>" alt="프로필 사진">
                            </c:when>
                            <c:otherwise>
                                <img src="${pageContext.request.contextPath}<c:out value='${targetUser.avatarUrl}'/>" alt="프로필 사진">
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <i class="fa-solid fa-user"></i>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="profile-header-info">
                <h1 class="profile-header-name"><c:out value="${targetUser.nickname}"/></h1>
                <c:if test="${not empty targetUser.bio}">
                    <p class="profile-header-bio"><c:out value="${targetUser.bio}"/></p>
                </c:if>
            </div>
        </div>

        <%-- ② 시리즈 섹션 --%>
        <section class="profile-series-section">
            <div class="profile-section-header">
                <h2><i class="fa-solid fa-layer-group"></i> 시리즈</h2>
                <span class="count-badge">${fn:length(seriesList)}</span>
            </div>

            <c:choose>
                <c:when test="${not empty seriesList}">
                    <div class="series-grid">
                        <c:forEach var="series" items="${seriesList}">
                            <a class="series-card"
                               href="${pageContext.request.contextPath}/series/detail.do?id=${series.seriesId}">
                                <div class="series-info">
                                    <h3><c:out value="${series.name}"/></h3>
                                    <p><c:out value="${series.description}"/></p>
                                </div>
                                <div class="series-meta">
                                    <span><i class="fa-regular fa-file-lines"></i>
                                        <c:out value="${series.postCount}"/> Posts</span>
                                    <span class="tag"><c:out value="${series.name}"/></span>
                                </div>
                            </a>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="profile-empty">아직 등록된 시리즈가 없습니다.</p>
                </c:otherwise>
            </c:choose>
        </section>

        <%-- ③ 공개 게시글 섹션 (시리즈 없는 글 포함 전체 공개 글) --%>
        <section class="profile-posts-section">
            <%-- 시리즈 없는 공개 글 수 카운트 --%>
            <c:set var="looseCount" value="0"/>
            <c:forEach var="post" items="${postList}">
                <c:if test="${empty post.seriesId}">
                    <c:set var="looseCount" value="${looseCount + 1}"/>
                </c:if>
            </c:forEach>

            <div class="profile-section-header">
                <h2><i class="fa-regular fa-file-lines"></i> 시리즈 없는 글</h2>
                <span class="count-badge">${looseCount}</span>
            </div>

            <c:choose>
                <c:when test="${looseCount > 0}">
                    <div class="profile-post-list">
                        <c:forEach var="post" items="${postList}">
                            <c:if test="${empty post.seriesId}">
                                <div class="profile-post-item">
                                    <a class="profile-post-title"
                                       href="${pageContext.request.contextPath}/post/detail.do?id=${post.postId}"
                                       title="<c:out value='${post.title}'/>">
                                        <c:out value="${post.title}"/>
                                    </a>
                                    <div class="profile-post-meta">
                                        <span>${post.createdAt}</span>
                                    </div>
                                </div>
                            </c:if>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="profile-empty">시리즈에 속하지 않은 공개 글이 없습니다.</p>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</div>
</body>
</html>
