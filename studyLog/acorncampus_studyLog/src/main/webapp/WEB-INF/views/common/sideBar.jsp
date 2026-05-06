<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  공통 사이드바 fragment
  사용법: <jsp:include page="/WEB-INF/views/common/sideBar.jsp">
              <jsp:param name="activeMenu" value="mypage"/>
              <jsp:param name="activeMenu" value="community"/>
          </jsp:include>
  의존 세션: loginUser (UserDto)
--%>
<script src="${pageContext.request.contextPath}/resources/js/page-transition.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/common/sidebar.css">
<aside class="sidebar" id="appSidebar">
    <button class="sidebar-toggle" id="sidebarToggle" title="사이드바 접기/펼치기">
        <i class="fa-solid fa-chevron-left" id="sidebarToggleIcon"></i>
    </button>
    <a class="brand-logo" href="${pageContext.request.contextPath}/">
        <i class="fa-solid fa-book-open"></i>
        <span class="sidebar-text">스터디로그</span>
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
        <li class="${param.activeMenu eq 'mypage' ? 'active' : ''}"
            data-label="내 시리즈"
            onclick="navigateWithTransition('${pageContext.request.contextPath}/l_check/user/mypage.do')">
            <i class="fa-solid fa-layer-group"></i>
            <span class="sidebar-text">내 시리즈</span>
        </li>
        <li class="${param.activeMenu eq 'community' ? 'active' : ''}"
            data-label="커뮤니티 탐색"
            onclick="navigateWithTransition('${pageContext.request.contextPath}/community.do')">
            <i class="fa-solid fa-globe"></i>
            <span class="sidebar-text">커뮤니티 탐색</span>
        </li>
        <c:if test="${loginUser.role eq 'ADMIN'}">
            <li class="nav-admin"
                data-label="관리자 모드"
                onclick="navigateWithTransition('${pageContext.request.contextPath}/admin/main.do')">
                <i class="fa-solid fa-user-shield"></i>
                <span class="sidebar-text">관리자 모드</span>
            </li>
        </c:if>
    </ul>
    <div class="logout-btn">
        <button class="btn btn-outline" style="width: 100%; font-size: 12px;"
                onclick="document.body.classList.toggle('dark-theme')">
            <i class="fa-solid fa-moon"></i>
            <span class="sidebar-text"> 테마 변경</span>
        </button>
        <div style="margin-top: 15px; font-size: 13px; font-weight: 600; color: var(--text-sub); cursor: pointer; text-align: center;"
             onclick="location.href='${pageContext.request.contextPath}/user/logout.do'">
            <i class="fa-solid fa-arrow-right-from-bracket"></i>
            <span class="logout-text"> 로그아웃</span>
        </div>
    </div>
</aside>
<script>
(function () {
    const sidebar    = document.getElementById('appSidebar');
    const toggleBtn  = document.getElementById('sidebarToggle');
    const toggleIcon = document.getElementById('sidebarToggleIcon');
    const KEY = 'sidebar_collapsed';
    function applyState(collapsed) {
        sidebar.classList.toggle('collapsed', collapsed);
        toggleIcon.className = collapsed
            ? 'fa-solid fa-chevron-right'
            : 'fa-solid fa-chevron-left';
    }
    applyState(localStorage.getItem(KEY) === 'true');
    toggleBtn.addEventListener('click', function () {
        const next = !sidebar.classList.contains('collapsed');
        applyState(next);
        localStorage.setItem(KEY, next);
    });
})();
</script>