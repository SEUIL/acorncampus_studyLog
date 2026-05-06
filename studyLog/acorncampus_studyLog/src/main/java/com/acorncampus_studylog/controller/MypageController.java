package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.SeriesDto;
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

@WebServlet("/l_check/user/*")
public class MypageController extends HttpServlet {

    private final UserService userService = new UserService();
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

    private void handleMypage(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: seriesService.getSeriesByUserId(loginUser.getUserId()) 로 교체
        List<SeriesDto> seriesList = new ArrayList<>();

        SeriesDto series = new SeriesDto();
        series.setSeriesId(201);
        series.setUserId(1);
        series.setName("임시 시리즈");
        series.setDescription("시리즈 목록 화면 연결 확인용 임시 데이터입니다.");
        series.setPostCount(1);
        seriesList.add(series);

        req.setAttribute("seriesList", seriesList);
        req.getRequestDispatcher("/WEB-INF/views/user/mypage.jsp").forward(req, resp);
    }

    private void handleUpdateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/user/update.jsp").forward(req, resp);
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: userService.updateProfile(userId, nickname, bio)
        resp.sendRedirect(req.getContextPath() + "/l_check/user/mypage.do");
    }
}
