<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>스터디로그 관리자 - 신고 관리</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/common/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/form.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/table.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/admin/admin_report_list.css">
</head>
<body>
<div class="dashboard-wrapper">
    <aside class="sidebar">
        <a class="brand-logo" href="${pageContext.request.contextPath}/admin/main.do"><i class="fa-solid fa-user-shield"></i> 스터디로그 Admin</a>
        <ul class="nav-menu" style="margin-top: 30px;">
            <li onclick="location.href='${pageContext.request.contextPath}/admin/main.do'"><i class="fa-solid fa-chart-pie"></i> 대시보드 메인</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/user/list.do'"><i class="fa-solid fa-users"></i> 회원 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/post/list.do'"><i class="fa-solid fa-file-lines"></i> 게시글 관리</li>
            <li class="active" onclick="location.href='${pageContext.request.contextPath}/admin/report/list.do'"><i class="fa-solid fa-triangle-exclamation"></i> 신고 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/tag/list.do'"><i class="fa-solid fa-tags"></i> 태그 관리</li>
        </ul>
    </aside>

    <main class="main-content">
        <div class="admin-header">
            <h1 style="background: var(--bg-card); padding: 15px 30px; border-radius: var(--radius-sm); border: 1px solid var(--border-color);">신고 목록</h1>
            <form class="controls-right" action="${pageContext.request.contextPath}/admin/report/list.do" method="get">
                <input type="text" name="keyword" class="admin-input" placeholder="검색어 입력" value="<c:out value='${param.keyword}'/>">
                <select name="status" class="admin-select">
                    <option value="">전체 상태</option>
                    <option value="PENDING" ${param.status eq 'PENDING' ? 'selected' : ''}>대기</option>
                    <option value="RESOLVED" ${param.status eq 'RESOLVED' ? 'selected' : ''}>처리 완료</option>
                    <option value="DISMISSED" ${param.status eq 'DISMISSED' ? 'selected' : ''}>기각</option>
                </select>
                <button class="btn-sm">검색</button>
            </form>
        </div>

        <c:set var="reportItems" value="${not empty reportList ? reportList : reports}" />
        <div class="admin-table-wrapper">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>신고ID</th><th>신고유형</th><th>대상 요약</th><th>신고자</th><th>상태</th><th>신고일</th><th>사유</th><th>관리</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty reportItems}">
                        <c:forEach var="report" items="${reportItems}">
                            <tr>
                                <td><c:out value="${report.reportId}"/></td>
                                <td><c:out value="${report.targetType}"/></td>
                                <td><c:out value="${report.targetSummary}"/></td>
                                <td><c:out value="${report.reporterName}"/></td>
                                <td><c:out value="${report.status}"/></td>
                                <td><c:out value="${report.createdAt}"/></td>
                                <td class="reason-box"><c:out value="${report.reason}"/></td>
                                <td>
                                    <div class="action-group">
                                        <form action="${pageContext.request.contextPath}/admin/report/resolve.do" method="post" data-ajax-form="true">
                                            <input type="hidden" name="reportId" value="${report.reportId}">
                                            <button type="submit" class="btn-sm btn-danger">승인</button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/admin/report/dismiss.do" method="post" data-ajax-form="true">
                                            <input type="hidden" name="reportId" value="${report.reportId}">
                                            <button type="submit" class="btn-sm">기각</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="8">신고 내역이 없습니다.</td></tr>
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

                // CLAUDE.md AJAX 패턴: Controller가 req.getParameter("reportId")로 읽을 수 있도록
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

