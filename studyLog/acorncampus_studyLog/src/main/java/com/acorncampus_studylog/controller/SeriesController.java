package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.PageDto;
import com.acorncampus_studylog.dto.SeriesDto;
import com.acorncampus_studylog.dto.UserDto;
import com.acorncampus_studylog.service.SeriesService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/series/*", "/l_check/series/*"})
public class SeriesController extends HttpServlet {

    private final SeriesService seriesService = new SeriesService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            path = "/list.do";
        }

        switch (path) {
            case "/list.do":
                handleList(req, resp);
                break;
            case "/detail.do":
                handleDetail(req, resp);
                break;
            case "/write.do":
                handleWriteForm(req, resp);
                break;
            case "/update.do":
                handleUpdateForm(req, resp);
                break;
            default:
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

        switch (path) {
            case "/write.do":
                handleWrite(req, resp);
                break;
            case "/update.do":
                handleUpdate(req, resp);
                break;
            case "/delete.do":
                handleDelete(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /series/list.do?page={page}&keyword={keyword}
     * → 시리즈 목록 (태그 검색 시 해당 태그를 포함한 게시글이 있는 시리즈 조회)
     */
    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int    pageNo  = parseInt(req.getParameter("page"), 1);
        String keyword = req.getParameter("keyword");

        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = keyword.trim();

            // --- 태그 검색 분기 로직 추가 ---
            if (keyword.startsWith("#")) {
                String tagName = keyword.substring(1).trim();
                // SeriesService에 새로 추가할 메서드 호출
                req.setAttribute("seriesList", seriesService.getSeriesByTag(tagName, pageNo));
                req.setAttribute("page",       seriesService.getSeriesPageByTag(tagName, pageNo));
            } else {
                // 일반 키워드 검색
                req.setAttribute("seriesList", seriesService.search(keyword, pageNo));
                req.setAttribute("page",       seriesService.getSearchPage(keyword, pageNo));
            }
            req.setAttribute("keyword", keyword);
        } else {
            req.setAttribute("seriesList", seriesService.getSeriesList(pageNo));
            req.setAttribute("page",       seriesService.getSeriesPage(pageNo));
        }

        req.setAttribute("currentUrl", getCurrentUrl(req));
        req.getRequestDispatcher("/WEB-INF/views/series/list.jsp").forward(req, resp);
    }

    private void handleDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 시리즈 상세와 소속 게시글 목록을 함께 조회
        int seriesId = parseInt(req.getParameter("id"), 0);
        SeriesDto series = seriesService.getSeriesDetail(seriesId);
        if (series == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        req.setAttribute("series", series);
        req.setAttribute("postList", series.getPostList());
        req.setAttribute("backUrl", resolveParentUrl(req, req.getContextPath() + "/series/list.do"));
        req.setAttribute("currentUrl", getCurrentUrl(req));
        req.getRequestDispatcher("/WEB-INF/views/series/detail.jsp").forward(req, resp);
    }

    private void handleWriteForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/series/write.jsp").forward(req, resp);
    }

    private void handleUpdateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 수정 화면은 시리즈 소유자 또는 관리자만 접근 가능
        int seriesId = parseInt(req.getParameter("id"), 0);
        SeriesDto series = seriesService.getSeriesDetail(seriesId);
        if (series == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!canManageSeries(req, series)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // series/write.jsp에서 ${series} 유무로 생성/수정 모드를 구분하므로 write.jsp로 통합
        req.setAttribute("series", series);
        req.getRequestDispatcher("/WEB-INF/views/series/write.jsp").forward(req, resp);
    }

    private void handleWrite(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 로그인 사용자의 새 시리즈 생성
        UserDto loginUser = getLoginUser(req);
        int seriesId = seriesService.createSeries(
                loginUser.getUserId(),
                req.getParameter("name"),
                req.getParameter("description"),
                req.getParameter("isPublic")
        );
        resp.sendRedirect(req.getContextPath() + "/l_check/series/detail.do?id=" + seriesId);
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 소유권 확인 후 시리즈 수정
        int seriesId = parseInt(req.getParameter("seriesId"), 0);
        SeriesDto series = seriesService.getSeriesDetail(seriesId);
        if (series == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!canManageSeries(req, series)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        seriesService.updateSeries(
                seriesId,
                req.getParameter("name"),
                req.getParameter("description"),
                req.getParameter("isPublic")
        );
        resp.sendRedirect(req.getContextPath() + "/l_check/series/detail.do?id=" + seriesId);
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 소유권 확인 후 시리즈 삭제
        int seriesId = parseInt(req.getParameter("seriesId"), 0);
        SeriesDto series = seriesService.getSeriesDetail(seriesId);
        if (series == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!canManageSeries(req, series)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        seriesService.deleteSeries(seriesId);
        resp.sendRedirect(req.getContextPath() + "/l_check/user/mypage.do");
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private UserDto getLoginUser(HttpServletRequest req) {
        return (UserDto) req.getSession().getAttribute("loginUser");
    }

    private String resolveParentUrl(HttpServletRequest req, String fallbackUrl) {
        String parentUrl = req.getParameter("parentUrl");
        if (parentUrl == null || parentUrl.trim().isEmpty()) {
            return fallbackUrl;
        }

        String contextPath = req.getContextPath();
        boolean validAppPath = contextPath.isEmpty()
                ? parentUrl.startsWith("/") && !parentUrl.startsWith("//")
                : parentUrl.startsWith(contextPath + "/");
        if (!validAppPath) {
            return fallbackUrl;
        }

        if (parentUrl.equals(getCurrentUrl(req))) {
            return fallbackUrl;
        }

        return parentUrl;
    }

    private String getCurrentUrl(HttpServletRequest req) {
        String currentUrl = req.getRequestURI();
        if (req.getQueryString() != null) {
            currentUrl += "?" + req.getQueryString();
        }
        return currentUrl;
    }

    private boolean canManageSeries(HttpServletRequest req, SeriesDto series) {
        UserDto loginUser = getLoginUser(req);
        return loginUser != null
                && (loginUser.getUserId() == series.getUserId() || "ADMIN".equals(loginUser.getRole()));
    }
}
