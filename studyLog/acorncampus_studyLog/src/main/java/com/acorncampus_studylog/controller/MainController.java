package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.PostDto;
import com.acorncampus_studylog.dto.SeriesDto;
import com.acorncampus_studylog.service.PostService;
import com.acorncampus_studylog.service.TagService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// "/community.do" 추가 이유: web.xml welcome-file이 index.jsp를 가로채므로 "/" 단독으로는 도달 불가
@WebServlet(urlPatterns = {"/", "/community.do"})
public class MainController extends HttpServlet {

    private final PostService postService = new PostService();
    private final TagService tagService = new TagService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: postService.getPopularPosts(limit) 로 교체
        List<PostDto> popularPosts = new ArrayList<>();
        PostDto post = new PostDto();
        post.setPostId(101);
        post.setUserId(1);
        post.setTitle("샘플 커뮤니티 게시글");
        post.setAuthorName("스터디로그");
        post.setLikeCount(12);
        post.setCommentCount(3);
        post.setViewCount(128);
        post.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        popularPosts.add(post);

        // TODO: seriesService.getPopularSeries(limit) 로 교체
        List<SeriesDto> popularSeries = new ArrayList<>();
        SeriesDto series = new SeriesDto();
        series.setSeriesId(201);
        series.setUserId(1);
        series.setName("샘플 시리즈");
        series.setDescription("커뮤니티 메인 연결 확인용 시리즈입니다.");
        series.setAuthorName("스터디로그");
        series.setPostCount(4);
        popularSeries.add(series);

        req.setAttribute("popularPosts", popularPosts);
        req.setAttribute("popularSeries", popularSeries);
        req.getRequestDispatcher("/WEB-INF/views/main.jsp").forward(req, resp);
    }
}
