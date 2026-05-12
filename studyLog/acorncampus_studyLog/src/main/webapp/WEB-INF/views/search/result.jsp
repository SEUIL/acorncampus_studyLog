<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>스터디로그 - 검색 결과</title>
    <jsp:include page="/WEB-INF/views/common/head.jsp"/>
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
        <jsp:include page="/WEB-INF/views/common/header.jsp">
            <jsp:param name="activeMenu" value="community"/>
        </jsp:include>

        <%-- 페이지 타이틀 --%>
        <div class="page-header">
            <h1>
                <i class="fa-solid fa-magnifying-glass"></i>
                '<c:out value="${keyword}"/>' 검색 결과
            </h1>
            <span style="font-size: 14px; color: var(--text-sub);">
                총 <strong><c:out value="${page.totalCount}"/></strong>건
            </span>
        </div>

        <%-- 검색창 (다시 검색할 수 있게) --%>
        <form class="controls-bar" action="${pageContext.request.contextPath}/search.do" method="get">
            <div class="controls-left">
                <input type="text" name="q" class="admin-input"
                       placeholder="키워드 또는 #태그명으로 검색"
                       value="<c:out value='${keyword}'/>"
                       style="width: 300px;">
                <button type="submit" class="btn-sm" style="background: var(--text-main); color: var(--bg-card);">
                    검색
                </button>
            </div>
        </form>

        <%-- 검색 결과 테이블 --%>
        <div class="admin-table-wrapper">
            <table class="admin-table">
                <thead>
                <tr>
                    <th style="width: 80px;">글번호</th>
                    <th style="width: 150px;">시리즈</th>
                    <th>제목</th>
                    <th style="width: 120px;">작성자</th>
                    <th style="width: 140px;">작성일</th>
                    <th style="width: 80px;">조회수</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty posts}">
                        <c:forEach var="post" items="${posts}">
                            <tr>
                                <td><c:out value="${post.postId}"/></td>
                                <td><c:out value="${empty post.seriesName ? '자유게시판' : post.seriesName}"/></td>
                                <td style="text-align: left;">
                                    <a class="post-link"
                                       href="${pageContext.request.contextPath}/post/detail.do?id=${post.postId}">
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
                            <td colspan="6" class="empty-row">
                                '<c:out value="${keyword}"/>'에 대한 검색 결과가 없습니다.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>

        <%-- 페이지네이션 --%>
        <c:if test="${page.totalPages > 1}">
            <div class="pagination" style="display: flex; justify-content: center; gap: 6px; margin-top: 24px;">

                <%-- 이전 페이지: keyword를 q 파라미터로 다시 붙여야 검색어가 유지됨 --%>
                <%-- href 안에 keyword를 그대로 쓰면 " 나 < 같은 문자로 속성 탈출이 가능하므로
                     fn:escapeXml()로 HTML 특수문자를 이스케이프 처리 (XSS 방지) --%>
                <c:if test="${page.hasPrev}">
                    <a class="page-btn"
                       href="${pageContext.request.contextPath}/search.do?q=${fn:escapeXml(keyword)}&amp;page=${page.prevPage}">
                        <i class="fa-solid fa-chevron-left"></i>
                    </a>
                </c:if>

                <%-- 페이지 번호 버튼: 최대 5개 블록씩 표시 --%>
                <c:set var="startPage" value="${page.pageNo - 2 > 0 ? page.pageNo - 2 : 1}"/>
                <c:set var="endPage"   value="${startPage + 4 < page.totalPages ? startPage + 4 : page.totalPages}"/>

                <c:forEach var="i" begin="${startPage}" end="${endPage}">
                    <c:choose>
                        <c:when test="${i eq page.pageNo}">
                            <span class="page-btn active"><c:out value="${i}"/></span>
                        </c:when>
                        <c:otherwise>
                            <a class="page-btn"
                               href="${pageContext.request.contextPath}/search.do?q=${fn:escapeXml(keyword)}&amp;page=${i}">
                                <c:out value="${i}"/>
                            </a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>

                <%-- 다음 페이지 --%>
                <c:if test="${page.hasNext}">
                    <a class="page-btn"
                       href="${pageContext.request.contextPath}/search.do?q=${fn:escapeXml(keyword)}&amp;page=${page.nextPage}">
                        <i class="fa-solid fa-chevron-right"></i>
                    </a>
                </c:if>

            </div>
        </c:if>

    </main>
</div>
</body>
</html>
