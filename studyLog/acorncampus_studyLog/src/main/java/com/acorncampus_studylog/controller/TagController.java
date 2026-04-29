package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.service.TagService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 태그 컨트롤러
 * URL 패턴: /tag/*
 *
 * GET /tag/list.do            → 태그 클라우드 전체 목록
 * GET /tag/post.do?tag={name} → 특정 태그의 게시글 목록
 */
@WebServlet("/tag/*")
public class TagController extends HttpServlet {

    private final TagService tagService = new TagService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/list.do";

        switch (path) {
            case "/list.do": handleTagCloud(req, resp); break;
            case "/post.do": handleTagPosts(req, resp); break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /tag/list.do → 태그 클라우드 페이지
     * forward: /WEB-INF/views/tag/list.jsp
     */
    private void handleTagCloud(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: tagService.getTagCloud() → setAttribute("tags") → forward
    }

    /**
     * GET /tag/post.do?tag={tagName}&page={page} → 태그별 게시글 목록
     * forward: /WEB-INF/views/tag/posts.jsp
     */
    private void handleTagPosts(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: tag, page 파라미터 → tagService.getTaggedPosts, getTaggedPostPage
        //       → setAttribute("posts", "page", "tagName") → forward
    }
}
