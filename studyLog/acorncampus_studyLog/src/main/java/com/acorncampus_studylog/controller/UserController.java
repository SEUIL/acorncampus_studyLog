package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.UserDto;
import com.acorncampus_studylog.service.UserService;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/user/*")
public class UserController extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            path = "/login.do";
        }

        switch (path) {
            case "/login.do":
                handleLoginForm(req, resp);
                break;
            case "/logout.do":
                handleLogout(req, resp);
                break;
            case "/reg.do":
                handleRegForm(req, resp);
                break;
            case "/checkEmail.do":
                handleCheckEmail(req, resp);
                break;
            case "/password.do":
                handlePasswordForm(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            path = "";
        }

        switch (path) {
            case "/login.do":
                handleLogin(req, resp);
                break;
            case "/reg.do":
                handleRegister(req, resp);
                break;
            case "/password.do":
                handlePassword(req, resp);
                break;
            case "/delete.do":
                handleDelete(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleLoginForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            resp.sendRedirect(req.getContextPath() + "/l_check/user/mypage.do");
            return;
        }

        req.setAttribute("authMode", "login");
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/user/login.do");
    }

    private void handleRegForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("authMode", "register");
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }

    private void handleCheckEmail(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String email = req.getParameter("email");
        // TODO: boolean available = userService.checkEmailAvailable(email)
        boolean available = email != null && !email.trim().isEmpty();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("available", available);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(new Gson().toJson(result));
    }

    private void handlePasswordForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loginUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/user/login.do");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/views/user/password.jsp").forward(req, resp);
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");

        // TODO: UserDto user = userService.login(email, password) → 실패 시 errorMsg + index.jsp(authMode=login) forward
        UserDto tempUser = new UserDto();
        tempUser.setUserId(1);
        tempUser.setEmail(email != null ? email : "temp@studylog.dev");
        tempUser.setUsername("임시사용자");
        tempUser.setRole("ADMIN");
        tempUser.setIsBanned("N");

        req.getSession(true).setAttribute("loginUser", tempUser);
        resp.sendRedirect(req.getContextPath() + "/l_check/user/mypage.do");
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 입력값 검증 → userService.register → redirect
        resp.sendRedirect(req.getContextPath() + "/user/login.do");
    }

    private void handlePassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 세션에서 userId → userService.changePassword
        resp.sendRedirect(req.getContextPath() + "/l_check/user/mypage.do");
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: userService.deleteAccount(userId, password) → session.invalidate
        resp.sendRedirect(req.getContextPath() + "/user/login.do");
    }
}
