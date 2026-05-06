package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.PostDto;
import com.acorncampus_studylog.dto.ReportDto;
import com.acorncampus_studylog.dto.TagDto;
import com.acorncampus_studylog.dto.UserDto;
import com.acorncampus_studylog.service.CommentService;
import com.acorncampus_studylog.service.PostService;
import com.acorncampus_studylog.service.ReportService;
import com.acorncampus_studylog.service.TagService;
import com.acorncampus_studylog.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 관리자 컨트롤러 (ADMIN 권한 필수 — LoginCheckFilter + role 체크 적용)
 * URL 패턴: /admin/*
 *
 * GET  /admin/main.do              → 대시보드 (통계)
 *
 * GET  /admin/user/list.do         → 사용자 목록
 * POST /admin/user/ban.do          → 사용자 정지
 * POST /admin/user/unban.do        → 사용자 정지 해제
 * POST /admin/user/delete.do       → 사용자 강제 삭제
 *
 * GET  /admin/post/list.do         → 게시글 목록 (전체)
 * POST /admin/post/delete.do       → 게시글 강제 삭제
 *
 * GET  /admin/comment/list.do      → 댓글 목록 (전체)
 * POST /admin/comment/delete.do    → 댓글 강제 삭제
 *
 * GET  /admin/report/list.do       → 신고 목록
 * POST /admin/report/resolve.do    → 신고 처리 (RESOLVED)
 * POST /admin/report/dismiss.do    → 신고 기각 (DISMISSED)
 *
 * GET  /admin/tag/list.do          → 태그 목록
 * POST /admin/tag/delete.do        → 태그 삭제
 */
@WebServlet("/admin/*")
public class AdminController extends HttpServlet {

    private final UserService    userService    = new UserService();
    private final PostService    postService    = new PostService();
    private final CommentService commentService = new CommentService();
    private final ReportService  reportService  = new ReportService();
    private final TagService     tagService     = new TagService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/main.do";

