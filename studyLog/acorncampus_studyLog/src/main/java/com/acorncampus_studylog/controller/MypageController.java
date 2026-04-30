package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.SeriesDto;
import com.acorncampus_studylog.service.PostService;
import com.acorncampus_studylog.service.SeriesService;
import com.acorncampus_studylog.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 마이페이지 / 프로필 수정 컨트롤러 (로그인 필수 — LoginCheckFilter 적용)
 * URL 패턴: /l_check/user/*
 *
 * GET  /l_check/user/mypage.do → 마이페이지 (내 게시글, 시리즈)
 * GET  /l_check/user/update.do → 프로필 수정 폼
 * POST /l_check/user/update.do → 프로필 수정 처리
 */
@WebServlet("/l_check/user/*")
public class MypageController extends HttpServlet {

    private final UserService   userService   = new UserService();
    private final PostService   postService   = new PostService();
    private final SeriesService seriesService = new SeriesService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/mypage.do";

        switch (path) {
            case "/mypage.do": handleMypage(req, resp); break;
            case "/update.do": handleUpdateForm(req, resp); break;
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
            case "/update.do": handleUpdate(req, resp); break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /l_check/user/mypage.do → 내 게시글/시리즈 목록
     * forward: /WEB-INF/views/user/mypage.jsp
     */
    private void handleMypage(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<SeriesDto> seriesList = new ArrayList<>();

        SeriesDto series = new SeriesDto();
        series.setSeriesId(201);
        series.setUserId(1);
        series.setName("임시 시리즈");
        series.setDescription("내 시리즈 복귀 동선 확인용 임시 데이터입니다.");
        series.setPostCount(1);
        seriesList.add(series);

        req.setAttribute("seriesList", seriesList);
        req.getRequestDispatcher("/WEB-INF/views/workspace_main.jsp").forward(req, resp);
    }

    /**
     * GET /l_check/user/update.do → 프로필 수정 폼 (현재 정보 미리채움)
     * forward: /WEB-INF/views/user/update.jsp
     */
    private void handleUpdateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 세션에서 userId → userService.getUserById → setAttribute("user", ...)
        //       → forward /WEB-INF/views/user/update.jsp
    }

    /**
     * POST /l_check/user/update.do → 프로필 수정 처리
     * 성공: /l_check/user/mypage.do 리다이렉트
     * forward: /WEB-INF/views/user/update.jsp
     */
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 파라미터 nickname, bio, avatarUrl → userService.updateProfile
        //       → 성공 시 세션 loginUser 닉네임 갱신 → redirect
        // 파일 업로드(아바타)는 commons-fileupload로 처리
    }
}
