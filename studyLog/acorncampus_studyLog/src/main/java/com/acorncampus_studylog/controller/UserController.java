package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.UserDetailDto;
import com.acorncampus_studylog.dto.UserDto;
import com.acorncampus_studylog.service.PostService;
import com.acorncampus_studylog.service.SeriesService;
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

    private final UserService   userService   = new UserService();
    private final PostService   postService   = new PostService();
    private final SeriesService seriesService = new SeriesService();

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
            case "/profile.do":
                handlePublicProfile(req, resp);
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

    /**
     * GET /user/profile.do?id={userId}
     * 타인(또는 본인)의 공개 프로필 + 공개 시리즈 + 공개 게시글 표시
     * 로그인 불필요 — /user/* 패턴이므로 LoginCheckFilter 미적용
     */
    private void handlePublicProfile(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. userId 파라미터 파싱 — 없거나 숫자가 아니면 404
        int targetUserId;
        try {
            targetUserId = Integer.parseInt(req.getParameter("id"));
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 2. 대상 사용자 정보 조회 — 탈퇴·밴 유저는 404 처리
        UserDetailDto targetUser = userService.getUserById(targetUserId);
        if (targetUser == null || "Y".equals(targetUser.getIsBanned())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 3. 공개 시리즈 목록
        java.util.List<com.acorncampus_studylog.dto.SeriesDto> seriesList =
                seriesService.getSeriesByUser(targetUserId);
        if (seriesList == null) seriesList = java.util.Collections.emptyList();

        // 4. 공개 게시글 목록 (비공개 제외)
        int pageNo;
        try {
            pageNo = Integer.parseInt(req.getParameter("page"));
            if (pageNo < 1) pageNo = 1;
        } catch (Exception e) {
            pageNo = 1;
        }

        // 5. request에 데이터 세팅 후 뷰 포워드
        req.setAttribute("targetUser",  targetUser);
        req.setAttribute("seriesList",  seriesList);
        req.setAttribute("postList",    postService.getPublicPostsByUser(targetUserId, pageNo));
        req.setAttribute("postPage",    postService.getPublicPostPageByUser(targetUserId, pageNo));
        req.getRequestDispatcher("/WEB-INF/views/user/public_profile.jsp").forward(req, resp);
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

        boolean available = userService.checkEmailAvailable(email);

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
        String password = req.getParameter("password");

        // TODO: UserDto user = userService.login(email, password) → 실패 시 errorMsg + index.jsp(authMode=login) forward

        // 로그인
        UserDetailDto user = userService.login(email, password);

        // 로그인 실패
        if (user == null) {
            req.setAttribute("errorMsg", "이메일 또는 비밀번호가 올바르지 않습니다.");
            req.getRequestDispatcher("/index.jsp?authMode=login").forward(req,resp);

            return;
        }

        // 세션 생성
        HttpSession session = req.getSession();

        // 세션에 로그인 사용자 저장
        session.setAttribute("loginUser", user.toSessionDto());

        // 마이페이지 이동
        resp.sendRedirect(req.getContextPath() +"/l_check/user/mypage.do");

    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 입력값 검증 → userService.register → redirect

        String email = req.getParameter("email");
        String nickname = req.getParameter("nickname");
        String password = req.getParameter("password");

        // 입력값 검증
        if (email == null || email.trim().isEmpty()
            || nickname == null || nickname.trim().isEmpty()
            || password == null || password.trim().isEmpty()){

            req.setAttribute("errorMsg", "모든 정보를 입력해주세요");

            // 회원가입 창 실행
            req.getRequestDispatcher("/index.jsp?authMode=register").forward(req, resp);

            return;
        }

        // 회원가입
        int userId = userService.register(email, nickname, password);

        // 회원가입 실패
        if (userId == 0 ) {

            req.setAttribute("errorMsg", "이미 사용 중인 이메일 또는 닉네임입니다.");

            req.getRequestDispatcher("/index.jsp?authMode=register").forward(req,resp);

            return;
        }

        // 회원가입 성공
        resp.sendRedirect(req.getContextPath() + "/user/login.do");
    }


    private void handlePassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 세션에서 userId → userService.changePassword

        // 세션 가져오기
        HttpSession session = req.getSession(false);

        // 로그인 확인
        if (session == null || session.getAttribute("loginUser")== null){
            resp.sendRedirect(req.getContextPath() + "/user/login.do");
            return;

        }

        // 세션 사용자 정보
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        int userId = loginUser.getUserId();

        // 입력값 받기
        String oldPassword = req.getParameter("oldPassword");
        String newPassword = req.getParameter("newPassword");

        // 비밀번호 변경
        boolean ok = userService.changePassword(userId, oldPassword, newPassword);

        // 실패
        if (!ok){
            req.setAttribute("errorMsg", "현재 비밀번호가 일치하지 않습니다");
            req.getRequestDispatcher("/WEB-INF/views/user/password.jsp").forward(req, resp);

            return;
        }

        // 성공
        resp.sendRedirect(req.getContextPath() + "/l_check/user/mypage.do");
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: userService.deleteAccount(userId, password) → session.invalidate

        // 세션 가져오기
        HttpSession session = req.getSession(false);

        // 로그인 확인
        if (session == null || session.getAttribute("loginUser")== null){
            resp.sendRedirect(req.getContextPath() + "/user/login.do");
            return;

        }

        // 세션 사용자 정보
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");
        int userId = loginUser.getUserId();

        // 비밀번호 확인
        String password = req.getParameter("password");

        // 탈퇴 처리
        boolean ok = userService.deleteAccount(userId,password);

        // 실패
        if ( !ok) {
            req.setAttribute("errorMsg", "비밀번호가 일치하지 않습니다");
            req.getRequestDispatcher("/WEB-INF/views/user/mypage.jsp").forward(req,resp);

            return;
        }

        // 성공
        session.invalidate();

        // 로그인 페이지 이동
        resp.sendRedirect(req.getContextPath() + "/user/login.do");
    }
}
