<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%-- ${series}가 있으면 수정 모드, 없으면 생성 모드 --%>
    <title>스터디로그 - 시리즈 <c:choose><c:when test="${not empty series}">수정</c:when><c:otherwise>만들기</c:otherwise></c:choose></title>
    <jsp:include page="/WEB-INF/views/common/head.jsp"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/ui.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/post/post_write.css">
</head>
<body>
<div class="dashboard-wrapper">
    <jsp:include page="/WEB-INF/views/common/sideBar.jsp">
        <jsp:param name="activeMenu" value="mypage"/>
    </jsp:include>

    <main class="main-content">
        <div class="top-bar">
            <a class="breadcrumb" href="${pageContext.request.contextPath}/l_check/user/mypage.do">
                <i class="fa-solid fa-arrow-left"></i> 돌아가기
            </a>
        </div>

        <%-- 생성: POST /l_check/series/write.do
             수정: POST /l_check/series/update.do (seriesId hidden으로 전달) --%>
        <form class="write-container"
              action="${pageContext.request.contextPath}/l_check/series/${not empty series ? 'update' : 'write'}.do"
              method="post">

            <%-- 수정 모드일 때만 seriesId를 hidden으로 전달 --%>
            <c:if test="${not empty series}">
                <input type="hidden" name="seriesId" value="${series.seriesId}">
            </c:if>

            <div class="title-area">
                <input type="text" name="name" class="title-input"
                       placeholder="시리즈 이름을 입력하세요"
                       value="<c:out value='${series.name}'/>"
                       required>
            </div>

            <div class="editor-area">
                <textarea name="description" class="editor-textarea"
                          placeholder="시리즈에 대한 간단한 설명을 입력하세요."><c:out value="${series.description}"/></textarea>
            </div>

            <div class="write-footer">
                <div class="footer-right">
                    <div class="visibility-toggle">
                        <span id="toggleText">비공개</span>
                        <label class="switch">
                            <%-- 수정 모드: 기존 isPublic 값 반영 / 생성 모드: 기본 공개 --%>
                            <%-- 체크 해제 시에도 비공개 값이 서버로 전송되도록 기본값을 둔다. --%>
                            <input type="hidden" id="isPublicValue" name="isPublic"
                                   value="${empty series or series.isPublic eq 'Y' ? 'Y' : 'N'}">
                            <input type="checkbox" id="publicToggle" value="Y"
                                   ${empty series or series.isPublic eq 'Y' ? 'checked' : ''}>
                            <span class="slider"></span>
                        </label>
                        <span id="toggleTextRight" style="color: var(--text-main);">공개</span>
                    </div>

                    <%-- 수정 모드일 때만 삭제 버튼 표시
                         form 중첩 불가(HTML 스펙) → 버튼은 외부 deleteForm을 JS로 submit --%>
                    <c:if test="${not empty series}">
                        <button type="button" class="btn btn-outline"
                                style="color:#ef5350; border-color:#ef5350;"
                                onclick="if(confirm('시리즈를 삭제하면 소속 게시글의 시리즈 연결이 해제됩니다. 삭제하시겠습니까?')) document.getElementById('deleteSeriesForm').submit()">
                            <i class="fa-regular fa-trash-can"></i> 시리즈 삭제
                        </button>
                    </c:if>

                    <button type="submit" class="btn btn-primary" style="padding: 10px 24px; font-size: 15px;">
                        <c:choose>
                            <c:when test="${not empty series}">수정 완료</c:when>
                            <c:otherwise>시리즈 만들기</c:otherwise>
                        </c:choose>
                    </button>
                </div>
            </div>
        </form>
    </main>
</div>

<%-- 삭제 전용 form: write-container form 중첩 불가로 외부에 분리
     버튼에서 JS로 submit() 호출 --%>
<c:if test="${not empty series}">
    <form id="deleteSeriesForm"
          action="${pageContext.request.contextPath}/l_check/series/delete.do"
          method="post">
        <input type="hidden" name="seriesId" value="${series.seriesId}">
    </form>
</c:if>

<script>
    const toggle = document.getElementById('publicToggle');
    const isPublicValue = document.getElementById('isPublicValue');
    const textLeft = document.getElementById('toggleText');
    const textRight = document.getElementById('toggleTextRight');

    function syncVisibilityText() {
        isPublicValue.value = toggle.checked ? 'Y' : 'N';
        if (toggle.checked) {
            textLeft.style.color = 'var(--text-sub)';
            textRight.style.color = 'var(--text-main)';
        } else {
            textLeft.style.color = 'var(--text-main)';
            textRight.style.color = 'var(--text-sub)';
        }
    }

    toggle.addEventListener('change', syncVisibilityText);
    syncVisibilityText();
</script>
</body>
</html>
