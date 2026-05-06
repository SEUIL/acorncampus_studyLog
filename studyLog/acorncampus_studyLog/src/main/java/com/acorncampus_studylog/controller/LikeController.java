package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.UserDto;
import com.acorncampus_studylog.service.LikeService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

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
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 1. 세션에서 로그인한 사용자 정보 가져오기[cite: 3]
            UserDto loginUser = (UserDto) req.getSession().getAttribute("loginUser");
            int userId = loginUser.getUserId();

            // 2. 파라미터 파싱
            int postId = Integer.parseInt(req.getParameter("postId"));
            String likeType = req.getParameter("likeType"); // "L" 또는 "D"

            // 3. 서비스 호출하여 결과 Map 받기
            Map<String, Object> result = likeService.togglePostLike(postId, userId, likeType);

            // 4. Map 데이터를 JSON 문자열로 변환 (myLike가 null일 경우의 처리 포함)
            String myLike = (String) result.get("myLike");
            String myLikeJson = (myLike == null) ? "null" : "\"" + myLike + "\"";

            String jsonResponse = String.format(
                    "{\"status\":\"%s\", \"likeCount\":%d, \"dislikeCount\":%d, \"myLike\":%s}",
                    result.get("status"),
                    result.get("likeCount"),
                    result.get("dislikeCount"),
                    myLikeJson
            );

            // 5. 응답 출력
            out.print(jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "서버 오류";
            out.print("{\"status\":\"error\", \"message\":\"" + msg + "\"}");
        }
    }

    /**
     * POST /l_check/like/comment.do → 댓글 좋아요/싫어요 토글
     * 응답: {"status":"ok","likeCount":N,"dislikeCount":N,"myLike":"L"/"D"/null}
     * 파라미터: commentId, likeType ("L" 또는 "D")
     */
    private void handleCommentLike(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 1. 세션에서 로그인한 사용자 정보 가져오기[cite: 3]
            UserDto loginUser = (UserDto) req.getSession().getAttribute("loginUser");
            int userId = loginUser.getUserId();

            // 2. 파라미터 파싱
            int commentId = Integer.parseInt(req.getParameter("commentId"));
            String likeType = req.getParameter("likeType"); // "L" 또는 "D"

            // 3. 서비스 호출하여 결과 Map 받기
            Map<String, Object> result = likeService.toggleCommentLike(commentId, userId, likeType);

            // 4. Map 데이터를 JSON 문자열로 변환
            String myLike = (String) result.get("myLike");
            String myLikeJson = (myLike == null) ? "null" : "\"" + myLike + "\"";

            String jsonResponse = String.format(
                    "{\"status\":\"%s\", \"likeCount\":%d, \"dislikeCount\":%d, \"myLike\":%s}",
                    result.get("status"),
                    result.get("likeCount"),
                    result.get("dislikeCount"),
                    myLikeJson
            );

            // 5. 응답 출력
            out.print(jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "서버 오류";
            out.print("{\"status\":\"error\", \"message\":\"" + msg + "\"}");
        }
    }
}
