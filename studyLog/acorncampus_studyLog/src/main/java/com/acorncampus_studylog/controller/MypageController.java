package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.SeriesDto;
import com.acorncampus_studylog.dto.UserDto;
import com.acorncampus_studylog.service.PostService;
import com.acorncampus_studylog.service.SeriesService;
import com.acorncampus_studylog.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet("/l_check/user/*")
public class MypageController extends HttpServlet {

    private final UserService   userService   = new UserService();
    private final PostService   postService   = new PostService();
    private final SeriesService seriesService = new SeriesService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            path = "/mypage.do";
        }

        switch (path) {
            case "/mypage.do":
                handleMypage(req, resp);
                break;
            case "/update.do":
                handleUpdateForm(req, resp);
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
            case "/update.do":
                handleUpdate(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /l_check/user/mypage.do?page={page}
     * → 마이페이지 (내 게시글 목록 + 시리즈 목록)
     */
    private void handleMypage(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // LoginCheckFilter가 /l_check/* 패턴에 걸려 있어서
        // 여기까지 왔다면 loginUser가 세션에 반드시 있음
        UserDto loginUser = getLoginUser(req);

        // page 파라미터 없으면 1페이지
        int pageNo;
        try {
            pageNo = Integer.parseInt(req.getParameter("page"));
            if (pageNo < 1) pageNo = 1;
        } catch (Exception e) {
            pageNo = 1;
        }

        // 마이페이지는 본인 글만 보여줘야 하므로 userId를 함께 전달
        // postPage는 페이지 버튼 렌더링용, postList는 실제 게시글 데이터
        req.setAttribute("postList",  postService.getPostsByUser(loginUser.getUserId(), pageNo));
        req.setAttribute("postPage",  postService.getPostPageByUser(loginUser.getUserId(), pageNo));

        // 시리즈 목록: SeriesService가 아직 TODO라서 null을 반환할 수 있음
        // JSP에서 <c:forEach>에 null이 들어가면 에러가 나므로 빈 리스트로 방어
        List<SeriesDto> seriesList = seriesService.getSeriesByUser(loginUser.getUserId());
        if (seriesList == null) seriesList = Collections.emptyList();
        req.setAttribute("seriesList", seriesList);

        req.getRequestDispatcher("/WEB-INF/views/user/mypage.jsp").forward(req, resp);
    }

    /**
     * GET /l_check/user/update.do → 프로필 수정 폼
     */
    private void handleUpdateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 수정 폼에는 현재 닉네임/소개글을 미리 채워줘야 하므로
        // 세션의 loginUser 정보를 JSP에서 ${sessionScope.loginUser.username}으로 직접 읽어감
        // 별도로 setAttribute 없이 JSP에서 세션 직접 참조
        req.getRequestDispatcher("/WEB-INF/views/user/update.jsp").forward(req, resp);
    }

    /**
     * POST /l_check/user/update.do → 프로필 수정 처리
     * 파라미터: nickname, bio, avatarUrl(optional)
     */
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserDto loginUser = getLoginUser(req);

        String nickname  = req.getParameter("nickname");
        String bio       = req.getParameter("bio");
        String avatarUrl = req.getParameter("avatarUrl");

        // 닉네임은 필수 입력값이므로 비어 있으면 폼으로 다시 forward
        if (nickname == null || nickname.trim().isEmpty()) {
            req.setAttribute("errorMsg", "닉네임을 입력해 주세요.");
            req.getRequestDispatcher("/WEB-INF/views/user/update.jsp").forward(req, resp);
            return;
        }

        // UserService.updateProfile()은 아직 TODO지만 구현되면 자동으로 연결됨
        // 내부에서 닉네임 중복 확인 + DB 업데이트를 처리함
        userService.updateProfile(loginUser.getUserId(), nickname.trim(), bio, avatarUrl);
        resp.sendRedirect(req.getContextPath() + "/l_check/user/mypage.do");
    }

    /** 세션에서 로그인 사용자 반환 */
    private UserDto getLoginUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        return (UserDto) session.getAttribute("loginUser");
    }
}
