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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/tabs.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/admin/admin_report_list.css?v=modal-20260508">
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
            <li class="active" onclick="location.href='${pageContext.request.contextPath}/admin/report/list.do'"><i class="fa-solid fa-triangle-exclamation"></i> 신고 관리</li>
            <li onclick="location.href='${pageContext.request.contextPath}/admin/tag/list.do'"><i class="fa-solid fa-tags"></i> 태그 관리</li>
        </ul>
    </aside>

    <main class="main-content">
        <div class="admin-header">
            <h1><i class="fa-solid fa-triangle-exclamation"></i> 신고 관리</h1>
            <p class="text-sub" style="margin-top: 10px;">신고된 게시글과 댓글을 검색하고 처리 상태를 관리합니다.</p>
        </div>

        <div class="board-tabs">
            <div class="board-tab active">신고 관리</div>
        </div>

        <form class="controls-bar" action="${pageContext.request.contextPath}/admin/report/list.do" method="get">
            <div class="controls-left">
                <input type="text" name="keyword" class="admin-input" placeholder="신고자, 대상 작성자, 사유 검색" style="width: 300px;" value="<c:out value='${param.keyword}'/>">
                <button class="btn-sm" style="background: var(--text-main); color: var(--bg-card);">검색</button>
            </div>
            <div class="controls-right">
                <select name="status" class="admin-select">
                    <option value="">전체 상태</option>
                    <option value="PENDING" ${param.status eq 'PENDING' ? 'selected' : ''}>대기</option>
                    <option value="RESOLVED" ${param.status eq 'RESOLVED' ? 'selected' : ''}>처리 완료</option>
                    <option value="DISMISSED" ${param.status eq 'DISMISSED' ? 'selected' : ''}>기각</option>
                </select>
            </div>
        </form>

        <c:set var="reportItems" value="${not empty reportList ? reportList : reports}" />
        <div class="admin-table-wrapper">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>신고ID</th><th>신고유형</th><th>대상 요약</th><th>신고자</th><th>신고당한 사람</th><th>상태</th><th>신고일</th><th>사유</th><th>관리</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${not empty reportItems}">
                        <c:forEach var="report" items="${reportItems}">
                            <tr>
                                <td><c:out value="${report.reportId}"/></td>
                                <td><c:out value="${report.targetType}"/></td>
                                <td><c:out value="${empty report.targetSummary ? '-' : report.targetSummary}"/></td>
                                <td><c:out value="${report.reporterName}"/></td>
                                <td><c:out value="${empty report.targetAuthorName ? '-' : report.targetAuthorName}"/></td>
                                <td><c:out value="${report.status}"/></td>
                                <td><c:out value="${report.createdAt}"/></td>
                                <td class="reason-box"><c:out value="${report.reason}"/></td>
                                <td>
                                    <div class="action-group">
                                        <button type="button" class="btn-sm detail-open-btn" data-modal-target="reportDetailModal-${report.reportId}">상세보기</button>
                                        <form action="${pageContext.request.contextPath}/admin/report/resolve.do" method="post" data-ajax-form="true">
                                            <input type="hidden" name="reportId" value="${report.reportId}">
                                            <button type="submit" class="btn-sm btn-danger">승인</button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/admin/report/dismiss.do" method="post" data-ajax-form="true">
                                            <input type="hidden" name="reportId" value="${report.reportId}">
                                            <button type="submit" class="btn-sm">기각</button>
                                        </form>
                                    </div>
                                    <div class="admin-detail-modal" id="reportDetailModal-${report.reportId}" aria-hidden="true" hidden>
                                        <div class="admin-detail-backdrop" data-modal-close></div>
                                        <div class="admin-detail-panel" role="dialog" aria-modal="true" aria-labelledby="reportDetailTitle-${report.reportId}">
                                            <div class="admin-detail-header">
                                                <h2 id="reportDetailTitle-${report.reportId}">신고 상세보기</h2>
                                                <button type="button" class="admin-detail-close" data-modal-close aria-label="닫기">
                                                    <i class="fa-solid fa-xmark"></i>
                                                </button>
                                            </div>
                                            <dl class="admin-detail-list">
                                                <div>
                                                    <dt>신고ID</dt>
                                                    <dd><c:out value="${report.reportId}"/></dd>
                                                </div>
                                                <div>
                                                    <dt>신고유형</dt>
                                                    <dd><c:out value="${report.targetType}"/></dd>
                                                </div>
                                                <div>
                                                    <dt>대상ID</dt>
                                                    <dd><c:out value="${report.targetId}"/></dd>
                                                </div>
                                                <div>
                                                    <dt>신고자</dt>
                                                    <dd><c:out value="${report.reporterName}"/></dd>
                                                </div>
                                                <div>
                                                    <dt>신고당한 사람</dt>
                                                    <dd><c:out value="${empty report.targetAuthorName ? '-' : report.targetAuthorName}"/></dd>
                                                </div>
                                                <div>
                                                    <dt>대상 작성자 이메일</dt>
                                                    <dd><c:out value="${empty report.targetAuthorEmail ? '-' : report.targetAuthorEmail}"/></dd>
                                                </div>
                                                <div>
                                                    <dt>상태</dt>
                                                    <dd><c:out value="${report.status}"/></dd>
                                                </div>
                                                <div>
                                                    <dt>신고일</dt>
                                                    <dd><c:out value="${report.createdAt}"/></dd>
                                                </div>
                                                <div class="detail-wide">
                                                    <dt>대상 요약</dt>
                                                    <dd><c:out value="${empty report.targetSummary ? '-' : report.targetSummary}"/></dd>
                                                </div>
                                                <div class="detail-wide">
                                                    <dt>신고 사유</dt>
                                                    <dd><c:out value="${empty report.reason ? '-' : report.reason}"/></dd>
                                                </div>
                                            </dl>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="9">신고 내역이 없습니다.</td></tr>
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

