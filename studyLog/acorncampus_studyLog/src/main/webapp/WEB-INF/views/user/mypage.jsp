<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>스터디로그 - 내 작업 공간</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/ui.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/series.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/workspace/workspace_main.css">
</head>
<body>
<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sideBar.jsp">
        <jsp:param name="activeMenu" value="mypage"/>
    </jsp:include>

    <main class="main-content">
        <jsp:include page="/WEB-INF/views/common/header.jsp">
            <jsp:param name="activeMenu" value="mypage"/>
        </jsp:include>

        <section class="workspace-area">
            <div class="workspace-header">
                <h1>내 작업물 (시리즈)</h1>
                <button class="btn btn-primary"
                        onclick="location.href='${pageContext.request.contextPath}/l_check/post/write.do'">
                    <i class="fa-solid fa-pen"></i> 새 글 작성
                </button>
            </div>

            <div class="series-grid">
                <c:forEach var="series" items="${seriesList}">
                    <a class="series-card" href="${pageContext.request.contextPath}/series/detail.do?id=${series.seriesId}">
                        <div class="series-info">
                            <h3><c:out value="${series.name}"/></h3>
                            <p><c:out value="${series.description}"/></p>
                        </div>
                        <div class="series-meta">
                            <span><i class="fa-regular fa-file-lines"></i> <c:out value="${series.postCount}"/> Posts</span>
                            <span class="tag"><c:out value="${series.name}"/></span>
                        </div>
                    </a>
                </c:forEach>

                <a class="series-card series-card-new"
                   href="${pageContext.request.contextPath}/l_check/series/write.do">
                    <i class="fa-solid fa-plus"></i>
                    <h3>새 시리즈 만들기</h3>
                </a>
            </div>
        </section>

        <%-- 시리즈에 속하지 않은 글 목록 --%>
        <%-- postList 전체 중 seriesId가 null인 항목만 필터링해서 표시 --%>
        <section class="loose-posts-section">
            <%-- 시리즈 없는 글 수를 먼저 카운트 --%>
            <c:set var="looseCount" value="0"/>
            <c:forEach var="post" items="${postList}">
                <c:if test="${empty post.seriesId}">
                    <c:set var="looseCount" value="${looseCount + 1}"/>
                </c:if>
            </c:forEach>

            <div class="loose-posts-header">
                <h2><i class="fa-regular fa-file-lines"></i> 시리즈 없는 글</h2>
                <span class="count-badge">${looseCount}</span>
            </div>

            <c:choose>
                <c:when test="${looseCount > 0}">
                    <div class="loose-post-list">
                        <c:forEach var="post" items="${postList}">
                            <c:if test="${empty post.seriesId}">
                                <div class="loose-post-item">
                                    <a class="loose-post-title"
                                       href="${pageContext.request.contextPath}/post/detail.do?id=${post.postId}"
                                       title="<c:out value='${post.title}'/>">
                                        <c:out value="${post.title}"/>
                                    </a>
                                    <div class="loose-post-meta">
                                        <c:if test="${post.isPublic eq 'N'}">
                                            <span class="badge-private"><i class="fa-solid fa-lock"></i> 비공개</span>
                                        </c:if>
                                        <span>${post.createdAt}</span>
                                    </div>
                                    <a class="loose-post-edit"
                                       href="${pageContext.request.contextPath}/l_check/post/write.do?id=${post.postId}">
                                        수정
                                    </a>
                                </div>
                            </c:if>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <p style="color: var(--text-sub); font-size: 14px; padding: 16px 0;">
                        시리즈에 속하지 않은 글이 없습니다.
                    </p>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</div>
</body>
</html>

