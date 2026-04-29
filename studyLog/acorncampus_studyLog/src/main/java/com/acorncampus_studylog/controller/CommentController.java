package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.service.CommentService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        // TODO: 세션 loginUser → postId, content 파싱
        //       → commentService.addComment → JSON 응답
    }

    /**
     * POST /l_check/comment/reply.do → 대댓글 등록 (2depth 제한)
     * 응답: {"status":"ok", "commentId":N} 또는 {"status":"error", "message":"..."}
     * 파라미터: postId, parentCommentId, content
     */
    private void handleReply(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: commentService.addReply (2depth 초과 시 서비스에서 예외 → error 응답)
    }

    /**
     * POST /l_check/comment/update.do → 댓글 수정
     * 응답: {"status":"ok"} 또는 {"status":"error"}
     * 파라미터: commentId, content
     */
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: 세션 userId → commentService의 findById로 작성자 확인 → commentService.updateComment
    }

    /**
     * POST /l_check/comment/delete.do → 댓글 소프트 삭제
     * 응답: {"status":"ok"} 또는 {"status":"error"}
     * 파라미터: commentId
     */
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: 세션 userId → 작성자 또는 ADMIN 확인 → commentService.deleteComment
    }
}
