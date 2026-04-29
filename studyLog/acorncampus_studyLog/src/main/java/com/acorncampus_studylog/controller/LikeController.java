package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.service.LikeService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 좋아요/싫어요 컨트롤러 (로그인 필수 — LoginCheckFilter 적용)
 * URL 패턴: /l_check/like/*
 * 모든 요청은 AJAX (JSON 응답)
 *
 * POST /l_check/like/post.do    → 게시글 좋아요/싫어요 토글
 * POST /l_check/like/comment.do → 댓글 좋아요/싫어요 토글
 */
@WebServlet("/l_check/like/*")
public class LikeController extends HttpServlet {

    private final LikeService likeService = new LikeService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        switch (path) {
            case "/post.do":    handlePostLike(req, resp);    break;
            case "/comment.do": handleCommentLike(req, resp); break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * POST /l_check/like/post.do → 게시글 좋아요/싫어요 토글
     * 응답: {"status":"ok","likeCount":N,"dislikeCount":N,"myLike":"L"/"D"/null}
     * 파라미터: postId, likeType ("L" 또는 "D")
     */
    private void handlePostLike(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: 세션 userId → postId, likeType 파싱
        //       → likeService.togglePostLike → JSON 응답 출력
        // resp.setContentType("application/json; charset=UTF-8")
    }

    /**
     * POST /l_check/like/comment.do → 댓글 좋아요/싫어요 토글
     * 응답: {"status":"ok","likeCount":N,"dislikeCount":N,"myLike":"L"/"D"/null}
     * 파라미터: commentId, likeType ("L" 또는 "D")
     */
    private void handleCommentLike(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: 세션 userId → commentId, likeType 파싱
        //       → likeService.toggleCommentLike → JSON 응답 출력
    }
}
