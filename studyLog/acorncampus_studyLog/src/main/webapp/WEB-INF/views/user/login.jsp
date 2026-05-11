<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  로그인 폼 fragment — index.jsp 에 <jsp:include> 되어 렌더링됨
  단독 접근 불가 (WEB-INF 내부). UserController.handleLoginForm() 이 index.jsp 로 forward 함.
  에러 표시: req.setAttribute("errorMsg", "...") 후 index.jsp(authMode=login) 로 forward
--%>
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
                <a href="${pageContext.request.contextPath}/user/pwd-reset/forgot.do">
                    비밀번호 찾기
                </a>
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
