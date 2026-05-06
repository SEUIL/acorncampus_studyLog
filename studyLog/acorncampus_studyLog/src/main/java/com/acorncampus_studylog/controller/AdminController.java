package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.PageDto;
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
        // TODO: 진희/철진/현겸 Service의 카운트 메서드가 완성되면 실제 호출로 교체
        req.setAttribute("totalUserCount", 0);
        req.setAttribute("todayUserCount", 0);
        req.setAttribute("totalPostCount", 0);
        req.setAttribute("todayPostCount", 0);
        req.setAttribute("pendingReportCount", 0);
        req.setAttribute("recentPostStats", "최근 작성 흐름");
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
        // 검색어와 페이지 번호를 기준으로 실제 회원 목록 조회
        String keyword = req.getParameter("keyword");
        int pageNo = parseInt(req.getParameter("page"), 1);
        PageDto page = userService.getUserPage(keyword, pageNo);

        req.setAttribute("userList", userService.getUserList(keyword, pageNo));
        req.setAttribute("page", page);
        // TODO: UserService 통계 메서드가 완성되면 실제 회원 상태 수로 교체
        req.setAttribute("totalUserCount", 0);
        req.setAttribute("activeUserCount", 0);
        req.setAttribute("bannedUserCount", 0);
        req.setAttribute("deletedUserCount", 0);
        req.getRequestDispatcher("/WEB-INF/views/admin/user/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/user/ban.do → 사용자 정지 / 파라미터: userId
     * redirect: /admin/user/list.do
     */
    private void handleUserBan(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 선택한 회원을 정지 상태로 변경
        userService.banUser(parseInt(req.getParameter("userId"), 0));
        resp.sendRedirect(req.getContextPath() + "/admin/user/list.do");
    }

    /**
     * POST /admin/user/unban.do → 사용자 정지 해제 / 파라미터: userId
     * redirect: /admin/user/list.do
     */
    private void handleUserUnban(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 선택한 회원의 정지 상태를 해제
        userService.unbanUser(parseInt(req.getParameter("userId"), 0));
        resp.sendRedirect(req.getContextPath() + "/admin/user/list.do");
    }

    /**
     * POST /admin/user/delete.do → 사용자 강제 삭제 / 파라미터: userId
     * redirect: /admin/user/list.do
     */
    private void handleUserDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 관리자 권한으로 회원을 강제 삭제
        userService.forceDeleteUser(parseInt(req.getParameter("userId"), 0));
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
        // 관리자 게시글 목록은 공개/비공개 글을 모두 포함
        int pageNo = parseInt(req.getParameter("page"), 1);
        req.setAttribute("postList", postService.getPostListForAdmin(pageNo));
        req.setAttribute("page", postService.getPostPageForAdmin(pageNo));
        req.getRequestDispatcher("/WEB-INF/views/admin/post/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/post/delete.do → 게시글 강제 삭제 / 파라미터: postId
     * redirect: /admin/post/list.do
     */
    private void handlePostDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 관리자 권한으로 게시글을 강제 삭제
        postService.forceDeletePost(parseInt(req.getParameter("postId"), 0));
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
        // 댓글 관리 화면용 전체 댓글 목록 조회
        int pageNo = parseInt(req.getParameter("page"), 1);
        req.setAttribute("commentList", commentService.getCommentListForAdmin(pageNo));
        req.setAttribute("page", commentService.getCommentPageForAdmin(pageNo));
        req.getRequestDispatcher("/WEB-INF/views/admin/comment/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/comment/delete.do → 댓글 강제 삭제 / 파라미터: commentId
     * redirect: /admin/comment/list.do
     */
    private void handleCommentDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 관리자 권한으로 댓글 삭제
        commentService.deleteComment(parseInt(req.getParameter("commentId"), 0));
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
        // 상태 필터가 있으면 해당 상태의 신고만 조회
        String status = normalizeReportStatus(req.getParameter("status"));
        int pageNo = parseInt(req.getParameter("page"), 1);
        req.setAttribute("reportList", reportService.getReportList(status, pageNo));
        req.setAttribute("page", reportService.getReportPage(status, pageNo));
        req.getRequestDispatcher("/WEB-INF/views/admin/report/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/report/resolve.do → 신고 처리 (RESOLVED) / 파라미터: reportId
     * redirect: /admin/report/list.do
     */
    private void handleReportResolve(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 신고 상태를 처리 완료로 변경
        reportService.resolveReport(parseInt(req.getParameter("reportId"), 0));
        resp.sendRedirect(req.getContextPath() + "/admin/report/list.do");
    }

    /**
     * POST /admin/report/dismiss.do → 신고 기각 (DISMISSED) / 파라미터: reportId
     * redirect: /admin/report/list.do
     */
    private void handleReportDismiss(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 신고 상태를 기각으로 변경
        reportService.dismissReport(parseInt(req.getParameter("reportId"), 0));
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
        // 전체 태그와 사용 빈도를 조회
        req.setAttribute("tagList", tagService.getAllTagsForAdmin());
        req.getRequestDispatcher("/WEB-INF/views/admin/tag/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/tag/delete.do → 태그 삭제 (연결된 post_tags CASCADE 삭제) / 파라미터: tagId
     * redirect: /admin/tag/list.do
     */
    private void handleTagDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 선택한 태그를 삭제
        tagService.deleteTag(parseInt(req.getParameter("tagId"), 0));
        resp.sendRedirect(req.getContextPath() + "/admin/tag/list.do");
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String normalizeReportStatus(String status) {
        if ("PENDING".equals(status) || "RESOLVED".equals(status) || "DISMISSED".equals(status)) {
            return status;
        }
        return null;
    }
}
