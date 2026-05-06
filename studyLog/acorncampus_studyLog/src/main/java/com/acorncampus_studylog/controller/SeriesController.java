package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.PostDto;
import com.acorncampus_studylog.dto.SeriesDto;
import com.acorncampus_studylog.service.SeriesService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/series/*", "/l_check/series/*"})
public class SeriesController extends HttpServlet {

    private final SeriesService seriesService = new SeriesService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            path = "/list.do";
        }

        switch (path) {
            case "/list.do":
                handleList(req, resp);
                break;
            case "/detail.do":
                handleDetail(req, resp);
                break;
            case "/write.do":
                handleWriteForm(req, resp);
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
            case "/write.do":
                handleWrite(req, resp);
                break;
            case "/update.do":
                handleUpdate(req, resp);
                break;
            case "/delete.do":
                handleDelete(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: seriesService.getPublicSeriesList(page) 로 교체
        List<SeriesDto> sampleSeries = new ArrayList<>();
        SeriesDto series = new SeriesDto();
        series.setSeriesId(201);
        series.setUserId(1);
        series.setName("샘플 시리즈");
        series.setDescription("시리즈 화면 연결 확인용 샘플 설명입니다.");
        series.setAuthorName("임시사용자");
        series.setPostCount(1);
        series.setIsPublic("Y");
        sampleSeries.add(series);

        req.setAttribute("seriesList", sampleSeries);
        req.getRequestDispatcher("/WEB-INF/views/series/list.jsp").forward(req, resp);
    }

    private void handleDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: seriesService.getSeriesDetail(seriesId) 로 교체
        SeriesDto series = new SeriesDto();
        series.setSeriesId(201);
        series.setUserId(1);
        series.setName("샘플 시리즈");
        series.setDescription("시리즈 상세 화면 연결 확인용 샘플 설명입니다.");
        series.setAuthorName("임시사용자");
        series.setPostCount(1);
        series.setIsPublic("Y");
        series.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        PostDto post = new PostDto();
        post.setPostId(101);
        post.setUserId(1);
        post.setTitle("샘플 커뮤니티 게시글");
        post.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        List<PostDto> postList = new ArrayList<>();
        postList.add(post);
        series.setPostList(postList);

        req.setAttribute("series", series);
        req.setAttribute("postList", postList);
        req.getRequestDispatcher("/WEB-INF/views/series/detail.jsp").forward(req, resp);
    }

    private void handleWriteForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/series/write.jsp").forward(req, resp);
    }

    private void handleUpdateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: seriesService.getSeriesDetail(seriesId) + 본인 확인 후 교체
        SeriesDto series = new SeriesDto();
        series.setSeriesId(201);
        series.setUserId(1);
        series.setName("수정용 샘플 시리즈");
        series.setDescription("시리즈 수정 화면 연결 확인용 샘플 설명입니다.");
        series.setAuthorName("스터디로그");
        series.setIsPublic("Y");

        req.setAttribute("series", series);
        req.getRequestDispatcher("/WEB-INF/views/series/update.jsp").forward(req, resp);
    }

    private void handleWrite(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: seriesService.createSeries(loginUser.getUserId(), name, description, isPublic)
        resp.sendRedirect(req.getContextPath() + "/l_check/user/mypage.do");
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: seriesService.updateSeries(seriesId, name, description, isPublic) + 본인 확인
        resp.sendRedirect(req.getContextPath() + "/l_check/series/detail.do?id=" + req.getParameter("seriesId"));
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: seriesService.deleteSeries(seriesId) — 소속 게시글도 함께 삭제 확인 필요
        resp.sendRedirect(req.getContextPath() + "/l_check/user/mypage.do");
    }
}
