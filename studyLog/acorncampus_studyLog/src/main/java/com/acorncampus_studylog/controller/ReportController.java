package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.service.ReportService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 신고 컨트롤러 (로그인 필수 — LoginCheckFilter 적용)
 * URL 패턴: /l_check/report/*
 * 모든 요청은 AJAX (JSON 응답)
 *
 * POST /l_check/report/post.do    → 게시글 신고
 * POST /l_check/report/comment.do → 댓글 신고
 */
@WebServlet("/l_check/report/*")
public class ReportController extends HttpServlet {

    private final ReportService reportService = new ReportService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        switch (path) {
            case "/post.do":    handleReportPost(req, resp);    break;
            case "/comment.do": handleReportComment(req, resp); break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * POST /l_check/report/post.do → 게시글 신고
     * 응답: {"status":"ok"} 또는 {"status":"error","message":"이미 신고한 게시글입니다."}
     * 파라미터: postId, reason
     */
    private void handleReportPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: 세션 userId → postId, reason → reportService.reportPost → JSON 응답
        // resp.setContentType("application/json; charset=UTF-8")
    }

    /**
     * POST /l_check/report/comment.do → 댓글 신고
     * 응답: {"status":"ok"} 또는 {"status":"error","message":"이미 신고한 댓글입니다."}
     * 파라미터: commentId, reason
     */
    private void handleReportComment(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: 세션 userId → commentId, reason → reportService.reportComment → JSON 응답
    }
}
