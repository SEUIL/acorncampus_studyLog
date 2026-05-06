package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.UserDto;
import com.acorncampus_studylog.service.ReportService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

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
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 1. 세션에서 로그인한 사용자(신고자) 정보 가져오기
            UserDto loginUser = (UserDto) req.getSession().getAttribute("loginUser");
            int reporterId = loginUser.getUserId();

            // 2. 파라미터 파싱
            int postId = Integer.parseInt(req.getParameter("postId"));
            String reason = req.getParameter("reason");

            // 3. 서비스 호출
            Map<String, Object> result = reportService.reportPost(reporterId, postId, reason);

            // 4. 결과에 따라 JSON 조립 및 출력
            String status = (String) result.get("status");
            if ("ok".equals(status)) {
                out.print("{\"status\":\"ok\"}");
            } else {
                String message = (String) result.get("message");
                out.print("{\"status\":\"error\", \"message\":\"" + message + "\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "서버 오류가 발생했습니다.";
            out.print("{\"status\":\"error\", \"message\":\"" + msg + "\"}");
        }
    }

    /**
     * POST /l_check/report/comment.do → 댓글 신고
     * 응답: {"status":"ok"} 또는 {"status":"error","message":"이미 신고한 댓글입니다."}
     * 파라미터: commentId, reason
     */
    private void handleReportComment(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 1. 세션에서 로그인한 사용자(신고자) 정보 가져오기
            UserDto loginUser = (UserDto) req.getSession().getAttribute("loginUser");
            int reporterId = loginUser.getUserId();

            // 2. 파라미터 파싱
            int commentId = Integer.parseInt(req.getParameter("commentId"));
            String reason = req.getParameter("reason");

            // 3. 서비스 호출
            Map<String, Object> result = reportService.reportComment(reporterId, commentId, reason);

            // 4. 결과에 따라 JSON 조립 및 출력
            String status = (String) result.get("status");
            if ("ok".equals(status)) {
                out.print("{\"status\":\"ok\"}");
            } else {
                String message = (String) result.get("message");
                out.print("{\"status\":\"error\", \"message\":\"" + message + "\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "서버 오류가 발생했습니다.";
            out.print("{\"status\":\"error\", \"message\":\"" + msg + "\"}");
        }
    }
}
