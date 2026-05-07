<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%-- ${post}가 있으면 수정 모드, 없으면 생성 모드 --%>
    <title>스터디로그 - 게시글 <c:choose><c:when test="${not empty post}">수정</c:when><c:otherwise>작성</c:otherwise></c:choose></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
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

        <%-- 에러 메시지 표시 (유효성 검사 실패 시) --%>
        <c:if test="${not empty errorMsg}">
            <div class="error-banner" style="color:#ef5350; padding: 10px 0; font-size: 14px;">
                <i class="fa-solid fa-circle-exclamation"></i> <c:out value="${errorMsg}"/>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="error-banner" style="color:#ef5350; padding: 10px 0; font-size: 14px;">
                <i class="fa-solid fa-circle-exclamation"></i> <c:out value="${param.error}"/>
            </div>
        </c:if>

        <%-- 생성: POST /l_check/post/write.do
             수정: POST /l_check/post/update.do (postId hidden으로 전달) --%>
        <form class="write-container"
              action="${pageContext.request.contextPath}/l_check/post/${not empty post ? 'update' : 'write'}.do"
              method="post">

            <%-- 수정 모드일 때만 postId를 hidden으로 전달 --%>
            <c:if test="${not empty post}">
                <input type="hidden" name="postId" value="${post.postId}">
            </c:if>

            <div class="title-area">
                <%-- 수정 모드: 기존 제목 / 생성 모드: param.title (에러 복귀 시 입력값 유지) --%>
                <input type="text" name="title" class="title-input"
                       placeholder="게시글 제목을 입력하세요"
                       value="<c:out value='${not empty post ? post.title : param.title}'/>"
                       required>
            </div>

            <div class="meta-area">
                <div>
                    <%-- 수정 모드: tagStr (컨트롤러에서 List<TagDto> → 쉼표 문자열로 변환해서 전달)
                         생성 모드: param.tags (에러 복귀 시 입력값 유지) --%>
                    <input type="text" name="tags" class="tag-input"
                           placeholder="태그를 쉼표로 구분해 입력하세요. 예) java, oracle, jsp"
                           value="<c:out value='${not empty post ? tagStr : param.tags}'/>">
                    <div class="help-text" style="margin-top: 8px;">최대 5개 태그 권장</div>
                </div>
                <div>
                    <select name="seriesId" class="series-select">
                        <option value="">시리즈 선택 안 함</option>
                        <c:forEach var="series" items="${seriesList}">
                            <%-- 수정 모드: 기존 seriesId와 일치하면 selected
                                 생성 모드: URL 파라미터 seriesId와 일치하면 selected (시리즈 상세에서 "새 글 추가" 클릭 시) --%>
                            <option value="${series.seriesId}"
                                ${(not empty post and post.seriesId eq series.seriesId)
                                  or (empty post and param.seriesId eq series.seriesId) ? 'selected' : ''}>
                                <c:out value="${series.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="editor-area">
                <%-- 수정 모드: 기존 본문 / 생성 모드: param.content (에러 복귀 시 입력값 유지) --%>
                <textarea name="content" class="editor-textarea"
                          placeholder="마크다운 형식으로 학습 내용을 기록해 보세요..."
                          required><c:out value="${not empty post ? post.content : param.content}" escapeXml="false"/></textarea>
            </div>

            <div class="write-footer">
                <button type="button" class="attachment-btn">
                    <i class="fa-regular fa-image"></i> 이미지 첨부
                </button>

                <div class="footer-right">
                    <div class="visibility-toggle">
                        <span id="toggleText">비공개</span>
                        <label class="switch">
                            <%-- 수정 모드: 기존 isPublic 값 반영
                                 생성 모드: 기본 공개(checked), 에러 복귀 시 param.isPublic 유지 --%>
                            <input type="checkbox" id="publicToggle" name="isPublic" value="Y"
                                   ${(not empty post and post.isPublic eq 'Y')
                                     or (empty post and (empty param.isPublic or param.isPublic eq 'Y')) ? 'checked' : ''}>
                            <span class="slider"></span>
                        </label>
                        <span id="toggleTextRight" style="color: var(--text-main);">공개</span>
                    </div>
                    <button type="submit" class="btn btn-primary" style="padding: 10px 24px; font-size: 15px;">
                        <c:choose>
                            <c:when test="${not empty post}">수정 완료</c:when>
                            <c:otherwise>작성 완료</c:otherwise>
                        </c:choose>
                    </button>
                </div>
            </div>
        </form>
    </main>
</div>

<script>
    const toggle = document.getElementById('publicToggle');
    const textLeft = document.getElementById('toggleText');
    const textRight = document.getElementById('toggleTextRight');

    function syncVisibilityText() {
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
