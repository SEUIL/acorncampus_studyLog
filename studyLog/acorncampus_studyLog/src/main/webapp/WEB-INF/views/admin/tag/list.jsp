<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>스터디로그 관리자 - 태그 관리</title>
    <jsp:include page="/WEB-INF/views/common/head.jsp"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/common/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/form.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/table.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/tabs.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/admin/admin_tag_list.css">
</head>
<body>
<div class="dashboard-wrapper">
    <aside class="sidebar">
        <a class="brand-logo" href="${pageContext.request.contextPath}/admin/main.do"><i class="fa-solid fa-user-shield"></i> 스터디로그 Admin</a>
        <ul class="nav-menu" style="margin-top: 30px;">
            <li onclick="location.href='${pageContext.request.contextPath}/admin/main.do'"><i class="fa-solid fa-chart-pie"></i> 대시보드 메인</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/user/list.do'"><i class="fa-solid fa-users"></i> 회원 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/post/list.do'"><i class="fa-solid fa-file-lines"></i> 게시글 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/comment/list.do'"><i class="fa-solid fa-comments"></i> 댓글 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/report/list.do'"><i class="fa-solid fa-triangle-exclamation"></i> 신고 관리</li>
            <li class="active" onclick="location.href='${pageContext.request.contextPath}/admin/tag/list.do'"><i class="fa-solid fa-tags"></i> 태그 관리</li>
        </ul>
    </aside>

    <main class="main-content">
        <div class="admin-header">
            <h1><i class="fa-solid fa-tags"></i> 태그 관리</h1>
            <p class="text-sub" style="margin-top: 10px;">등록된 태그를 검색하고 정렬 기준에 따라 관리합니다.</p>
        </div>

        <c:set var="currentSort" value="${empty param.sort ? 'count' : param.sort}" />
        <c:url var="countSortUrl" value="/admin/tag/list.do">
            <c:param name="sort" value="count" />
            <c:param name="keyword" value="${param.keyword}" />
        </c:url>
        <c:url var="latestSortUrl" value="/admin/tag/list.do">
            <c:param name="sort" value="latest" />
            <c:param name="keyword" value="${param.keyword}" />
        </c:url>
        <c:url var="nameSortUrl" value="/admin/tag/list.do">
            <c:param name="sort" value="name" />
            <c:param name="keyword" value="${param.keyword}" />
        </c:url>

        <div class="tag-tabs">
            <a class="tag-tab ${currentSort eq 'count' ? 'active' : ''}" href="${countSortUrl}">사용빈도순</a>
            <a class="tag-tab ${currentSort eq 'latest' ? 'active' : ''}" href="${latestSortUrl}">최신 등록</a>
            <a class="tag-tab ${currentSort eq 'name' ? 'active' : ''}" href="${nameSortUrl}">이름순</a>
        </div>

        <form class="controls-bar" action="${pageContext.request.contextPath}/admin/tag/list.do" method="get">
            <input type="hidden" name="sort" value="${currentSort}">
            <div class="controls-left">
                <input type="text" name="keyword" class="admin-input" placeholder="태그 이름 검색" style="width: 300px;" value="<c:out value='${param.keyword}'/>">
                <button class="btn-sm" style="background: var(--text-main); color: var(--bg-card);" type="submit">검색</button>
            </div>
            <div class="controls-right"></div>
        </form>

        <c:set var="tagItems" value="${not empty tagList ? tagList : tags}" />
        <div class="admin-table-wrapper">
            <table class="admin-table">
                <thead>
                <tr>
                    <th style="width: 35%;">태그 이름</th>
                    <th style="width: 35%;">사용 횟수</th>
                    <th style="width: 30%;">관리</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty tagItems}">
                        <c:forEach var="tag" items="${tagItems}">
                            <tr>
                                <td>#<c:out value="${tag.name}"/></td>
                                <td><c:out value="${tag.postCount}"/>회</td>
                                <td>
                                    <form action="${pageContext.request.contextPath}/admin/tag/delete.do" method="post" style="display:inline;" data-ajax-form="true">
                                        <input type="hidden" name="tagId" value="${tag.tagId}">
                                        <button type="submit" class="btn-sm btn-danger">삭제</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="3">등록된 태그가 없습니다.</td></tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </main>
</div>
<%-- 관리자 화면 표시용 전환 스크립트 --%>
<script src="${pageContext.request.contextPath}/resources/js/page-transition.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        document.querySelectorAll('form[data-ajax-form="true"]').forEach(function(form) {
            form.addEventListener('submit', function(e) {
                e.preventDefault();

                // CLAUDE.md AJAX 패턴: Controller가 req.getParameter("tagId")로 읽을 수 있도록
                // FormData를 URLSearchParams로 변환해 application/x-www-form-urlencoded 형식으로 전송
                const params = new URLSearchParams(new FormData(form));

                fetch(form.action, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: params
                })
                .then(response => response.json())
                .then(data => {
                    alert(data.message);
                    if (data.status === 'ok') {
                        window.location.reload();
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('서버 통신 중 오류가 발생했습니다.');
                });
            });
        });
    });
</script>
</body>
</html>

