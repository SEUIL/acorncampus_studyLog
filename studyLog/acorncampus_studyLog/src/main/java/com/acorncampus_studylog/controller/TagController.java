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
        // TagService.getTagCloud()가 post_count 내림차순으로 정렬된 전체 태그 목록을 반환
        // JSP에서 post_count 값을 이용해 태그 크기나 색상을 다르게 표현할 수 있음
        req.setAttribute("tags", tagService.getTagCloud());
        req.getRequestDispatcher("/WEB-INF/views/tag/list.jsp").forward(req, resp);
    }

    /**
     * GET /tag/post.do?tag={tagName} → search.do?q=#{tagName} 으로 리다이렉트
     *
     * 게시글 상세의 태그 칩 클릭 시 이 URL로 진입하는데,
     * 태그 검색 결과를 search/result.jsp 하나로 통일하기 위해 SearchController에 위임
     * SearchController에서 '#' 감지 → TagService 호출 → result.jsp forward 처리
     */
    private void handleTagPosts(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String tagName = req.getParameter("tag");

        // 태그 이름 파라미터가 없으면 태그 클라우드 목록으로 리다이렉트
        if (tagName == null || tagName.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/tag/list.do");
            return;
        }

        // search.do?q=#태그명 으로 리다이렉트 → SearchController가 태그 검색으로 처리
        // URL 인코딩: '#'은 %23, 한글 태그명도 인코딩해서 전달
        String encoded = java.net.URLEncoder.encode("#" + tagName.trim(), "UTF-8");
        resp.sendRedirect(req.getContextPath() + "/search.do?q=" + encoded);
    }
}
