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
<script src="${pageContext.request.contextPath}/resources/js/background-effect.js"></script>
<script defer src="${pageContext.request.contextPath}/resources/js/interactions.js?v=3"></script>
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
    <a class="brand-logo" href="${pageContext.request.contextPath}/l_check/user/mypage.do">
        <i class="fa-solid fa-book-open"></i>
        <span class="sidebar-text">스터디로그</span>
    </a>
    <div class="grass-section">
        <span class="grass-title">Contributions (12w)</span>
        <div class="grass-grid">
            <%--
                JandiFilter가 request에 주입한 jandiLevels (List<Integer>, 168개)를 순회.
                인덱스 0 = 167일 전(가장 오래된 날), 인덱스 167 = 오늘.
                grid-auto-flow: column 방향으로 렌더링되어 7행 × 24열(주) 구조가 됨.
                레벨 0: 활동 없음(기본 회색) / 1~4: 활동량 증가에 따라 진한 색상.
                비로그인이거나 jandiLevels가 없으면 이 블록은 출력되지 않음.
            --%>
            <c:if test="${not empty jandiLevels}">
                <c:forEach items="${jandiLevels}" var="lvl">
                    <div class="grass-node<c:if test="${lvl > 0}"> level-${lvl}</c:if>"></div>
                </c:forEach>
            </c:if>
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
            <%-- bio가 있으면 본인 설정 인사말, 없으면 기본 문구 --%>
            <p><c:choose>
                <c:when test="${not empty loginUser.bio}"><c:out value="${loginUser.bio}"/></c:when>
                <c:otherwise>Learning &amp; Recording...</c:otherwise>
            </c:choose></p>
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
let _themeTransitioning = false;

function toggleTheme() {
    if (_themeTransitioning) return;   /* 전환 중 중복 클릭 차단 */

    _themeTransitioning = true;

    const btn       = document.querySelector('.logout-btn .btn-outline');
    const themeIcon = document.getElementById('themeToggleIcon');

    /* 버튼 시각적 비활성화 */
    if (btn) {
        btn.style.opacity      = '0.4';
        btn.style.pointerEvents = 'none';
        btn.style.cursor        = 'not-allowed';
    }

    const isDark = document.body.classList.toggle('dark-theme');
    localStorage.setItem('studyLogTheme', isDark ? 'dark' : 'light');

    if (themeIcon) {
        themeIcon.className = isDark ? 'fa-solid fa-sun' : 'fa-solid fa-moon';
    }

    /* 전환 애니메이션 총 시간(~2.3s) 후 버튼 복구 */
    setTimeout(function () {
        if (btn) {
            btn.style.opacity       = '';
            btn.style.pointerEvents = '';
            btn.style.cursor        = '';
        }
        _themeTransitioning = false;
    }, 2400);
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