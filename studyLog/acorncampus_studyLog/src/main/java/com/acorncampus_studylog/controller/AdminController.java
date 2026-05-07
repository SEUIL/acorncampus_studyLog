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
 * 관리자 화면의 요청을 한 곳에서 분기하는 컨트롤러.
 * /admin/* 아래로 들어오는 URL을 메뉴별 Service로 연결하고,
 * JSP에서 필요한 목록/페이지/통계 데이터를 request attribute로 넘긴다.
 *
 * 권한 체크는 LoginCheckFilter와 role 체크에서 처리하는 전제로 작성한다.
 */
@WebServlet("/admin/*")
public class AdminController extends HttpServlet {

    private final UserService userService = new UserService();
    private final PostService postService = new PostService();
    private final CommentService commentService = new CommentService();
    private final ReportService reportService = new ReportService();
    private final TagService tagService = new TagService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            path = "/main.do";
        }

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
        if (path == null) {
            path = "";
        }

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

    /**
     * GET /admin/main.do
     * 관리자 메인 화면에 필요한 요약 통계를 조회한다.
     * 현재 Service에 있는 페이지 정보의 totalCount를 활용해서 전체 수를 표시한다.
     * 오늘 가입/오늘 게시글처럼 날짜 조건이 필요한 값은 담당 Service 메서드가 추가되면 교체한다.
     *
     * forward: /WEB-INF/views/admin/main.jsp
     */
    private void handleDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        PageDto userPage = userService.getUserPage(null, 1);
        PageDto postPage = postService.getPostPageForAdmin(1);
        PageDto pendingReportPage = reportService.getReportPage("PENDING", 1);
        req.setAttribute("totalUserCount", userPage.getTotalCount());
        req.setAttribute("todayUserCount", 0);
        req.setAttribute("totalPostCount", postPage.getTotalCount());
        req.setAttribute("todayPostCount", 0);
        req.setAttribute("pendingReportCount", pendingReportPage.getTotalCount());
        // TODO: 날짜별 게시글 통계 Service가 생기면 최근 7일 그래프 데이터로 교체
        req.setAttribute("recentPostStats", "최근 작성 흐름");
        req.getRequestDispatcher("/WEB-INF/views/admin/main.jsp").forward(req, resp);
    }

    private void routeUserGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (action) {
            case "list.do":
                handleUserList(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void routeUserPost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        switch (action) {
            case "ban.do":
                handleUserBan(req, resp);
                break;
            case "unban.do":
                handleUserUnban(req, resp);
                break;
            case "delete.do":
                handleUserDelete(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /admin/user/list.do?keyword={kw}&page={page}
     * 관리자 회원관리 화면에 회원 목록과 페이지 정보를 전달한다.
     * keyword가 있으면 이메일/닉네임 검색 결과를, 없으면 전체 회원을 조회한다.
     *
     * forward: /WEB-INF/views/admin/user/list.jsp
     */
    private void handleUserList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        int pageNo = parseInt(req.getParameter("page"), 1);
        PageDto page = userService.getUserPage(keyword, pageNo);

        req.setAttribute("userList", userService.getUserList(keyword, pageNo));
        req.setAttribute("page", page);
        req.setAttribute("totalUserCount", page.getTotalCount());
        // TODO: UserService에 상태별 회원 수 메서드가 생기면 실제 정상/정지/탈퇴 수로 교체
        req.setAttribute("activeUserCount", 0);
        req.setAttribute("bannedUserCount", 0);
        req.setAttribute("deletedUserCount", 0);
        req.getRequestDispatcher("/WEB-INF/views/admin/user/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/user/ban.do
     * 선택한 회원을 정지 처리한 뒤 다시 회원 목록으로 이동한다.
     *
     * 파라미터: userId
     * redirect: /admin/user/list.do
     */
    private void handleUserBan(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        userService.banUser(parseInt(req.getParameter("userId"), 0));
        resp.sendRedirect(req.getContextPath() + "/admin/user/list.do");
    }

    /**
     * POST /admin/user/unban.do
     * 정지 상태인 회원을 정상 상태로 되돌린 뒤 회원 목록으로 이동한다.
     *
     * 파라미터: userId
     * redirect: /admin/user/list.do
     */
    private void handleUserUnban(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        userService.unbanUser(parseInt(req.getParameter("userId"), 0));
        resp.sendRedirect(req.getContextPath() + "/admin/user/list.do");
    }

    /**
     * POST /admin/user/delete.do
     * 관리자가 선택한 회원을 강제 삭제한다.
     * 실제 삭제 방식은 UserService/UserDao 구현을 따른다.
     *
     * 파라미터: userId
     * redirect: /admin/user/list.do
     */
    private void handleUserDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        userService.forceDeleteUser(parseInt(req.getParameter("userId"), 0));
        resp.sendRedirect(req.getContextPath() + "/admin/user/list.do");
    }

    private void routePostGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (action) {
            case "list.do":
                handlePostList(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void routePostPost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        switch (action) {
            case "delete.do":
                handlePostDelete(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /admin/post/list.do?page={page}
     * 관리자 게시글관리 화면에 전체 게시글 목록을 전달한다.
     * 일반 사용자 화면과 달리 공개/비공개 글을 모두 관리 대상으로 본다.
     *
     * forward: /WEB-INF/views/admin/post/list.jsp
     */
    private void handlePostList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int pageNo = parseInt(req.getParameter("page"), 1);
        req.setAttribute("postList", postService.getPostListForAdmin(pageNo));
        req.setAttribute("page", postService.getPostPageForAdmin(pageNo));
        req.getRequestDispatcher("/WEB-INF/views/admin/post/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/post/delete.do
     * 선택한 게시글을 관리자 권한으로 삭제하고 게시글 목록으로 돌아간다.
     *
     * 파라미터: postId
     * redirect: /admin/post/list.do
     */
    private void handlePostDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        postService.forceDeletePost(parseInt(req.getParameter("postId"), 0));
        resp.sendRedirect(req.getContextPath() + "/admin/post/list.do");
    }

    private void routeCommentGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (action) {
            case "list.do":
                handleCommentList(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void routeCommentPost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        switch (action) {
            case "delete.do":
                handleCommentDelete(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /admin/comment/list.do?page={page}
     * 댓글관리 화면에 전체 댓글 목록과 페이지 정보를 전달한다.
     * 분업 핵심 범위는 아니지만 기존 관리자 메뉴 구조에 맞춰 연결해 둔다.
     *
     * forward: /WEB-INF/views/admin/comment/list.jsp
     */
    private void handleCommentList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int pageNo = parseInt(req.getParameter("page"), 1);
        req.setAttribute("commentList", commentService.getCommentListForAdmin(pageNo));
        req.setAttribute("page", commentService.getCommentPageForAdmin(pageNo));
        req.getRequestDispatcher("/WEB-INF/views/admin/comment/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/comment/delete.do
     * 선택한 댓글을 관리자 권한으로 삭제하고 댓글 목록으로 돌아간다.
     *
     * 파라미터: commentId
     * redirect: /admin/comment/list.do
     */
    private void handleCommentDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        commentService.deleteComment(parseInt(req.getParameter("commentId"), 0));
        resp.sendRedirect(req.getContextPath() + "/admin/comment/list.do");
    }

    private void routeReportGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (action) {
            case "list.do":
                handleReportList(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void routeReportPost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        switch (action) {
            case "resolve.do":
                handleReportResolve(req, resp);
                break;
            case "dismiss.do":
                handleReportDismiss(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /admin/report/list.do?status={status}&page={page}
     * 신고관리 화면에 신고 목록을 전달한다.
     * status가 PENDING/RESOLVED/DISMISSED 중 하나면 해당 상태만 필터링한다.
     *
     * forward: /WEB-INF/views/admin/report/list.jsp
     */
    private void handleReportList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String status = normalizeReportStatus(req.getParameter("status"));
        int pageNo = parseInt(req.getParameter("page"), 1);
        req.setAttribute("reportList", reportService.getReportList(status, pageNo));
        req.setAttribute("page", reportService.getReportPage(status, pageNo));
        req.getRequestDispatcher("/WEB-INF/views/admin/report/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/report/resolve.do
     * 선택한 신고를 처리 완료 상태로 변경한다.
     *
     * 파라미터: reportId
     * redirect: /admin/report/list.do
     */
    private void handleReportResolve(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        reportService.resolveReport(parseInt(req.getParameter("reportId"), 0));
        resp.sendRedirect(req.getContextPath() + "/admin/report/list.do");
    }

    /**
     * POST /admin/report/dismiss.do
     * 선택한 신고를 기각 상태로 변경한다.
     *
     * 파라미터: reportId
     * redirect: /admin/report/list.do
     */
    private void handleReportDismiss(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        reportService.dismissReport(parseInt(req.getParameter("reportId"), 0));
        resp.sendRedirect(req.getContextPath() + "/admin/report/list.do");
    }

    private void routeTagGet(String action, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (action) {
            case "list.do":
                handleTagList(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void routeTagPost(String action, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        switch (action) {
            case "delete.do":
                handleTagDelete(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /admin/tag/list.do
     * 태그관리 화면에 전체 태그 목록을 전달한다.
     * 현재 정렬/검색 파라미터는 화면에만 있고 Service에는 아직 연결하지 않았다.
     *
     * forward: /WEB-INF/views/admin/tag/list.jsp
     */
    private void handleTagList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("tagList", tagService.getAllTagsForAdmin());
        req.getRequestDispatcher("/WEB-INF/views/admin/tag/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/tag/delete.do
     * 선택한 태그를 삭제하고 태그 목록으로 돌아간다.
     * 연결 테이블 삭제 여부는 DB 제약조건과 TagDao 구현을 따른다.
     *
     * 파라미터: tagId
     * redirect: /admin/tag/list.do
     */
    private void handleTagDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
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
