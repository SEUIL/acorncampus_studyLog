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

/**
 * 시리즈 컨트롤러
 * URL 패턴: /series/* (공개), /l_check/series/* (로그인 필요)
 *
 * GET  /series/list.do               → 공개 시리즈 목록
 * GET  /series/detail.do?id={id}     → 시리즈 상세 (소속 게시글 TOC)
 * GET  /l_check/series/write.do      → 시리즈 생성 폼
 * POST /series/write.do              → 시리즈 생성 처리
 * GET  /l_check/series/update.do?id={id} → 시리즈 수정 폼
 * POST /series/update.do             → 시리즈 수정 처리
 * POST /l_check/series/delete.do     → 시리즈 삭제
 */
@WebServlet(urlPatterns = {"/series/*", "/l_check/series/*"})
public class SeriesController extends HttpServlet {

    private final SeriesService seriesService = new SeriesService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/list.do";

        switch (path) {
            case "/list.do":   handleList(req, resp);       break;
            case "/detail.do": handleDetail(req, resp);     break;
            case "/write.do":  handleWriteForm(req, resp);  break;
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
            case "/write.do":  handleWrite(req, resp);  break;
            case "/update.do": handleUpdate(req, resp); break;
            case "/delete.do": handleDelete(req, resp); break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /series/list.do?page={page} → 공개 시리즈 목록
     * forward: /WEB-INF/views/series/list.jsp
     */
    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<SeriesDto> sampleSeries = new ArrayList<>();
        SeriesDto series = new SeriesDto();
        series.setSeriesId(201);
        series.setUserId(1);
        series.setName("임시 시리즈");
        series.setDescription("시리즈 화면 연결 확인을 위한 임시 설명입니다.");
        series.setAuthorName("임시사용자");
        series.setPostCount(1);
        series.setIsPublic("Y");
        sampleSeries.add(series);

        req.setAttribute("seriesList", sampleSeries);
        req.getRequestDispatcher("/WEB-INF/views/series/list.jsp").forward(req, resp);
    }

    /**
     * GET /series/detail.do?id={seriesId} → 시리즈 상세 (게시글 TOC 포함)
     * forward: /WEB-INF/views/series/detail.jsp
     */
    private void handleDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        SeriesDto series = new SeriesDto();
        series.setSeriesId(201);
        series.setUserId(1);
        series.setName("임시 시리즈");
        series.setDescription("시리즈 상세 화면 연결 확인을 위한 임시 설명입니다.");
        series.setAuthorName("임시사용자");
        series.setPostCount(1);
        series.setIsPublic("Y");
        series.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        PostDto post = new PostDto();
        post.setPostId(101);
        post.setUserId(1);
        post.setTitle("임시 커뮤니티 게시글");
        post.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        List<PostDto> postList = new ArrayList<>();
        postList.add(post);
        series.setPostList(postList);

        req.setAttribute("series", series);
        req.setAttribute("postList", postList);
        req.getRequestDispatcher("/WEB-INF/views/series/detail.jsp").forward(req, resp);
    }

    /**
     * GET /l_check/series/write.do → 시리즈 생성 폼
     * forward: /WEB-INF/views/series/write.jsp
     */
    private void handleWriteForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: forward /WEB-INF/views/series/write.jsp
    }

    /**
     * GET /l_check/series/update.do?id={seriesId} → 수정 폼 (본인만)
     * forward: /WEB-INF/views/series/update.jsp
     */
    private void handleUpdateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 세션 userId → seriesService.getSeriesDetail → 본인 확인 → forward
    }

    /**
     * POST /series/write.do → 시리즈 생성
     * 성공: /series/detail.do?id={newId} 리다이렉트
     * 파라미터: name, description, isPublic
     */
    private void handleWrite(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: 세션 로그인 확인 → seriesService.createSeries → redirect
    }

    /**
     * POST /series/update.do → 시리즈 수정
     * 성공: /series/detail.do?id={seriesId} 리다이렉트
     * 파라미터: seriesId, name, description, isPublic
     */
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: 세션 userId → 본인 확인 → seriesService.updateSeries → redirect
    }

    /**
     * POST /l_check/series/delete.do → 시리즈 삭제
     * 성공: /series/list.do 리다이렉트
     * 파라미터: seriesId
     */
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: 세션 userId → 본인 확인 → seriesService.deleteSeries → redirect
    }
}
