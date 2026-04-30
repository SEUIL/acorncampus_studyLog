<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>스터디로그 관리자 - 대시보드</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/admin/admin_main.css">
</head>
<body>
<div class="dashboard-wrapper">
    <aside class="sidebar">
        <a class="brand-logo" href="${pageContext.request.contextPath}/admin/main.do"><i class="fa-solid fa-user-shield"></i> 스터디로그 Admin</a>
        <ul class="nav-menu" style="margin-top: 30px;">
            <li class="active" onclick="location.href='${pageContext.request.contextPath}/admin/main.do'"><i class="fa-solid fa-chart-pie"></i> 대시보드 메인</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/user/list.do'"><i class="fa-solid fa-users"></i> 회원 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/post/list.do'"><i class="fa-solid fa-file-lines"></i> 게시글 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/report/list.do'"><i class="fa-solid fa-triangle-exclamation"></i> 신고 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/tag/list.do'"><i class="fa-solid fa-tags"></i> 태그 관리</li>
        </ul>
        <div style="margin-top: auto; border-top: 1px solid var(--border-color); padding-top: 20px;">
            <button class="btn btn-outline" style="width: 100%; font-size: 13px; margin-bottom:10px;" onclick="document.body.classList.toggle('dark-theme')">
                <i class="fa-solid fa-moon"></i> 테마 변경
            </button>
            <button class="btn btn-outline" style="width: 100%; font-size: 13px;" onclick="location.href='${pageContext.request.contextPath}/l_check/user/mypage.do'">
                <i class="fa-solid fa-desktop"></i> 사용자 모드로
            </button>
        </div>
    </aside>

    <main class="main-content">
        <div class="admin-header">
            <h1>관리자 메뉴</h1>
            <button class="btn-sm"><i class="fa-solid fa-gear"></i> 설정</button>
        </div>
        <div class="admin-main-layout">
            <section class="graph-area">
                <h2 style="margin:0; font-size:18px; color:var(--text-main);">최근 7일 게시글 작성 현황</h2>
                <div class="graph-placeholder">
                    <c:out value="${empty recentPostStats ? '그래프 데이터 영역' : recentPostStats}"/>
                </div>
                <div class="summary-grid">
                    <div class="summary-card">
                        <span>전체 회원 수 / 오늘 가입자</span>
                        <strong><c:out value="${totalUserCount}"/> / <c:out value="${todayUserCount}"/></strong>
                    </div>
                    <div class="summary-card">
                        <span>전체 게시글 수 / 오늘 작성글</span>
                        <strong><c:out value="${totalPostCount}"/> / <c:out value="${todayPostCount}"/></strong>
                    </div>
                    <div class="summary-card">
                        <span>처리 대기 신고</span>
                        <strong><c:out value="${pendingReportCount}"/></strong>
                    </div>
                    <div class="summary-card">
                        <span>관리자 메모</span>
                        <strong style="font-size:16px;">운영 현황 확인</strong>
                    </div>
                </div>
            </section>
            <div class="menu-grid">
                <a class="menu-card" href="${pageContext.request.contextPath}/admin/user/list.do">회원 관리</a>
                <a class="menu-card" href="${pageContext.request.contextPath}/admin/post/list.do">게시글 관리</a>
                <a class="menu-card" href="${pageContext.request.contextPath}/admin/report/list.do">신고 관리</a>
                <a class="menu-card" href="${pageContext.request.contextPath}/admin/tag/list.do">태그 관리</a>
            </div>
        </div>
    </main>
</div>
</body>
</html>

