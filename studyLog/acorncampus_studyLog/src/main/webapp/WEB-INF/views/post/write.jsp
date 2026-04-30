<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>스터디로그 - 게시글 작성</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/post/post_write.css">
</head>
<body>
<div class="dashboard-wrapper">
    <aside class="sidebar">
        <a class="brand-logo" href="${pageContext.request.contextPath}/">
            <i class="fa-solid fa-book-open"></i> 스터디로그
        </a>
        <ul class="nav-menu" style="margin-top: 30px;">
            <li class="active" onclick="location.href='${pageContext.request.contextPath}/l_check/user/mypage.do'"><i class="fa-solid fa-layer-group"></i> 내 시리즈</li>
            <li onclick="location.href='${pageContext.request.contextPath}/post/list.do'"><i class="fa-solid fa-globe"></i> 커뮤니티 탐색</li>
        </ul>
    </aside>

    <main class="main-content">
        <div class="top-bar">
            <a class="breadcrumb" href="${pageContext.request.contextPath}/l_check/user/mypage.do">
                <i class="fa-solid fa-arrow-left"></i> 돌아가기
            </a>
        </div>

        <form class="write-container" action="${pageContext.request.contextPath}/post/write.do" method="post">
            <div class="title-area">
                <input type="text" name="title" class="title-input" placeholder="게시글 제목을 입력하세요" value="<c:out value='${param.title}'/>" required>
            </div>

            <div class="meta-area">
                <div>
                    <input type="text" name="tags" class="tag-input" placeholder="태그를 쉼표로 구분해 입력하세요. 예) java, oracle, jsp"
                           value="<c:out value='${param.tags}'/>">
                    <div class="help-text" style="margin-top: 8px;">최대 5개 태그 권장</div>
                </div>
                <div>
                    <select name="seriesId" class="series-select">
                        <option value="">시리즈 선택 안 함</option>
                        <c:forEach var="series" items="${seriesList}">
                            <option value="${series.seriesId}" ${param.seriesId eq series.seriesId ? 'selected' : ''}>
                                <c:out value="${series.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="editor-area">
                <textarea name="content" class="editor-textarea" placeholder="마크다운 형식으로 학습 내용을 기록해 보세요..." required><c:out value="${param.content}"/></textarea>
            </div>

            <div class="write-footer">
                <button type="button" class="attachment-btn">
                    <i class="fa-regular fa-image"></i> 이미지 첨부
                </button>

                <div class="footer-right">
                    <div class="visibility-toggle">
                        <span id="toggleText">비공개</span>
                        <label class="switch">
                            <input type="checkbox" id="publicToggle" name="isPublic" value="Y" ${empty param.isPublic or param.isPublic eq 'Y' ? 'checked' : ''}>
                            <span class="slider"></span>
                        </label>
                        <span id="toggleTextRight" style="color: var(--text-main);">공개</span>
                    </div>
                    <button type="submit" class="btn btn-primary" style="padding: 10px 24px; font-size: 15px;">작성완료</button>
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

