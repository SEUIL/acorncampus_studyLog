<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>스터디로그 - 로그인</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/auth/login.css">
</head>
<body>
<div class="main-wrapper">
    <section class="interaction-section">
        <svg id="jelly-container" viewBox="0 0 500 400" preserveAspectRatio="xMidYMid meet">
            <g id="char-purple">
                <path class="body" d="M85,360 L85,86 Q85,48 123,48 Q161,48 161,86 L161,360 Z" fill="#7C5CE7"/>
                <circle class="eye-white" cx="104" cy="100" r="7.5" fill="white"/>
                <circle class="pupil" cx="104" cy="100" r="3.5" fill="#222"/>
                <circle class="eye-white" cx="142" cy="100" r="7.5" fill="white"/>
                <circle class="pupil" cx="142" cy="100" r="3.5" fill="#222"/>
            </g>
            <g id="char-orange">
                <path class="body" d="M110,360 L110,271 Q110,186 195,186 Q280,186 280,271 L280,360 Z" fill="#FF9F43"/>
                <circle class="eye-white" cx="165" cy="235" r="13" fill="white"/>
                <circle class="pupil" cx="165" cy="235" r="6" fill="#222"/>
                <circle class="eye-white" cx="225" cy="235" r="13" fill="white"/>
                <circle class="pupil" cx="225" cy="235" r="6" fill="#222"/>
                <path class="mouth" d="M175,258 Q195,274 215,258" stroke="#333" stroke-width="3" fill="none" stroke-linecap="round"/>
            </g>
            <g id="char-pink">
                <path class="body" d="M238,360 L238,196 Q238,150 284,150 Q330,150 330,196 L330,360 Z" fill="#E84393"/>
                <circle class="eye-white" cx="263" cy="204" r="9" fill="white"/>
                <circle class="pupil" cx="263" cy="204" r="4.2" fill="#222"/>
                <circle class="eye-white" cx="305" cy="204" r="9" fill="white"/>
                <circle class="pupil" cx="305" cy="204" r="4.2" fill="#222"/>
            </g>
            <g id="char-yellow">
                <path class="body" d="M295,360 L295,310 Q295,250 355,250 Q415,250 415,310 L415,360 Z" fill="#FECA57"/>
                <circle class="eye-white" cx="325" cy="316" r="9" fill="white"/>
                <circle class="pupil" cx="325" cy="316" r="4.2" fill="#222"/>
                <circle class="eye-white" cx="385" cy="316" r="9" fill="white"/>
                <circle class="pupil" cx="385" cy="316" r="4.2" fill="#222"/>
                <line class="mouth" x1="328" y1="338" x2="382" y2="338" stroke="#333" stroke-width="3" stroke-linecap="round"/>
            </g>
        </svg>
    </section>

    <section class="login-section">
        <div class="login-container">
            <header class="login-header">
                <div class="text-logo"><i class="fa-solid fa-book-open" style="color: var(--accent-color);"></i> 스터디로그</div>
                <h1>Welcome back!</h1>
                <p class="text-sub">학습과 기록을 다시 이어가 보세요.</p>
            </header>

            <c:if test="${not empty errorMsg}">
                <div class="error-msg"><c:out value="${errorMsg}"/></div>
            </c:if>

            <form action="${pageContext.request.contextPath}/user/login.do" method="post">
                <div class="form-group">
                    <label for="email">이메일</label>
                    <input type="email" id="email" name="email" class="input-control"
                           placeholder="example@domain.com"
                           value="<c:out value='${param.email}'/>" required>
                </div>

                <div class="form-group">
                    <label for="password">비밀번호</label>
                    <input type="password" id="password" name="password" class="input-control"
                           placeholder="비밀번호를 입력하세요" required>
                </div>

                <div class="form-options">
                    <label style="cursor: pointer; display: flex; align-items: center; gap: 5px;">
                        <input type="checkbox"> 자동 로그인
                    </label>
                    <a href="javascript:void(0)">비밀번호 찾기</a>
                </div>

                <button type="submit" class="btn-login">로그인</button>

                <div class="divider">OR</div>

                <button type="button" class="btn-google">
                    <img src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" alt="Google Logo" style="width: 18px;">
                    Google 계정으로 로그인
                </button>
            </form>

            <div class="signup-link">
                계정이 없으신가요? <a href="${pageContext.request.contextPath}/user/reg.do">회원가입</a>
            </div>
        </div>
    </section>
</div>
<script src="${pageContext.request.contextPath}/resources/js/index-interaction.js"></script>
</body>
</html>

