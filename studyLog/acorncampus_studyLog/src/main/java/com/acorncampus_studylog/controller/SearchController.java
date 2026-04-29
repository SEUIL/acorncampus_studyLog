package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.service.PostService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 검색 컨트롤러 — 게시글 제목 + 본문 키워드 검색
 * URL 패턴: /search.do
 *
 * GET /search.do?q={keyword}&page={page}
 */
@WebServlet("/search.do")
public class SearchController extends HttpServlet {

    private final PostService postService = new PostService();

    /**
     * GET /search.do?q={keyword}&page={page} → 검색 결과 페이지
     * forward: /WEB-INF/views/search/result.jsp
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: q, page 파라미터 파싱 → q가 비어있으면 / 리다이렉트
        //       → postService.search(keyword, page) → getAttribute("posts", "page", "keyword")
        //       → forward /WEB-INF/views/search/result.jsp
    }
}
