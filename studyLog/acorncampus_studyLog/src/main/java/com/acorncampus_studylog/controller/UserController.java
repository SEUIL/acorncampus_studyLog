package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.UserDetailDto;
import com.acorncampus_studylog.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 회원 기능 컨트롤러 (로그인 불필요 기능)
 * URL 패턴: /user/*
 *
 * GET  /user/login.do    → 로그인 폼
 * POST /user/login.do    → 로그인 처리
 * GET  /user/logout.do   → 로그아웃 처리
 * GET  /user/reg.do      → 회원가입 폼
 * POST /user/reg.do      → 회원가입 처리
 * GET  /user/checkEmail.do → 이메일 중복 확인 (AJAX)
 * GET  /user/password.do → 비밀번호 변경 폼
 * POST /user/password.do → 비밀번호 변경 처리
 * POST /user/delete.do   → 회원 탈퇴 처리
 */
@WebServlet("/user/*")
public class UserController extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/login.do";

        switch (path) {
            case "/login.do":      handleLoginForm(req, resp);      break;
            case "/logout.do":     handleLogout(req, resp);         break;
            case "/reg.do":        handleRegForm(req, resp);        break;
            case "/checkEmail.do": handleCheckEmail(req, resp);     break;
            case "/password.do":   handlePasswordForm(req, resp);   break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        switch (path) {
            case "/login.do":    handleLogin(req, resp);      break;
            case "/reg.do":      handleRegister(req, resp);   break;
            case "/password.do": handlePassword(req, resp);   break;
            case "/delete.do":   handleDelete(req, resp);     break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // ── GET 핸들러 ──────────────────────────────────────────────────────────

    /**
     * GET /user/login.do → 로그인 폼 표시
     * forward: /WEB-INF/views/user/login.jsp
     * 이미 로그인 상태면 / 로 리다이렉트
     */
    private void handleLoginForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 세션에 loginUser 있으면 resp.sendRedirect(contextPath + "/")
        // TODO: req.getRequestDispatcher("/WEB-INF/views/user/login.jsp").forward(req, resp)
    }

    /**
     * GET /user/logout.do → 세션 무효화 후 로그인 페이지로 리다이렉트
     */
    private void handleLogout(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: session.invalidate() → resp.sendRedirect(contextPath + "/user/login.do")
    }

    /**
     * GET /user/reg.do → 회원가입 폼 표시
     * forward: /WEB-INF/views/user/register.jsp
     */
    private void handleRegForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: req.getRequestDispatcher("/WEB-INF/views/user/register.jsp").forward(req, resp)
    }

    /**
     * GET /user/checkEmail.do?email={email} → 이메일 중복 확인 (AJAX JSON 응답)
     * 응답: {"available": true} 또는 {"available": false}
     */
    private void handleCheckEmail(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: userService.checkEmailAvailable(email) → JSON 응답 출력
        // resp.setContentType("application/json; charset=UTF-8")
    }

    /**
     * GET /user/password.do → 비밀번호 변경 폼 (로그인 필요 — Controller에서 직접 세션 확인)
     * forward: /WEB-INF/views/user/password.jsp
     */
    private void handlePasswordForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 세션 loginUser 없으면 로그인 페이지로 → forward
    }

    // ── POST 핸들러 ─────────────────────────────────────────────────────────

    /**
     * POST /user/login.do → 로그인 처리
     * 성공: 세션에 loginUser 저장 → / 리다이렉트
     * 실패: login.jsp에 errorMsg setAttribute 후 forward
     */
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: userService.login(email, pw) → 세션 저장 → redirect
        // 파라미터: email, password
    }

    /**
     * POST /user/reg.do → 회원가입 처리
     * 성공: /user/login.do 리다이렉트
     * 실패: register.jsp에 errorMsg 후 forward
     */
    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 입력값 검증 → userService.register → redirect
        // 파라미터: email, nickname, password, passwordConfirm
    }

    /**
     * POST /user/password.do → 비밀번호 변경 처리
     * 성공: / 리다이렉트
     * 실패: 폼으로 forward + 오류 메시지
     */
    private void handlePassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 세션에서 userId → userService.changePassword → redirect
        // 파라미터: oldPassword, newPassword, newPasswordConfirm
    }

    /**
     * POST /user/delete.do → 회원 탈퇴 처리
     * 성공: 세션 무효화 → /user/login.do 리다이렉트
     * 실패: 폼으로 forward + 오류 메시지
     */
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 세션에서 userId → userService.deleteAccount(userId, password) → 세션 무효화 → redirect
        // 파라미터: password
    }
}
