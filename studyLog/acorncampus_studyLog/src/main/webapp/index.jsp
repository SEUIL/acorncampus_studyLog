<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>나만의 학습 기록 블로그 - Welcome</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <c:choose>
        <c:when test="${authMode eq 'register'}">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/auth/register.css">
        </c:when>
        <c:otherwise>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/auth/login.css">
        </c:otherwise>
    </c:choose>
</head>
<body>
<div class="main-wrapper">

    <%-- 좌측: 젤리 캐릭터 연출 영역 (JS 인터랙션 포함) --%>
    <section class="interaction-section">
        <svg id="jelly-container" viewBox="0 0 500 400" preserveAspectRatio="xMidYMid meet">

            <%-- ① 보라: x=85, w=76, topY=48 --%>
            <g id="char-purple">
                <path class="body" d="M85,360 L85,86 Q85,48 123,48 Q161,48 161,86 L161,360 Z" fill="#7C5CE7"/>
                <circle class="eye-white" cx="104" cy="100" r="7.5" fill="white"/>
                <circle class="pupil"     cx="104" cy="100" r="3.5" fill="#222"/>
                <circle class="eye-white" cx="142" cy="100" r="7.5" fill="white"/>
                <circle class="pupil"     cx="142" cy="100" r="3.5" fill="#222"/>
            </g>

            <%-- ② 주황: x=110, w=170, topY=186 --%>
            <g id="char-orange">
                <path class="body" d="M110,360 L110,271 Q110,186 195,186 Q280,186 280,271 L280,360 Z" fill="#FF9F43"/>
                <circle class="eye-white" cx="165" cy="235" r="13"  fill="white"/>
                <circle class="pupil"     cx="165" cy="235" r="6"   fill="#222"/>
                <circle class="eye-white" cx="225" cy="235" r="13"  fill="white"/>
                <circle class="pupil"     cx="225" cy="235" r="6"   fill="#222"/>
                <path class="mouth" d="M175,258 Q195,274 215,258" stroke="#333" stroke-width="3" fill="none" stroke-linecap="round"/>
            </g>

            <%-- ③ 핑크: x=238, w=92, topY=150 --%>
            <g id="char-pink">
                <path class="body" d="M238,360 L238,196 Q238,150 284,150 Q330,150 330,196 L330,360 Z" fill="#E84393"/>
                <circle class="eye-white" cx="263" cy="204" r="9"   fill="white"/>
                <circle class="pupil"     cx="263" cy="204" r="4.2" fill="#222"/>
                <circle class="eye-white" cx="305" cy="204" r="9"   fill="white"/>
                <circle class="pupil"     cx="305" cy="204" r="4.2" fill="#222"/>
            </g>

            <%-- ④ 노랑: x=295, w=120, topY=250 --%>
            <g id="char-yellow">
                <path class="body" d="M295,360 L295,310 Q295,250 355,250 Q415,250 415,310 L415,360 Z" fill="#FECA57"/>
                <circle class="eye-white" cx="325" cy="316" r="9"   fill="white"/>
                <circle class="pupil"     cx="325" cy="316" r="4.2" fill="#222"/>
                <circle class="eye-white" cx="385" cy="316" r="9"   fill="white"/>
                <circle class="pupil"     cx="385" cy="316" r="4.2" fill="#222"/>
                <line class="mouth" x1="328" y1="338" x2="382" y2="338" stroke="#333" stroke-width="3" stroke-linecap="round"/>
            </g>

            <%-- Zzz 수면 애니메이션 — 인트로 오버레이가 뜰 때(에러 없는 첫 진입)만 표시
                 로그인 실패 재진입 시에는 렌더링하지 않음 --%>
            <c:if test="${authMode ne 'register' and empty errorMsg}">
            <g id="zzz-group" font-family="Pretendard, sans-serif" font-style="italic" font-weight="700">
                <%-- 보라 위 (topY=48, centerX=123) --%>
                <text class="zzz z1" x="130" y="40"  fill="rgba(255,255,255,0.55)" font-size="13">z</text>
                <text class="zzz z2" x="141" y="26"  fill="rgba(255,255,255,0.55)" font-size="17">z</text>
                <text class="zzz z3" x="154" y="12"  fill="rgba(255,255,255,0.55)" font-size="21">Z</text>
                <%-- 주황 위 (topY=186, centerX=195) --%>
                <text class="zzz z1" x="202" y="178" fill="rgba(255,255,255,0.55)" font-size="13">z</text>
                <text class="zzz z2" x="214" y="163" fill="rgba(255,255,255,0.55)" font-size="17">z</text>
                <text class="zzz z3" x="228" y="148" fill="rgba(255,255,255,0.55)" font-size="21">Z</text>
                <%-- 핑크 위 (topY=150, centerX=284) --%>
                <text class="zzz z1" x="290" y="142" fill="rgba(255,255,255,0.55)" font-size="13">z</text>
                <text class="zzz z2" x="302" y="128" fill="rgba(255,255,255,0.55)" font-size="17">z</text>
                <text class="zzz z3" x="315" y="114" fill="rgba(255,255,255,0.55)" font-size="21">Z</text>
                <%-- 노랑 위 (topY=250, centerX=355) --%>
                <text class="zzz z1" x="361" y="243" fill="rgba(255,255,255,0.55)" font-size="13">z</text>
                <text class="zzz z2" x="373" y="229" fill="rgba(255,255,255,0.55)" font-size="17">z</text>
                <text class="zzz z3" x="386" y="215" fill="rgba(255,255,255,0.55)" font-size="21">Z</text>
            </g>
            </c:if>
        </svg>
    </section>

    <%-- 우측: authMode에 따라 로그인 또는 회원가입 폼 include --%>
    <c:choose>
        <c:when test="${authMode eq 'register'}">
            <jsp:include page="/WEB-INF/views/user/register.jsp"/>
        </c:when>
        <c:otherwise>
            <jsp:include page="/WEB-INF/views/user/login.jsp"/>
        </c:otherwise>
    </c:choose>

</div>
<script src="${pageContext.request.contextPath}/resources/js/page-transition.js"></script>
<script src="${pageContext.request.contextPath}/resources/js/index-interaction.js"></script>

<%-- 불끄기 인트로 오버레이 — 로그인 페이지 + 에러 없을 때만 표시
     로그인 실패(errorMsg 있음) 시에는 오버레이를 건너뛰고 폼을 바로 노출 --%>
<c:choose>
    <c:when test="${authMode ne 'register' and empty errorMsg}">
        <%-- 정상 진입: 인트로 오버레이 표시 --%>
        <div id="intro-overlay">
            <button id="light-switch-btn" aria-label="불 켜기">
                <i class="fa-solid fa-lightbulb"></i>
            </button>
            <p class="light-hint">불을 켜주세요</p>
        </div>
    </c:when>
    <c:when test="${authMode ne 'register' and not empty errorMsg}">
        <%-- 로그인 실패 재진입: 오버레이 없이 폼 즉시 노출 --%>
        <script>
            document.addEventListener('DOMContentLoaded', function () {
                var ls = document.querySelector('.login-section');
                if (ls) ls.classList.add('revealed');
            });
        </script>
    </c:when>
</c:choose>
</body>
</html>
