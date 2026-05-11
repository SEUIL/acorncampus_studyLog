<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>새 비밀번호 설정 - 스터디로그</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/global_theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/typography.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/form.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/components/ui.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">

    <style>
        .auth-wrapper {
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 40px 20px;
        }

        .auth-card {
            width: 100%;
            max-width: 420px;
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-md);
            box-shadow: var(--shadow-sm);
            padding: 36px;
        }

        .auth-logo {
            font-size: 18px;
            font-weight: 800;
            color: var(--text-main);
            margin-bottom: 20px;
        }

        .auth-logo i {
            color: var(--accent-color);
            margin-right: 6px;
        }

        .auth-header {
            margin-bottom: 24px;
        }

        .form-group {
            margin-bottom: 16px;
        }

        .form-group label {
            display: block;
            font-size: 13px;
            font-weight: 700;
            color: var(--text-main);
            margin-bottom: 6px;
        }

        .input-control {
            width: 100%;
        }

        .auth-message {
            padding: 14px;
            border-radius: var(--radius-sm);
            font-size: 13px;
            margin-bottom: 16px;
            border: 1px solid var(--border-color);
            background: var(--list-hover);
            color: #ef5350;
        }

        .auth-footer {
            margin-top: 20px;
            text-align: center;
        }

        .auth-footer a {
            justify-content: center;
        }
    </style>
</head>
<body>

<div class="auth-wrapper">
    <main class="auth-card">

        <div class="auth-logo">
            <i class="fa-solid fa-book-open"></i> 스터디로그
        </div>

        <header class="auth-header">
            <h1>새 비밀번호 설정</h1>
            <p class="text-sub">
                새로 사용할 비밀번호를 입력해 주세요.
            </p>
        </header>

        <c:if test="${not empty errorMsg}">
            <div class="auth-message">
                <c:out value="${errorMsg}" />
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/user/pwd-reset/reset.do" method="post">
            <input type="hidden" name="token" value="<c:out value='${token}'/>">

            <div class="form-group">
                <label for="newPassword">새 비밀번호</label>
                <input type="password"
                       id="newPassword"
                       name="newPassword"
                       class="input-control"
                       placeholder="8자리 이상 영문,숫자 포함 입력"
                       minlength="8"
                       pattern="^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9]{8,}$"
                       required>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%;">
                비밀번호 변경
            </button>
        </form>

        <div class="auth-footer">
            <a href="${pageContext.request.contextPath}/user/login.do" class="breadcrumb">
                <i class="fa-solid fa-arrow-left"></i>
                로그인으로 돌아가기
            </a>
        </div>

    </main>
</div>

<script src="${pageContext.request.contextPath}/resources/js/page-transition.js"></script>
</body>
</html>