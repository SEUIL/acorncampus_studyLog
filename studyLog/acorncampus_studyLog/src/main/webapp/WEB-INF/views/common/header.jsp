<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- 공통 헤더. 각 JSP에서 <%@ include file="/WEB-INF/views/common/header.jsp" %> 로 포함 --%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%-- pageTitle 미설정 시 기본 제목 사용 --%>
    <title><c:out value="${pageTitle != null ? pageTitle : '나만의 학습 기록'}"/></title>

    <%-- contextPath: 앱 루트 경로 (예: /blog). 절대 경로로 써야 URL depth 무관하게 동작 --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">

    <!-- Toast UI Editor (마크다운 에디터) CDN -->
    <link rel="stylesheet" href="https://uicdn.toast.com/editor/latest/toastui-editor.min.css">
    <script src="https://uicdn.toast.com/editor/latest/toastui-editor-all.min.js"></script>
</head>
<body>

<header class="site-header">
    <nav class="navbar">

        <a class="navbar-brand" href="${pageContext.request.contextPath}/">StudyLog</a>

        <!-- 검색: keyword 파라미터를 search/result.do 로 전송 -->
            <form class="search-form" action="${pageContext.request.contextPath}/search.do" method="get">
            <input type="text" name="keyword" class="search-input"
                   placeholder="검색어 입력..."
                   value="<c:out value='${param.keyword}'/>">
            <button type="submit" class="search-btn">검색</button>
        </form>

        <!-- 로그인 상태에 따라 버튼 분기 -->
        <div class="nav-auth">
            <c:choose>
                <c:when test="${not empty sessionScope.loginUser}">
                    <%-- 로그인 상태: 닉네임 + 로그아웃 --%>
                    <a href="${pageContext.request.contextPath}/l_check/user/mypage.do" class="btn-nav">
                        <c:out value="${sessionScope.loginUser.username}"/> 님
                    </a>
                    <a href="${pageContext.request.contextPath}/user/logout.do" class="btn-nav">로그아웃</a>
                </c:when>
                <c:otherwise>
                    <%-- 비로그인 상태: 로그인 + 회원가입 --%>
                    <a href="${pageContext.request.contextPath}/user/login.do" class="btn-nav">로그인</a>
            <a href="${pageContext.request.contextPath}/user/reg.do" class="btn-nav btn-primary">회원가입</a>
                </c:otherwise>
            </c:choose>
        </div>

    </nav>
</header>

<%-- 본문 시작. footer.jsp의 </main> 과 쌍 --%>
<main class="site-content">