        if ("/main.do".equals(path)) {
            handleDashboard(req, resp);
        } else if (path.startsWith("/user/")) {
            routeUserGet(path.substring(6), req, resp);
        } else if (path.startsWith("/post/")) {
            routePostGet(path.substring(6), req, resp);
        } else if (path.startsWith("/comment/")) {
            routeCommentGet(path.substring(9), req, resp);
        } else if (path.startsWith("/report/")) {
            routeReportGet(path.substring(8), req, resp);
        } else if (path.startsWith("/tag/")) {
            routeTagGet(path.substring(5), req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        if (path.startsWith("/user/")) {
            routeUserPost(path.substring(6), req, resp);
        } else if (path.startsWith("/post/")) {
            routePostPost(path.substring(6), req, resp);
        } else if (path.startsWith("/comment/")) {
            routeCommentPost(path.substring(9), req, resp);
        } else if (path.startsWith("/report/")) {
            routeReportPost(path.substring(8), req, resp);
        } else if (path.startsWith("/tag/")) {
            routeTagPost(path.substring(5), req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // ── 대시보드 ─────────────────────────────────────────────────────────────

    /**
     * GET /admin/main.do → 관리자 대시보드 (총 회원 수, 오늘 가입, 총 게시글, 대기 신고 수)
     * forward: /WEB-INF/views/admin/main.jsp
     */
    private void handleDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: 각 service 통계 호출로 교체
        req.setAttribute("totalUserCount", 12);
        req.setAttribute("todayUserCount", 1);
        req.setAttribute("totalPostCount", 34);
        req.setAttribute("todayPostCount", 2);
        req.setAttribute("pendingReportCount", 1);
        req.setAttribute("recentPostStats", "최근 작성 흐름 샘플");
        req.getRequestDispatcher("/WEB-INF/views/admin/main.jsp").forward(req, resp);
    }

    // ── 사용자 관리 ──────────────────────────────────────────────────────────

    private void routeUserGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (action) {
            case "list.do": handleUserList(req, resp); break;
            default: resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void routeUserPost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        switch (action) {
            case "ban.do":    handleUserBan(req, resp);    break;
            case "unban.do":  handleUserUnban(req, resp);  break;
            case "delete.do": handleUserDelete(req, resp); break;
            default: resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /admin/user/list.do?keyword={kw}&page={page} → 사용자 목록
     * forward: /WEB-INF/views/admin/user/list.jsp
     */
    private void handleUserList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: userService.getUserListForAdmin(keyword, page) 로 교체
        List<UserDto> users = new ArrayList<>();
        UserDto user = new UserDto();
        user.setUserId(1);
        user.setUsername("임시사용자");
        user.setEmail("temp@studylog.dev");
        user.setRole("ADMIN");
        user.setIsBanned("N");
        users.add(user);

        req.setAttribute("userList", users);
        req.setAttribute("totalUserCount", 12);
        req.setAttribute("activeUserCount", 11);
        req.setAttribute("bannedUserCount", 1);
        req.setAttribute("deletedUserCount", 0);
        req.getRequestDispatcher("/WEB-INF/views/admin/user/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/user/ban.do → 사용자 정지 / 파라미터: userId
     * redirect: /admin/user/list.do
     */
    private void handleUserBan(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: userService.banUser(Integer.parseInt(req.getParameter("userId")))
        resp.sendRedirect(req.getContextPath() + "/admin/user/list.do");
    }

    /**
     * POST /admin/user/unban.do → 사용자 정지 해제 / 파라미터: userId
     * redirect: /admin/user/list.do
     */
    private void handleUserUnban(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: userService.unbanUser(Integer.parseInt(req.getParameter("userId")))
        resp.sendRedirect(req.getContextPath() + "/admin/user/list.do");
    }

    /**
     * POST /admin/user/delete.do → 사용자 강제 삭제 / 파라미터: userId
     * redirect: /admin/user/list.do
     */
    private void handleUserDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: userService.forceDeleteUser(Integer.parseInt(req.getParameter("userId")))
        resp.sendRedirect(req.getContextPath() + "/admin/user/list.do");
    }

    // ── 게시글 관리 ──────────────────────────────────────────────────────────

    private void routePostGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (action) {
            case "list.do": handlePostList(req, resp); break;
            default: resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void routePostPost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        switch (action) {
            case "delete.do": handlePostDelete(req, resp); break;
            default: resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /admin/post/list.do?page={page} → 전체 게시글 목록 (비공개 포함)
     * forward: /WEB-INF/views/admin/post/list.jsp
     */
    private void handlePostList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: postService.getPostListForAdmin(page) 로 교체
        List<PostDto> posts = new ArrayList<>();
        PostDto post = new PostDto();
        post.setPostId(101);
        post.setTitle("임시 커뮤니티 게시글");
        post.setAuthorName("임시사용자");
        post.setSeriesName("임시 시리즈");
        post.setViewCount(42);
        post.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        post.setIsPublic("Y");
        posts.add(post);

        req.setAttribute("postList", posts);
        req.getRequestDispatcher("/WEB-INF/views/admin/post/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/post/delete.do → 게시글 강제 삭제 / 파라미터: postId
     * redirect: /admin/post/list.do
     */
    private void handlePostDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: postService.forceDeletePost(Integer.parseInt(req.getParameter("postId")))
        resp.sendRedirect(req.getContextPath() + "/admin/post/list.do");
    }

    // ── 댓글 관리 ────────────────────────────────────────────────────────────

    private void routeCommentGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (action) {
            case "list.do": handleCommentList(req, resp); break;
            default: resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void routeCommentPost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        switch (action) {
            case "delete.do": handleCommentDelete(req, resp); break;
            default: resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /admin/comment/list.do?page={page} → 전체 댓글 목록
     * forward: /WEB-INF/views/admin/comment/list.jsp
     */
    private void handleCommentList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: commentService.getCommentListForAdmin(page) 로 교체
        req.setAttribute("commentList", Collections.emptyList());
        req.getRequestDispatcher("/WEB-INF/views/admin/comment/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/comment/delete.do → 댓글 강제 삭제 / 파라미터: commentId
     * redirect: /admin/comment/list.do
     */
    private void handleCommentDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: commentService.forceDeleteComment(Integer.parseInt(req.getParameter("commentId")))
        resp.sendRedirect(req.getContextPath() + "/admin/comment/list.do");
    }

    // ── 신고 관리 ────────────────────────────────────────────────────────────

    private void routeReportGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (action) {
            case "list.do": handleReportList(req, resp); break;
            default: resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void routeReportPost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        switch (action) {
            case "resolve.do": handleReportResolve(req, resp); break;
            case "dismiss.do": handleReportDismiss(req, resp); break;
            default: resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /admin/report/list.do?status={status}&page={page} → 신고 목록
     * forward: /WEB-INF/views/admin/report/list.jsp
     */
    private void handleReportList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: reportService.getReportList(status, page) 로 교체
        List<ReportDto> reports = new ArrayList<>();
        ReportDto report = new ReportDto();
        report.setReportId(301);
        report.setTargetType("POST");
        report.setTargetSummary("임시 커뮤니티 게시글");
        report.setReporterName("테스트신고자");
        report.setStatus("PENDING");
        report.setReason("화면 연결 확인용 임시 신고 데이터");
        report.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        reports.add(report);

        req.setAttribute("reportList", reports);
        req.getRequestDispatcher("/WEB-INF/views/admin/report/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/report/resolve.do → 신고 처리 (RESOLVED) / 파라미터: reportId
     * redirect: /admin/report/list.do
     */
    private void handleReportResolve(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: reportService.resolveReport(Integer.parseInt(req.getParameter("reportId")))
        // 대상 컨텐츠 삭제가 필요하면 postService/commentService도 호출
        resp.sendRedirect(req.getContextPath() + "/admin/report/list.do");
    }

    /**
     * POST /admin/report/dismiss.do → 신고 기각 (DISMISSED) / 파라미터: reportId
     * redirect: /admin/report/list.do
     */
    private void handleReportDismiss(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: reportService.dismissReport(Integer.parseInt(req.getParameter("reportId")))
        resp.sendRedirect(req.getContextPath() + "/admin/report/list.do");
    }

    // ── 태그 관리 ────────────────────────────────────────────────────────────

    private void routeTagGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (action) {
            case "list.do": handleTagList(req, resp); break;
            default: resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void routeTagPost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        switch (action) {
            case "delete.do": handleTagDelete(req, resp); break;
            default: resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /admin/tag/list.do → 전체 태그 목록 (사용 빈도 포함)
     * forward: /WEB-INF/views/admin/tag/list.jsp
     */
    private void handleTagList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: tagService.getTagListForAdmin() 로 교체
        List<TagDto> tags = new ArrayList<>();
        TagDto tag = new TagDto();
        tag.setTagId(401);
        tag.setName("java");
        tag.setPostCount(5);
        tags.add(tag);

        req.setAttribute("tagList", tags);
        req.getRequestDispatcher("/WEB-INF/views/admin/tag/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/tag/delete.do → 태그 삭제 (연결된 post_tags CASCADE 삭제) / 파라미터: tagId
     * redirect: /admin/tag/list.do
     */
    private void handleTagDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // TODO: tagService.deleteTag(Integer.parseInt(req.getParameter("tagId")))
        resp.sendRedirect(req.getContextPath() + "/admin/tag/list.do");
    }
}
