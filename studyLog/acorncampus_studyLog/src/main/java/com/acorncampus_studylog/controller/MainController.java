package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.PostDto;
import com.acorncampus_studylog.dto.SeriesDto;
import com.acorncampus_studylog.dto.TagDto;
import com.acorncampus_studylog.service.PostService;
import com.acorncampus_studylog.service.SeriesService;
import com.acorncampus_studylog.service.TagService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

// urlPatterns에 "/community.do"를 함께 등록한 이유:
// web.xml의 welcome-file-list에 index.jsp가 등록되어 있어서
// URL이 "/" 단독으로 들어오면 Tomcat이 Servlet보다 index.jsp를 먼저 처리해버림
// → 커뮤니티 메인은 "/community.do"로 접근하고, 로그인 화면은 index.jsp가 담당
@WebServlet(urlPatterns = {"/", "/community.do"})
public class MainController extends HttpServlet {

    private final PostService   postService   = new PostService();
    private final SeriesService seriesService = new SeriesService();
    private final TagService    tagService    = new TagService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 인기 게시글: 최신 글 1페이지(10개) 중 앞 4개만 메인에 표시
        // 나중에 likes 기준 정렬이 필요하면 postService에 getPopularPosts() 메서드를 추가하면 됨
        List<PostDto> popularPosts = postService.getPostList(1);
        if (popularPosts != null && popularPosts.size() > 4) {
            popularPosts = popularPosts.subList(0, 4);
        }

        // 인기 시리즈: SeriesService가 아직 TODO라서 null 반환 가능 → 빈 리스트로 방어
        // JSP의 ${popularSeries} 변수명과 반드시 일치해야 함
        List<SeriesDto> popularSeries = seriesService.getSeriesList(1);
        if (popularSeries == null) popularSeries = java.util.Collections.emptyList();
        if (popularSeries.size() > 5) {
            popularSeries = popularSeries.subList(0, 5);
        }

        req.setAttribute("popularPosts",  popularPosts);
        req.setAttribute("popularSeries", popularSeries);
        req.getRequestDispatcher("/WEB-INF/views/main.jsp").forward(req, resp);
    }
}
