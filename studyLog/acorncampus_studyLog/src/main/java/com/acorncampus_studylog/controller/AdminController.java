package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.PageDto;
import com.acorncampus_studylog.service.CommentService;
import com.acorncampus_studylog.service.PostService;
import com.acorncampus_studylog.service.ReportService;
import com.acorncampus_studylog.service.TagService;
import com.acorncampus_studylog.service.UserService;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 관리자 화면 요청을 한 곳에서 분기하는 컨트롤러.
 * /admin/* 아래 URL을 메뉴별 Service로 연결하고,
 * JSP에 필요한 목록/페이지/통계 데이터를 request attribute로 전달한다.
 *
 * 로그인 및 관리자 권한 검사는 LoginCheckFilter에서 처리한다.
 */
@WebServlet("/admin/*")
public class AdminController extends HttpServlet {

    private final UserService userService = new UserService();
    private final PostService postService = new PostService();
    private final CommentService commentService = new CommentService();
    private final ReportService reportService = new ReportService();
    private final TagService tagService = new TagService();
    private final Gson gson = new Gson();

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
     * 관리자 메인 화면에 필요한 요약 통계를 전달한다.
     */
    private void handleDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        PageDto userPage = userService.getUserPage(null, 1);
        PageDto postPage = postService.getPostPageForAdmin(1);
        PageDto pendingReportPage = reportService.getReportPage("PENDING", 1);
        req.setAttribute("totalUserCount", userPage.getTotalCount());
        // Admin dashboard uses the actual count instead of a temporary zero.
        req.setAttribute("todayUserCount", userService.getTodayUserCount());
        req.setAttribute("totalPostCount", postPage.getTotalCount());
        req.setAttribute("todayPostCount", 0);
        req.setAttribute("pendingReportCount", pendingReportPage.getTotalCount());
        // TODO: 날짜별 게시글 통계 Service가 생기면 최근 7일 그래프 데이터로 교체한다.
        // Admin dashboard chart - recent 7 days registered users.
        req.setAttribute("recentUserStats", userService.getRecentUserStats());
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
     * GET /admin/user/list.do?keyword={kw}&status={status}&page={page}
     * 회원 목록과 페이지 정보를 전달한다.
     */
    private void handleUserList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status = normalizeUserStatus(req.getParameter("status"));
        int pageNo = parseInt(req.getParameter("page"), 1);
        PageDto page = userService.getUserPage(keyword, status, pageNo);
        int totalUserCount = userService.getUserCountByStatus(null);
        int activeUserCount = userService.getUserCountByStatus("active");
        int bannedUserCount = userService.getUserCountByStatus("banned");
        int deletedUserCount = userService.getUserCountByStatus("deleted");
        int graphTotal = Math.max(totalUserCount + deletedUserCount, 1);

