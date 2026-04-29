package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.service.PostService;
import com.acorncampus_studylog.service.TagService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 메인 페이지 — 최신 공개 게시글 + 인기 태그 표시 */
@WebServlet("/")
public class MainController extends HttpServlet {

    private final PostService postService = new PostService();
    private final TagService  tagService  = new TagService();

    /**
     * GET / → 메인 페이지
     * - 최신 공개 게시글 목록 (page=1, size=10)
     * - 태그 클라우드 (상위 N개)
     * forward: /WEB-INF/views/main.jsp
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: postService.getPostList(1) → req.setAttribute("posts", ...)
        // TODO: tagService.getTagCloud()   → req.setAttribute("tags", ...)
        // TODO: req.getRequestDispatcher("/WEB-INF/views/main.jsp").forward(req, resp)
    }
}
