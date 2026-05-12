<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>스터디로그 - 프로필 수정</title>
    <jsp:include page="/WEB-INF/views/common/head.jsp"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/ui.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/user/profile_update.css">
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

        <div class="profile-update-wrapper">
            <h2 class="section-title">프로필 수정</h2>

            <%-- 에러 메시지 --%>
            <c:if test="${not empty errorMsg}">
                <div class="error-banner"><c:out value="${errorMsg}"/></div>
            </c:if>

            <%-- 파일 업로드를 위해 enctype 설정 (백엔드 @MultipartConfig 연동 필요) --%>
            <form action="${pageContext.request.contextPath}/l_check/user/update.do"
                  method="post" enctype="multipart/form-data">

                <%-- 프로필 사진 --%>
                <div class="profile-avatar-section">
                    <div class="avatar-preview" id="avatarPreview">
                        <c:choose>
                            <c:when test="${not empty userDetail.avatarUrl}">
                                <%-- 외부 URL이면 그대로, 로컬 파일이면 contextPath 붙임 --%>
                                <c:choose>
                                    <c:when test="${fn:startsWith(userDetail.avatarUrl, 'http')}">
                                        <img src="<c:out value='${userDetail.avatarUrl}'/>" alt="프로필 사진" style="width:100%; height:100%; object-fit:cover;">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${pageContext.request.contextPath}<c:out value='${userDetail.avatarUrl}'/>" alt="프로필 사진" style="width:100%; height:100%; object-fit:cover;">
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:otherwise>
                                <i class="fa-solid fa-user"></i>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="avatar-actions">
                        <p class="avatar-label">프로필 사진</p>

                        <%-- 탭 전환: 파일 업로드 / URL 입력 --%>
                        <div class="avatar-tabs">
                            <button type="button" class="avatar-tab active" id="tabFile"
                                    onclick="switchTab('file')">
                                <i class="fa-solid fa-upload"></i> 파일 업로드
                            </button>
                            <button type="button" class="avatar-tab" id="tabUrl"
                                    onclick="switchTab('url')">
                                <i class="fa-solid fa-link"></i> URL 입력
                            </button>
                        </div>

                        <%-- 파일 업로드 영역 --%>
                        <div class="avatar-panel" id="panelFile">
                            <label class="file-upload-btn" for="avatarFile">
                                <i class="fa-solid fa-image"></i> 파일 선택
                            </label>
                            <input type="file" name="avatarFile" id="avatarFile"
                                   accept="image/*" style="display: none;">
                            <span class="file-name-display" id="fileNameDisplay">선택된 파일 없음</span>
                        </div>

                        <%-- URL 입력 영역 --%>
                        <div class="avatar-panel" id="panelUrl" style="display: none;">
                            <input type="text" name="avatarUrl" class="profile-input"
                                   placeholder="이미지 URL을 입력하세요"
                                   value="<c:out value='${userDetail.avatarUrl}'/>"
                                   id="avatarUrlInput">
                        </div>

                        <p class="help-text">jpg, png, gif 등 이미지 파일 또는 외부 이미지 URL을 사용할 수 있습니다.</p>
                    </div>
                </div>

                <div class="divider"></div>

                <%-- 닉네임 --%>
                <div class="field-group">
                    <label class="field-label">닉네임 <span class="required">*</span></label>
                    <input type="text" name="nickname" class="profile-input"
                           placeholder="닉네임을 입력하세요"
                           value="<c:out value='${userDetail.nickname}'/>"
                           required>
                </div>

                <%-- 인사말 --%>
                <div class="field-group">
                    <label class="field-label">인사말</label>
                    <textarea name="bio" class="profile-textarea"
                              placeholder="자신을 소개하는 짧은 인사말을 남겨보세요."><c:out value="${userDetail.bio}"/></textarea>
                </div>

                <div class="form-footer">
                    <a href="${pageContext.request.contextPath}/l_check/user/mypage.do"
                       class="btn btn-outline">취소</a>
                    <button type="submit" class="btn btn-primary">수정 완료</button>
                </div>
            </form>
        </div>
    </main>
</div>

<script>
    // 탭 전환
    function switchTab(tab) {
        const panelFile = document.getElementById('panelFile');
        const panelUrl  = document.getElementById('panelUrl');
        const tabFile   = document.getElementById('tabFile');
        const tabUrl    = document.getElementById('tabUrl');

        if (tab === 'file') {
            panelFile.style.display = '';
            panelUrl.style.display  = 'none';
            tabFile.classList.add('active');
            tabUrl.classList.remove('active');
        } else {
            panelFile.style.display = 'none';
            panelUrl.style.display  = '';
            tabFile.classList.remove('active');
            tabUrl.classList.add('active');
        }
    }

    // 파일 선택 시 미리보기
    document.getElementById('avatarFile').addEventListener('change', function () {
        const file = this.files[0];
        const display = document.getElementById('fileNameDisplay');
        const preview = document.getElementById('avatarPreview');

        if (file) {
            display.textContent = file.name;
            const reader = new FileReader();
            reader.onload = function (e) {
                preview.innerHTML = '<img src="' + e.target.result + '" alt="프로필 사진" style="width:100%; height:100%; object-fit:cover;">';
            };
            reader.readAsDataURL(file);
        } else {
            display.textContent = '선택된 파일 없음';
        }
    });

    // URL 입력 시 미리보기
    document.getElementById('avatarUrlInput').addEventListener('input', function () {
        const preview = document.getElementById('avatarPreview');
        const url = this.value.trim();
        if (url) {
            preview.innerHTML = '<img src="' + url + '" alt="프로필 사진" style="width:100%; height:100%; object-fit:cover;">';
        } else {
            preview.innerHTML = '<i class="fa-solid fa-user"></i>';
        }
    });
</script>
</body>
</html>
