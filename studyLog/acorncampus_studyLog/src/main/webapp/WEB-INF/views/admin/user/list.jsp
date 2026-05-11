<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>스터디로그 관리자 - 회원 관리</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/common/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/form.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/table.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/admin/admin_user_list.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/admin/admin_user_list.css?v=modal-20260508">
    <style>
        <%-- 회원관리 그래프 스타일: 브라우저가 예전 CSS를 캐시해도 막대 그래프가 보이도록 보강 --%>
        .big-graph { min-height: 200px; height: auto; font-size: 14px; font-weight: 400; color: var(--text-main); padding: 24px; }
        .user-chart { width: 100%; max-width: 760px; display: flex; flex-direction: column; gap: 18px; }
        .chart-row { display: grid; grid-template-columns: 70px minmax(120px, 1fr) 70px; align-items: center; gap: 14px; font-size: 14px; color: var(--text-main); }
        .chart-label { font-weight: 700; }
        .chart-track { height: 18px; background: var(--bg-card); border: 1px solid var(--border-color); border-radius: 999px; overflow: hidden; }
        .chart-bar { height: 100%; min-width: 2px; border-radius: 999px; }
        .chart-bar.active { background: var(--accent-color); }
        .chart-bar.banned { background: #ef5350; }
        .chart-bar.deleted { background: #90A4AE; }
        .chart-row strong { text-align: right; font-weight: 800; }
    </style>
</head>
<body>
<div class="dashboard-wrapper">
    <aside class="sidebar">
        <a class="brand-logo" href="${pageContext.request.contextPath}/admin/main.do"><i class="fa-solid fa-user-shield"></i> 스터디로그 Admin</a>
        <ul class="nav-menu" style="margin-top: 30px;">
            <li onclick="location.href='${pageContext.request.contextPath}/admin/main.do'"><i class="fa-solid fa-chart-pie"></i> 대시보드 메인</li>
            <li class="active" onclick="location.href='${pageContext.request.contextPath}/admin/user/list.do'"><i class="fa-solid fa-users"></i> 회원 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/post/list.do'"><i class="fa-solid fa-file-lines"></i> 게시글 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/comment/list.do'"><i class="fa-solid fa-comments"></i> 댓글 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/report/list.do'"><i class="fa-solid fa-triangle-exclamation"></i> 신고 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/tag/list.do'"><i class="fa-solid fa-tags"></i> 태그 관리</li>
        </ul>
    </aside>

    <main class="main-content">
        <div class="admin-header">
            <h1><i class="fa-solid fa-users"></i> 회원 리스트</h1>
        </div>

        <div class="stats-banner">
            <div class="stat-item">전체 회원수 <span><c:out value="${totalUserCount}"/></span></div>
            <div class="stat-item">정상 <span><c:out value="${activeUserCount}"/></span></div>
            <div class="stat-item" style="color: #ef5350;">정지 <span><c:out value="${bannedUserCount}"/></span></div>
            <div class="stat-item">탈퇴 <span><c:out value="${deletedUserCount}"/></span></div>
        </div>

        <div class="big-graph">
            <%-- 회원 상태 통계를 한눈에 볼 수 있는 간단 막대 그래프 --%>
            <div class="user-chart">
                <div class="chart-row">
                    <span class="chart-label">정상</span>
                    <div class="chart-track"><div class="chart-bar active" style="width:${activeUserPercent}%;"></div></div>
                    <strong><c:out value="${activeUserCount}"/>명</strong>
                </div>
                <div class="chart-row">
                    <span class="chart-label">정지</span>
                    <div class="chart-track"><div class="chart-bar banned" style="width:${bannedUserPercent}%;"></div></div>
                    <strong><c:out value="${bannedUserCount}"/>명</strong>
                </div>
                <div class="chart-row">
                    <span class="chart-label">탈퇴</span>
                    <div class="chart-track"><div class="chart-bar deleted" style="width:${deletedUserPercent}%;"></div></div>
                    <strong><c:out value="${deletedUserCount}"/>명</strong>
                </div>
            </div>
        </div>

        <form class="controls-bar" action="${pageContext.request.contextPath}/admin/user/list.do" method="get">
            <div class="controls-left">
                <input type="text" name="keyword" class="admin-input" placeholder="이름, 이메일, ID 검색" style="width: 250px;" value="<c:out value='${param.keyword}'/>">
                <button class="btn-sm" style="background: var(--text-main); color: var(--bg-card);">검색</button>
            </div>
            <div class="controls-right">
                <select class="admin-select" name="status">
                    <option value="">상태 : 전체</option>
                    <option value="banned" ${param.status eq 'banned' ? 'selected' : ''}>상태 : 정지</option>
                    <option value="active" ${param.status eq 'active' ? 'selected' : ''}>상태 : 정상</option>
                    <option value="deleted" ${param.status eq 'deleted' ? 'selected' : ''}>상태 : 탈퇴</option>
                </select>
            </div>
        </form>

        <c:set var="userItems" value="${not empty userList ? userList : users}" />
        <div class="admin-table-wrapper">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>회원ID</th><th>아이디</th><th>이메일</th><th>권한</th><th>상태</th><th>관리</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty userItems}">
                        <c:forEach var="user" items="${userItems}">
                            <tr>
                                <td><c:out value="${user.userId}"/></td>
                                <td><c:out value="${user.nickname}"/></td>
                                <td><c:out value="${user.email}"/></td>
                                <td><c:out value="${user.role}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${user.isBanned eq 'Y'}">정지</c:when>
                                        <c:otherwise>정상</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="action-group">
                                        <c:choose>
                                            <c:when test="${user.isBanned eq 'Y'}">
                                                <form action="${pageContext.request.contextPath}/admin/user/unban.do" method="post" data-ajax-form="true">
                                                    <input type="hidden" name="userId" value="${user.userId}">
                                                    <button type="submit" class="btn-sm">정지 해제</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <form action="${pageContext.request.contextPath}/admin/user/ban.do" method="post" data-ajax-form="true">
                                                    <input type="hidden" name="userId" value="${user.userId}">
                                                    <button type="submit" class="btn-sm">계정 정지</button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>
                                        <button type="button" class="btn-sm detail-open-btn" data-modal-target="userDetailModal-${user.userId}">상세보기</button>
                                        <form action="${pageContext.request.contextPath}/admin/user/delete.do" method="post" data-ajax-form="true">
                                            <input type="hidden" name="userId" value="${user.userId}">
                                            <button type="submit" class="btn-sm btn-danger">회원 삭제</button>
                                        </form>
                                    </div>
                                    <div class="admin-detail-modal" id="userDetailModal-${user.userId}" aria-hidden="true" hidden>
                                        <div class="admin-detail-backdrop" data-modal-close></div>
                                        <div class="admin-detail-panel" role="dialog" aria-modal="true" aria-labelledby="userDetailTitle-${user.userId}">
                                            <div class="admin-detail-header">
                                                <h2 id="userDetailTitle-${user.userId}">회원 상세보기</h2>
                                                <button type="button" class="admin-detail-close" data-modal-close aria-label="닫기">
                                                    <i class="fa-solid fa-xmark"></i>
                                                </button>
                                            </div>
                                            <dl class="admin-detail-list">
                                                <div>
                                                    <dt>회원ID</dt>
                                                    <dd><c:out value="${user.userId}"/></dd>
                                                </div>
                                                <div>
                                                    <dt>닉네임</dt>
                                                    <dd><c:out value="${user.nickname}"/></dd>
                                                </div>
                                                <div>
                                                    <dt>이메일</dt>
                                                    <dd><c:out value="${user.email}"/></dd>
                                                </div>
                                                <div>
                                                    <dt>권한</dt>
                                                    <dd><c:out value="${user.role}"/></dd>
                                                </div>
                                                <div>
                                                    <dt>상태</dt>
                                                    <dd>
                                                        <c:choose>
                                                            <c:when test="${not empty user.deletedAt}">탈퇴</c:when>
                                                            <c:when test="${user.isBanned eq 'Y'}">정지</c:when>
                                                            <c:otherwise>정상</c:otherwise>
                                                        </c:choose>
                                                    </dd>
                                                </div>
                                                <div>
                                                    <dt>가입일</dt>
                                                    <dd><c:out value="${user.createdAt}"/></dd>
                                                </div>
                                                <div>
                                                    <dt>탈퇴일</dt>
                                                    <dd><c:out value="${empty user.deletedAt ? '-' : user.deletedAt}"/></dd>
                                                </div>
                                                <div class="detail-wide">
                                                    <dt>자기소개</dt>
                                                    <dd><c:out value="${empty user.bio ? '-' : user.bio}"/></dd>
                                                </div>
                                            </dl>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="6">조회된 회원이 없습니다.</td></tr>
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

                // CLAUDE.md AJAX 패턴: Controller가 req.getParameter("userId")로 읽을 수 있도록
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

        document.querySelectorAll('.detail-open-btn').forEach(function(button) {
            button.addEventListener('click', function() {
                const modal = document.getElementById(button.dataset.modalTarget);
                if (modal) {
                    modal.hidden = false;
                    modal.classList.add('is-open');
                    modal.setAttribute('aria-hidden', 'false');
                }
            });
        });

        document.querySelectorAll('[data-modal-close]').forEach(function(button) {
            button.addEventListener('click', function() {
                const modal = button.closest('.admin-detail-modal');
                if (modal) {
                    modal.classList.remove('is-open');
                    modal.setAttribute('aria-hidden', 'true');
                    modal.hidden = true;
                }
            });
        });
    });
</script>
</body>
</html>

