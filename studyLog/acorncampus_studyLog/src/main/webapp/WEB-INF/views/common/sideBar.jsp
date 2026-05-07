<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  공통 사이드바 fragment
  사용법: <jsp:include page="/WEB-INF/views/common/sideBar.jsp">
              <jsp:param name="activeMenu" value="mypage"/>
              <jsp:param name="activeMenu" value="community"/>
          </jsp:include>
  의존 세션: loginUser (UserDto)
--%>
<script src="${pageContext.request.contextPath}/resources/js/page-transition.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/jandi.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/common/sidebar.css">

<!-- 🟢 1. 테마 즉시 복구 (깜빡임 방지를 위해 최상단 배치) -->
<script>
    (function() {
        const savedTheme = localStorage.getItem('studyLogTheme');
        if (savedTheme === 'dark') {
            document.body.classList.add('dark-theme');
        }
    })();
</script>

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
    <div class="profile-section" title="프로필 수정"
         onclick="navigateWithTransition('${pageContext.request.contextPath}/l_check/user/update.do')"
         style="cursor: pointer;">
        <div class="profile-avatar">
            <c:choose>
                <c:when test="${not empty loginUser.avatarUrl}">
                    <%-- 외부 URL(http로 시작)이면 contextPath 없이, 로컬 파일이면 contextPath 붙임 --%>
                    <c:choose>
                        <c:when test="${fn:startsWith(loginUser.avatarUrl, 'http')}">
                            <img src="<c:out value='${loginUser.avatarUrl}'/>"
                                 alt="프로필 사진"
                                 style="width:100%; height:100%; object-fit:cover; border-radius:50%;">
                        </c:when>
                        <c:otherwise>
                            <img src="${pageContext.request.contextPath}<c:out value='${loginUser.avatarUrl}'/>"
                                 alt="프로필 사진"
                                 style="width:100%; height:100%; object-fit:cover; border-radius:50%;">
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <i class="fa-solid fa-user"></i>
                </c:otherwise>
            </c:choose>
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
        <!-- 🟢 2. 테마 변경 버튼 로직 수정 (toggleTheme 함수 호출) -->
        <button class="btn btn-outline" style="width: 100%; font-size: 12px;" onclick="toggleTheme()">
            <i class="fa-solid fa-moon" id="themeToggleIcon"></i>
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
// 🟢 3. 테마 토글 함수 정의
function toggleTheme() {
    const isDark = document.body.classList.toggle('dark-theme');
    const themeIcon = document.getElementById('themeToggleIcon');

    // 로컬 스토리지에 현재 상태 저장
    localStorage.setItem('studyLogTheme', isDark ? 'dark' : 'light');

    // 아이콘 변경
    if (themeIcon) {
        themeIcon.className = isDark ? 'fa-solid fa-sun' : 'fa-solid fa-moon';
    }
}

// 초기 로딩 시 아이콘 모양 맞추기 (DOM이 다 그려진 후 실행)
document.addEventListener('DOMContentLoaded', function() {
    const themeIcon = document.getElementById('themeToggleIcon');
    if (document.body.classList.contains('dark-theme') && themeIcon) {
        themeIcon.className = 'fa-solid fa-sun';
    }
});

// 기존 사이드바 접기/펼치기 로직
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