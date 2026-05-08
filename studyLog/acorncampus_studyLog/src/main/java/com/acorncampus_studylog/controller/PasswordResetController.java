package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.PasswordResetTokenDto;
import com.acorncampus_studylog.service.PasswordResetService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 비밀번호 재설정 컨트롤러
 *
 * <p>URL 패턴: {@code /user/pwd-reset/*}
 * <ul>
 *   <li>GET  /user/pwd-reset/forgot.do → 이메일 입력 폼 표시</li>
 *   <li>POST /user/pwd-reset/forgot.do → 재설정 메일 발송 처리</li>
 *   <li>GET  /user/pwd-reset/reset.do?token=xxx → 새 비밀번호 입력 폼 표시</li>
 *   <li>POST /user/pwd-reset/reset.do → 비밀번호 변경 처리</li>
 * </ul>
 *
 * <p>JSP 경로 (팀원이 생성):
 * <ul>
 *   <li>{@code /WEB-INF/views/user/forgot_password.jsp}</li>
 *   <li>{@code /WEB-INF/views/user/reset_password.jsp}</li>
 * </ul>
 */
@WebServlet("/user/pwd-reset/*")
public class PasswordResetController extends HttpServlet {

    private final PasswordResetService resetService = new PasswordResetService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        switch (path) {
            case "/forgot.do":
                handleForgotForm(req, resp);
                break;
            case "/reset.do":
                handleResetForm(req, resp);
                break;
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
            case "/forgot.do":
                handleForgotSubmit(req, resp);
                break;
            case "/reset.do":
                handleResetSubmit(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // ── GET 핸들러 ────────────────────────────────────────────────────────────

    /**
     * GET /user/pwd-reset/forgot.do
     * 이메일 입력 폼을 표시한다.
     *
     * <p>구현 힌트: forgot_password.jsp 로 forward
     */
    private void handleForgotForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: req.getRequestDispatcher("/WEB-INF/views/user/forgot_password.jsp").forward(req, resp);

        req.getRequestDispatcher("/WEB-INF/views/user/forgot_password.jsp").forward(req, resp);
    }

    /**
     * GET /user/pwd-reset/reset.do?token=xxx
     * URL 파라미터에서 token을 꺼내 유효성을 검증한 뒤 새 비밀번호 입력 폼을 표시한다.
     *
     * <p>구현 힌트:
     * <ol>
     *   <li>req.getParameter("token") 으로 토큰 추출</li>
     *   <li>resetService.validateToken(token) 으로 유효성 확인</li>
     *   <li>유효하지 않으면 req.setAttribute("errorMsg", "유효하지 않거나 만료된 링크입니다.")
     *       후 forgot_password.jsp 로 forward</li>
     *   <li>유효하면 req.setAttribute("token", token) 후 reset_password.jsp 로 forward</li>
     * </ol>
     */
    private void handleResetForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 구현 필요

        // 토큰 추출
        String token = req.getParameter("token");

        // 유효성 확인
        if (token == null || resetService.validateToken(token) == null ) {
            req.setAttribute("errorMsg", "유효하지 않거나 만료된 링크입니다.");
            req.getRequestDispatcher("/WEB-INF/views/user/forgot_password.jsp").forward(req,resp);

            return;
        }

        req.setAttribute("token", token);
        req.getRequestDispatcher("/WEB-INF/views/user/reset_password.jsp").forward(req,resp);

    }

    // ── POST 핸들러 ───────────────────────────────────────────────────────────

    /**
     * POST /user/pwd-reset/forgot.do
     * 이메일을 받아 재설정 메일을 발송한다.
     *
     * <p>구현 힌트:
     * <ol>
     *   <li>req.getParameter("email") 로 이메일 추출 후 null/blank 검증</li>
     *   <li>resetService.requestReset(email) 호출</li>
     *   <li>성공/실패 모두 동일한 메시지 표시 (이메일 열거 공격 방지):
     *       req.setAttribute("successMsg", "입력하신 이메일로 재설정 링크를 보냈습니다.")</li>
     *   <li>forgot_password.jsp 로 forward</li>
     * </ol>
     */
    private void handleForgotSubmit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 구현 필요

        String email = req.getParameter("email");

        // 이메일 추출 후  null/blank 검증
        if(email == null || email.trim().isEmpty()){
            req.setAttribute("errorMsg", "이메일을 입력해주세요");
            req.getRequestDispatcher("/WEB-INF/views/user/forgot_password.jsp").forward(req,resp);
            return;
        }

        resetService.requestReset(email);

        // 성공/실패 모두 동일한 메시지
        req.setAttribute("successMsg", "입력하신 이메일로 재설정 링크를 보냈습니다.");
        req.getRequestDispatcher("/WEB-INF/views/user/forgot_password.jsp").forward(req,resp);

    }

    /**
     * POST /user/pwd-reset/reset.do
     * 새 비밀번호로 변경한다.
     *
     * <p>구현 힌트:
     * <ol>
     *   <li>req.getParameter("token"), req.getParameter("newPassword") 추출</li>
     *   <li>입력값 검증 (null/blank, 최소 길이 등)</li>
     *   <li>resetService.resetPassword(token, newPassword) 호출</li>
     *   <li>실패(false 반환)하면 errorMsg 설정 후 reset_password.jsp forward</li>
     *   <li>성공하면 로그인 페이지로 redirect:
     *       resp.sendRedirect(req.getContextPath() + "/user/login.do")</li>
     * </ol>
     */
    private void handleResetSubmit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 구현 필요

        String token = req.getParameter("token");
        String newPassword =  req.getParameter("newPassword");

        // 입력값 검증
        if (token == null || token.trim().isEmpty()
                || newPassword == null || newPassword.trim().length() < 6 ){
            req.setAttribute("errorMsg" ,"비밀번호는 6자리 이상 입력해주세요");
            req.setAttribute("token", token);
            req.getRequestDispatcher("/WEB-INF/views/user/reset_password.jsp").forward(req,resp);
            return;
        }

        // 비밀번호 재설정
        boolean ok = resetService.resetPassword(token, newPassword.trim());

        // 실패
        if (!ok) {
            req.setAttribute("errorMsg", "유효하지 않거나 만료된 링크입니다");
            req.setAttribute("token", token);

            req.getRequestDispatcher("/WEB-INF/views/user/reset_password.jsp").forward(req,resp);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/user/login.do");

    }
}
