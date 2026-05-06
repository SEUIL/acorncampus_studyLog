package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.service.PostService;
import com.acorncampus_studylog.service.TagService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 검색 컨트롤러 — 키워드 검색 + 해시태그 태그 검색을 result.jsp 하나로 통합 처리
 * URL 패턴: /search.do
 *
 * GET /search.do?q={keyword}&page={page}   → 키워드 검색
 * GET /search.do?q=%23{tagName}&page={page} → 태그 검색 (#태그명 형식)
 */
@WebServlet("/search.do")
public class SearchController extends HttpServlet {

    private final PostService postService = new PostService();
    // TagService를 SearchController에서 직접 사용: #태그 검색 결과도 result.jsp 하나로 통일하기 위해
    // TagController로 redirect하면 별도 JSP로 분기되므로 여기서 직접 호출해 forward로 처리
    private final TagService tagService = new TagService();

    /**
     * GET /search.do?q={keyword}&page={page} → 검색 결과 페이지
     * forward: /WEB-INF/views/search/result.jsp
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("q");

        // 키워드가 없거나 공백만 있으면 커뮤니티 메인으로 리다이렉트
        // 빈 검색어로 DB 검색이 실행되면 전체 게시글이 나와서 성능 낭비가 생기기 때문에 막음
        if (keyword == null || keyword.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/community.do");
            return;
        }

        // trim()으로 앞뒤 공백 제거: "  Java  " → "Java"
        keyword = keyword.trim();

        // page 파라미터 없거나 잘못된 값이면 1페이지 처리
        int pageNo;
        try {
            pageNo = Integer.parseInt(req.getParameter("page"));
            if (pageNo < 1) pageNo = 1;
        } catch (Exception e) {
            pageNo = 1;
        }

        if (keyword.startsWith("#")) {
            // ── 태그 검색 분기 ──────────────────────────────────────────────
            // "#java" → tagName = "java"
            String tagName = keyword.substring(1).trim();

            if (tagName.isEmpty()) {
                // "#" 만 입력했으면 태그 클라우드 목록으로 이동
                resp.sendRedirect(req.getContextPath() + "/tag/list.do");
                return;
            }

            // TagService를 직접 호출해서 result.jsp로 forward
            // → 페이지네이션 링크가 search.do?q=%23java&page=N 형태로 생성되므로
            //   다음 페이지 클릭 시에도 다시 이 분기를 타서 태그 검색이 유지됨
            req.setAttribute("posts",   tagService.getTaggedPosts(tagName, pageNo));
            req.setAttribute("page",    tagService.getTaggedPostPage(tagName, pageNo));
            req.setAttribute("keyword", keyword); // JSP 타이틀에 "#java 검색 결과"로 표시됨

        } else {
            // ── 키워드 검색 분기 ────────────────────────────────────────────
            // JSP에서 필요한 세 가지 데이터를 모두 전달:
            // posts   → 검색 결과 게시글 목록
            // page    → 페이지 버튼 렌더링용 PageDto
            // keyword → JSP 상단 "'{keyword}' 검색 결과" 타이틀 + 페이지 링크 재조합용
            req.setAttribute("posts",   postService.search(keyword, pageNo));
            req.setAttribute("page",    postService.getSearchPage(keyword, pageNo));
            req.setAttribute("keyword", keyword);
        }

        // 태그 검색이든 키워드 검색이든 동일한 result.jsp로 forward
        req.getRequestDispatcher("/WEB-INF/views/search/result.jsp").forward(req, resp);
    }
}