        req.setAttribute("userList", userService.getUserList(keyword, status, pageNo));
        req.setAttribute("page", page);
        // 상단 통계와 그래프는 목록 필터와 별개로 전체 회원 상태 기준으로 표시한다.
        req.setAttribute("totalUserCount", totalUserCount);
        req.setAttribute("activeUserCount", activeUserCount);
        req.setAttribute("bannedUserCount", bannedUserCount);
        req.setAttribute("deletedUserCount", deletedUserCount);
        req.setAttribute("activeUserPercent", toPercent(activeUserCount, graphTotal));
        req.setAttribute("bannedUserPercent", toPercent(bannedUserCount, graphTotal));
        req.setAttribute("deletedUserPercent", toPercent(deletedUserCount, graphTotal));
        req.getRequestDispatcher("/WEB-INF/views/admin/user/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/user/ban.do
     * 선택한 회원을 정지 처리한다.
     */
    private void handleUserBan(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        writeActionJson(resp, () -> userService.banUser(parseInt(req.getParameter("userId"), 0)),
                "회원 정지 완료");
    }

    /**
     * POST /admin/user/unban.do
     * 정지 상태인 회원을 정상 상태로 되돌린다.
     */
    private void handleUserUnban(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        writeActionJson(resp, () -> userService.unbanUser(parseInt(req.getParameter("userId"), 0)),
                "회원 정지 해제 완료");
    }

    /**
     * POST /admin/user/delete.do
     * 선택한 회원을 관리자 권한으로 삭제 처리한다.
     */
    private void handleUserDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 관리자 회원 삭제는 soft delete로 처리한다.
        // 사용자가 작성한 게시글/댓글/신고 FK 때문에 users hard delete는 실패할 수 있다.
        writeActionJson(resp, () -> userService.deleteUserByAdmin(parseInt(req.getParameter("userId"), 0)),
                "회원 삭제 완료");
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
     * GET /admin/post/list.do?keyword={kw}&status={Y|N}&page={page}
     * 공개/비공개 글을 모두 포함한 관리자 게시글 목록을 전달한다.
     */
    private void handlePostList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status = normalizePostStatus(req.getParameter("status"));
        int pageNo = parseInt(req.getParameter("page"), 1);
        // 게시글 관리 화면의 검색어/공개상태 필터를 실제 조회 조건으로 전달한다.
        req.setAttribute("postList", postService.getPostListForAdmin(keyword, status, pageNo));
        req.setAttribute("page", postService.getPostPageForAdmin(keyword, status, pageNo));
        req.getRequestDispatcher("/WEB-INF/views/admin/post/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/post/delete.do
     * 선택한 게시글을 관리자 권한으로 삭제 처리한다.
     */
    private void handlePostDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 관리자 게시글 삭제는 soft delete로 통일한다.
        // posts에 연결된 댓글/좋아요/신고 FK 때문에 hard delete는 운영 중 실패할 수 있다.
        writeActionJson(resp, () -> postService.deletePost(parseInt(req.getParameter("postId"), 0)),
                "게시글 삭제 완료");
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
     * 전체 댓글 목록과 페이지 정보를 전달한다.
     */
    private void handleCommentList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int pageNo = parseInt(req.getParameter("page"), 1);
        String keyword = req.getParameter("keyword");
        req.setAttribute("commentList", commentService.getCommentListForAdmin(keyword, pageNo));
        req.setAttribute("page", commentService.getCommentPageForAdmin(keyword, pageNo));
        req.getRequestDispatcher("/WEB-INF/views/admin/comment/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/comment/delete.do
     * 선택한 댓글을 관리자 권한으로 삭제 처리한다.
     */
    private void handleCommentDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        writeActionJson(resp, () -> commentService.deleteComment(parseInt(req.getParameter("commentId"), 0)),
                "댓글 삭제 완료");
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
     * 신고 목록을 전달한다. 현재는 status 필터만 적용한다.
     */
    private void handleReportList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String status = normalizeReportStatus(req.getParameter("status"));
        String keyword = req.getParameter("keyword");
        int pageNo = parseInt(req.getParameter("page"), 1);
        // 신고 관리 검색창의 keyword를 실제 목록/페이지 조회 조건으로 전달한다.
        req.setAttribute("reportList", reportService.getReportList(status, keyword, pageNo));
        req.setAttribute("page", reportService.getReportPage(status, keyword, pageNo));
        req.getRequestDispatcher("/WEB-INF/views/admin/report/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/report/resolve.do
     * 선택한 신고를 처리 완료 상태로 변경한다.
     */
    private void handleReportResolve(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        writeActionJson(resp, () -> reportService.resolveReport(parseInt(req.getParameter("reportId"), 0)),
                "신고 처리 완료");
    }

    /**
     * POST /admin/report/dismiss.do
     * 선택한 신고를 기각 상태로 변경한다.
     */
    private void handleReportDismiss(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        writeActionJson(resp, () -> reportService.dismissReport(parseInt(req.getParameter("reportId"), 0)),
                "신고 기각 완료");
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
     * 전체 태그 목록을 전달한다. 현재 keyword/sort UI는 아직 Service에 연결되지 않았다.
     */
    private void handleTagList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String sort = normalizeTagSort(req.getParameter("sort"));
        // 태그 관리 검색/정렬 UI를 실제 태그 조회 조건으로 전달한다.
        req.setAttribute("tagList", tagService.getAllTagsForAdmin(keyword, sort));
        req.getRequestDispatcher("/WEB-INF/views/admin/tag/list.jsp").forward(req, resp);
    }

    /**
     * POST /admin/tag/delete.do
     * 선택한 태그를 삭제한다.
     */
    private void handleTagDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        writeActionJson(resp, () -> tagService.deleteTag(parseInt(req.getParameter("tagId"), 0)),
                "태그 삭제 완료");
    }

    private void writeActionJson(HttpServletResponse resp, AdminAction action, String successMessage)
            throws IOException {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            action.execute();
            result.put("status", "ok");
            result.put("message", successMessage);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage() == null ? "관리자 작업 처리 중 오류가 발생했습니다." : e.getMessage());
        }

        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(gson.toJson(result));
    }

    private interface AdminAction {
        void execute();
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

    private String normalizeUserStatus(String status) {
        if ("active".equals(status) || "banned".equals(status) || "deleted".equals(status)) {
            return status;
        }
        return null;
    }

    private String normalizePostStatus(String status) {
        if ("Y".equals(status) || "N".equals(status)) {
            return status;
        }
        return null;
    }

    private String normalizeTagSort(String sort) {
        if ("latest".equals(sort) || "name".equals(sort)) {
            return sort;
        }
        return "count";
    }

    private int toPercent(int count, int total) {
        return Math.round(count * 100f / total);
    }
}
