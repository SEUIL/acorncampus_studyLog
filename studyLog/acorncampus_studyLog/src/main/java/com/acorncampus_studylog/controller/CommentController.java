package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.CommentDto;
import com.acorncampus_studylog.dto.UserDto;
import com.acorncampus_studylog.service.CommentService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 댓글/대댓글 컨트롤러 (로그인 필수 — LoginCheckFilter 적용)
 * URL 패턴: /l_check/comment/*
 * 모든 요청은 AJAX (JSON 응답)
 *
 * POST /l_check/comment/write.do  → 댓글 등록
 * POST /l_check/comment/reply.do  → 대댓글 등록
 * POST /l_check/comment/update.do → 댓글 수정
 * POST /l_check/comment/delete.do → 댓글 삭제
 */
@WebServlet("/l_check/comment/*")
public class CommentController extends HttpServlet {

    private final CommentService commentService = new CommentService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        switch (path) {
            case "/write.do":  handleWrite(req, resp);  break;
            case "/reply.do":  handleReply(req, resp);  break;
            case "/update.do": handleUpdate(req, resp); break;
            case "/delete.do": handleDelete(req, resp); break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * POST /l_check/comment/write.do → 댓글 등록
     * 응답: {"status":"ok", "commentId":N} 또는 {"status":"error", "message":"..."}
     * 파라미터: postId, content
     */
    private void handleWrite(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 필터를 통과했으므로 안전하게 세션에서 사용자 정보 획득[cite: 3]
            UserDto loginUser = (UserDto) req.getSession().getAttribute("loginUser");
            int userId = loginUser.getUserId(); // UserDto에 getUserId()가 있다고 가정

            int postId = Integer.parseInt(req.getParameter("postId"));
            String content = req.getParameter("content");

            // 이전에 분리한 최상위 댓글 전용 메서드 사용
            int commentId = commentService.addComment(postId, userId, content);

            out.print("{\"status\":\"ok\", \"commentId\":" + commentId + "}");
        } catch (Exception e) {
            e.printStackTrace();
            // 에러 메시지 내 쌍따옴표 이스케이프 처리
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "알 수 없는 오류";
            out.print("{\"status\":\"error\", \"message\":\"" + msg + "\"}");
        }

    }

    /**
     * POST /l_check/comment/reply.do → 대댓글 등록 (2depth 제한)
     * 응답: {"status":"ok", "commentId":N} 또는 {"status":"error", "message":"..."}
     * 파라미터: postId, parentCommentId, content
     */
    private void handleReply(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            UserDto loginUser = (UserDto) req.getSession().getAttribute("loginUser");
            int userId = loginUser.getUserId();

            int postId = Integer.parseInt(req.getParameter("postId"));
            int parentCommentId = Integer.parseInt(req.getParameter("parentCommentId"));
            String content = req.getParameter("content");

            // 이전에 분리한 답글 전용 메서드 사용
            int commentId = commentService.addReply(postId, userId, parentCommentId, content);

            out.print("{\"status\":\"ok\", \"commentId\":" + commentId + "}");
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "알 수 없는 오류";
            out.print("{\"status\":\"error\", \"message\":\"" + msg + "\"}");
        }
    }

    /**
     * POST /l_check/comment/update.do → 댓글 수정
     * 응답: {"status":"ok"} 또는 {"status":"error"}
     * 파라미터: commentId, content
     */
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            UserDto loginUser = (UserDto) req.getSession().getAttribute("loginUser");
            int userId = loginUser.getUserId();

            int commentId = Integer.parseInt(req.getParameter("commentId"));
            String content = req.getParameter("content");

            // 1. 수정 권한 확인
            CommentDto comment = commentService.findById(commentId);
            if (comment == null || comment.getUserId() != userId) {
                out.print("{\"status\":\"error\", \"message\":\"수정 권한이 없거나 존재하지 않는 댓글입니다.\"}");
                return;
            }

            // 2. 수정 진행 (Service 계층 메서드 호출)
            commentService.updateComment(commentId, content);
            out.print("{\"status\":\"ok\"}");
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"수정 중 오류가 발생했습니다.\"}");
        }
    }

    /**
     * POST /l_check/comment/delete.do → 댓글 소프트 삭제
     * 응답: {"status":"ok"} 또는 {"status":"error"}
     * 파라미터: commentId
     */
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            UserDto loginUser = (UserDto) req.getSession().getAttribute("loginUser");
            int userId = loginUser.getUserId();
            String userRole = loginUser.getRole(); // "ADMIN" 권한 체크용[cite: 3]

            int commentId = Integer.parseInt(req.getParameter("commentId"));

            // 1. 삭제 권한 확인 (작성자 본인이거나 ADMIN일 경우 허용)[cite: 3]
            CommentDto comment = commentService.findById(commentId);
            if (comment == null) {
                out.print("{\"status\":\"error\", \"message\":\"존재하지 않는 댓글입니다.\"}");
                return;
            }

            if (comment.getUserId() != userId && !"ADMIN".equals(userRole)) {
                out.print("{\"status\":\"error\", \"message\":\"삭제 권한이 없습니다.\"}");
                return;
            }

            // 2. 삭제 진행 (소프트 삭제)
            commentService.deleteComment(commentId);
            out.print("{\"status\":\"ok\"}");
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"삭제 중 오류가 발생했습니다.\"}");
        }
    }
}
