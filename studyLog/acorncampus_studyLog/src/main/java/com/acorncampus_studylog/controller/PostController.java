package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.service.PostService;
import com.acorncampus_studylog.service.SeriesService;
import com.acorncampus_studylog.service.TagService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 게시글 컨트롤러
 * URL 패턴: /post/* (공개), /l_check/post/* (로그인 필요 — LoginCheckFilter 적용)
 *
 * GET  /post/list.do              → 게시글 목록
 * GET  /post/detail.do?id={id}   → 게시글 상세
 * GET  /l_check/post/write.do    → 작성 폼
 * POST /post/write.do            → 작성 처리
 * GET  /l_check/post/update.do?id={id} → 수정 폼
 * POST /post/update.do           → 수정 처리
 * POST /l_check/post/delete.do   → 삭제 처리
 * POST /post/upload.do           → 이미지 업로드 (AJAX, multipart)
 */
@WebServlet(urlPatterns = {"/post/*", "/l_check/post/*"})
public class PostController extends HttpServlet {

    private final PostService   postService   = new PostService();
    private final TagService    tagService    = new TagService();
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
            case "/upload.do": handleUpload(req, resp); break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // ── GET 핸들러 ──────────────────────────────────────────────────────────

    /**
     * GET /post/list.do?page={page} → 공개 게시글 목록 (페이지네이션)
     * forward: /WEB-INF/views/post/list.jsp
     */
    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: page 파라미터 파싱 → postService.getPostList(page) → setAttribute → forward
    }

    /**
     * GET /post/detail.do?id={postId} → 게시글 상세 (조회수 자동 증가)
     * forward: /WEB-INF/views/post/detail.jsp
     */
    private void handleDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: id 파라미터 → postService.getPostDetail(id) → 없으면 404
        //       → commentService.getCommentsByPost → setAttribute → forward
    }

    /**
     * GET /l_check/post/write.do → 게시글 작성 폼
     * forward: /WEB-INF/views/post/write.jsp
     */
    private void handleWriteForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 세션에서 userId → seriesService.getSeriesByUser(userId) → setAttribute("seriesList")
        //       → forward /WEB-INF/views/post/write.jsp
    }

    /**
     * GET /l_check/post/update.do?id={postId} → 게시글 수정 폼 (본인 또는 관리자만)
     * forward: /WEB-INF/views/post/update.jsp
     */
    private void handleUpdateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: id → postService.getPostDetail → 권한 확인(작성자 또는 ADMIN)
        //       → setAttribute("post") → forward
    }

    // ── POST 핸들러 ─────────────────────────────────────────────────────────

    /**
     * POST /post/write.do → 게시글 저장
     * 로그인 여부는 Controller에서 직접 세션 확인 (필터는 GET 폼에만 적용)
     * 성공: /post/detail.do?id={newId} 리다이렉트
     */
    private void handleWrite(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 세션 loginUser 확인 → 파라미터 파싱(title, content, isPublic, seriesId, tags)
        //       → postService.createPost → redirect /post/detail.do?id=N
        // 파라미터: title, content, isPublic, seriesId(optional), tags(콤마 구분 또는 배열)
    }

    /**
     * POST /post/update.do → 게시글 수정
     * 성공: /post/detail.do?id={postId} 리다이렉트
     */
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 세션 loginUser → postId → 권한 확인 → postService.updatePost → redirect
        // 파라미터: postId, title, content, isPublic, seriesId, tags
    }

    /**
     * POST /l_check/post/delete.do → 게시글 소프트 삭제
     * 성공: / 리다이렉트
     */
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 세션 loginUser → postId → 권한 확인(작성자 또는 ADMIN) → postService.deletePost → redirect
        // 파라미터: postId
    }

    /**
     * POST /post/upload.do → Toast UI Editor 이미지 업로드 (multipart/form-data)
     * 응답: {"url": "/resources/upload/xxx.jpg"} (Toast UI Editor 요구 형식)
     */
    private void handleUpload(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: commons-fileupload로 파일 파싱 → postService.saveUploadedImage
        //       → resp.setContentType("application/json") → {"url": "..."} 출력
    }
}
