<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>스터디로그 - 시리즈 상세</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/ui.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/series/series_detail.css">
</head>
<body>
<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sideBar.jsp">
        <jsp:param name="activeMenu" value="mypage"/>
    </jsp:include>

    <main class="main-content">
        <div class="top-bar">
            <a class="breadcrumb" href="${pageContext.request.contextPath}/l_check/user/mypage.do">
                <i class="fa-solid fa-arrow-left"></i> 돌아가기
            </a>
            <button class="btn btn-primary" onclick="location.href='${pageContext.request.contextPath}/l_check/post/write.do?seriesId=${series.seriesId}'">
                <i class="fa-solid fa-plus"></i> 새 글 추가
            </button>
        </div>

        <div class="series-header">
            <div class="series-title-area">
                <h1><c:out value="${series.name}"/></h1>
                <p><c:out value="${series.description}"/></p>
                <div class="series-meta">
                    <span><i class="fa-solid fa-circle-user"></i> <c:out value="${series.authorName}"/></span>
                    <span><i class="fa-regular fa-file-lines"></i> <c:out value="${series.postCount}"/>개 글</span>
                    <span>
                        <c:choose>
                            <c:when test="${series.isPublic eq 'Y'}"><i class="fa-solid fa-lock-open"></i> 공개 시리즈</c:when>
                            <c:otherwise><i class="fa-solid fa-lock"></i> 비공개 시리즈</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>
            <c:if test="${not empty loginUser and (loginUser.userId eq series.userId or loginUser.role eq 'ADMIN')}">
                <div class="series-actions">
                    <a class="btn btn-outline" href="${pageContext.request.contextPath}/l_check/series/update.do?id=${series.seriesId}">
                        <i class="fa-solid fa-gear"></i> 설정
                    </a>
                </div>
            </c:if>
        </div>

        <c:set var="seriesPosts" value="${not empty series.postList ? series.postList : postList}" />
        <ul class="post-list">
            <c:choose>
                <c:when test="${not empty seriesPosts}">
                    <c:forEach var="post" items="${seriesPosts}" varStatus="status">
                        <li class="post-item">
                            <a class="post-info" href="${pageContext.request.contextPath}/post/detail.do?id=${post.postId}">
                                <div class="post-number">${status.index + 1}</div>
                                <div>
                                    <span class="post-title"><c:out value="${post.title}"/></span>
                                    <span class="post-date"><c:out value="${post.createdAt}"/></span>
                                </div>
                            </a>
                            <c:if test="${not empty loginUser and (loginUser.userId eq post.userId or loginUser.role eq 'ADMIN')}">
                                <div class="post-actions">
                                    <a class="action-btn" href="${pageContext.request.contextPath}/l_check/post/update.do?id=${post.postId}">수정</a>
                                    <form action="${pageContext.request.contextPath}/l_check/post/delete.do" method="post" style="display:inline;">
                                        <input type="hidden" name="postId" value="${post.postId}">
                                        <button type="submit" class="action-btn delete">삭제</button>
                                    </form>
                                </div>
                            </c:if>
                        </li>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <li class="empty-state">이 시리즈에는 아직 게시글이 없습니다.</li>
                </c:otherwise>
            </c:choose>
        </ul>
    </main>
</div>
</body>
</html>

