<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  공통 대시보드 상단 바 fragment
  1) <jsp:include page="/WEB-INF/views/common/header.jsp">
         <jsp:param name="activeMenu" value="mypage"/>
     </jsp:include>
  2) <jsp:include page="/WEB-INF/views/common/header.jsp">
         <jsp:param name="activeMenu" value="community"/>
     </jsp:include>
  3) <jsp:include page="/WEB-INF/views/common/header.jsp">
         <jsp:param name="pageLabel" value="게시글 상세"/>
     </jsp:include>
--%>
<div class="top-bar">
    <div class="nav-left">
        <c:choose>
            <c:when test="${param.activeMenu eq 'mypage'}">
                <span class="breadcrumb">
                    <i class="fa-solid fa-house"></i> Home
                    <span style="color: var(--border-color);">/</span>
                    내 작업 공간
                </span>
                <button class="btn btn-outline" style="padding: 6px 12px; font-size: 13px;"
                        onclick="navigateWithTransition('${pageContext.request.contextPath}/community.do')">
                    <i class="fa-solid fa-globe"></i> 커뮤니티로 전환
                </button>
            </c:when>
            <c:when test="${param.activeMenu eq 'community'}">
                <span class="breadcrumb">
                    <i class="fa-solid fa-house"></i> Home
                    <span style="color: var(--border-color);">/</span>
                    커뮤니티
                </span>
                <button class="btn btn-outline" style="padding: 6px 12px; font-size: 13px;"
                        onclick="navigateWithTransition('${pageContext.request.contextPath}/l_check/user/mypage.do')">
                    <i class="fa-solid fa-desktop"></i> 내 작업 공간으로
                </button>
            </c:when>
            <c:otherwise>
                <a class="breadcrumb"
                   href="${pageContext.request.contextPath}/l_check/user/mypage.do"
                   onclick="event.preventDefault(); navigateWithTransition(this.href)">
                    <i class="fa-solid fa-house"></i> Home
                    <c:if test="${not empty param.pageLabel}">
                        <span style="color: var(--border-color);">/</span>
                        <c:out value="${param.pageLabel}"/>
                    </c:if>
                </a>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="nav-right">
    </div>
</div>
<%-- navigateWithTransition() 은 page-transition.js (sideBar.jsp 에서 로드) 에 정의됨 --%>