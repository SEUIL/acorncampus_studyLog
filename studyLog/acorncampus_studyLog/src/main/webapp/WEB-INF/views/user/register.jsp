<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  회원가입 폼 fragment — index.jsp 에 <jsp:include> 되어 렌더링됨
  단독 접근 불가 (WEB-INF 내부). UserController.handleRegForm() 이 index.jsp 로 forward 함.
  에러 표시: req.setAttribute("errorMsg", "...") 후 index.jsp(authMode=register) 로 forward
--%>
<section class="login-section">
    <div class="login-container">
        <header class="login-header">
            <div class="text-logo"><i class="fa-solid fa-book-open" style="color: var(--accent-color);"></i> 스터디로그</div>
            <h1>계정 만들기</h1>
            <p class="text-sub">학습 성장을 기록할 준비가 되셨나요?</p>
        </header>

        <c:if test="${not empty errorMsg}">
            <div class="error-msg"><c:out value="${errorMsg}"/></div>
        </c:if>

        <form action="${pageContext.request.contextPath}/user/reg.do" method="post">
            <div class="form-group">
                <label for="email">이메일</label>
                <input type="email" id="email" name="email" class="input-control"
                       placeholder="example@domain.com"
                       value="<c:out value='${param.email}'/>" required>
            </div>

            <div class="form-group">
                <label for="nickname">닉네임</label>
                <input type="text" id="nickname" name="nickname" class="input-control"
                       placeholder="사용할 닉네임을 입력하세요"
                       value="<c:out value='${param.nickname}'/>" required>
            </div>

            <div class="form-group">
                <label for="password">비밀번호</label>
                <input type="password" id="password" name="password" class="input-control"
                       placeholder="8자 이상 영문, 숫자 조합" required>
            </div>

            <div class="form-group">
                <label for="passwordConfirm">비밀번호 확인</label>
                <input type="password" id="passwordConfirm" name="passwordConfirm" class="input-control"
                       placeholder="비밀번호를 한 번 더 입력하세요" required>
            </div>

            <button type="submit" class="btn-login">가입하기</button>

            <div class="divider">OR</div>

            <button type="button" class="btn-google">
                <img src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" alt="Google Logo" style="width: 18px;">
                Google 계정으로 가입
            </button>
        </form>

        <div class="signup-link">
            이미 계정이 있으신가요? <a href="${pageContext.request.contextPath}/user/login.do">로그인</a>
        </div>
    </div>
</section>
