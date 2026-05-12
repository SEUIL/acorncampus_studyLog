package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.CommentDto;
import com.acorncampus_studylog.dto.PostDto;
import com.acorncampus_studylog.dto.SeriesDto;
import com.acorncampus_studylog.dto.UserDto;
import com.acorncampus_studylog.service.CommentService;
import com.acorncampus_studylog.service.LikeService;
import com.acorncampus_studylog.service.PostService;
import com.acorncampus_studylog.service.SeriesService;
import com.google.gson.Gson;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 게시글 컨트롤러
 * URL 패턴: /post/* (공개), /l_check/post/* (로그인 필요 — LoginCheckFilter 적용)
 *
 * GET  /post/list.do                   → 게시글 목록 (페이지네이션)
 * GET  /post/detail.do?id={id}         → 게시글 상세 (조회수 자동 증가)
 * GET  /l_check/post/write.do          → 작성 폼
 * POST /l_check/post/write.do          → 작성 처리
 * GET  /l_check/post/update.do?id={id} → 수정 폼
 * POST /l_check/post/update.do         → 수정 처리
 * POST /l_check/post/delete.do         → 삭제 처리
 * POST /post/upload.do                 → 이미지 업로드 (AJAX, multipart)
 */
@WebServlet(urlPatterns = {"/post/*", "/l_check/post/*"})
public class PostController extends HttpServlet {

    private final PostService    postService    = new PostService();
    private final CommentService commentService = new CommentService();
    private final SeriesService  seriesService  = new SeriesService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // getPathInfo()는 URL에서 서블릿 패턴 이후의 경로를 반환
        // 예: /post/list.do → pathInfo = "/list.do"
        //     /l_check/post/write.do → pathInfo = "/write.do"
        // 두 패턴 모두 pathInfo가 동일하게 나와서 하나의 switch로 처리 가능
        String path = req.getPathInfo();
        if (path == null) path = "/list.do";

        switch (path) {
            case "/list.do":   handleList(req, resp);       break;
            case "/detail.do": handleDetail(req, resp);     break;
            case "/write.do":  handleWriteForm(req, resp);  break;
            case "/update.do": handleUpdateForm(req, resp); break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        switch (path) {
            case "/write.do":  handleWrite(req, resp);  break;
            case "/update.do": handleUpdate(req, resp); break;
            case "/delete.do": handleDelete(req, resp); break;
            case "/upload.do":      handleUpload(req, resp);      break;
            case "/file-upload.do": handleFileUpload(req, resp); break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // ── GET 핸들러 ──────────────────────────────────────────────────────────

    /**
     * GET /post/list.do?page={page}&keyword={keyword}&sort={sort}
     * → 공개 게시글 목록 (최신순/조회수/좋아요순, 페이지네이션, 태그 검색 지원)
     */
    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int    pageNo  = parsePageNo(req.getParameter("page"));
        String keyword = req.getParameter("keyword");
        String sort    = req.getParameter("sort");
        if (sort == null || sort.isEmpty()) sort = "latest";

        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = keyword.trim();

            // --- 태그 검색 분기 로직 추가 ---
            if (keyword.startsWith("#")) {
                String tagName = keyword.substring(1).trim();
                // PostService에 이미 구현된 태그 검색 메서드 활용
                req.setAttribute("postList", postService.getPostsByTag(tagName, pageNo));
                req.setAttribute("page",     postService.getPostPageByTag(tagName, pageNo));
            } else {
                // 일반 키워드 검색
                req.setAttribute("postList", postService.search(keyword, pageNo));
                req.setAttribute("page",     postService.getSearchPage(keyword, pageNo));
            }
            req.setAttribute("keyword", keyword);
        } else {
            req.setAttribute("postList", postService.getPostList(pageNo, sort));
            req.setAttribute("page",     postService.getPostPage(pageNo));
        }

        req.setAttribute("sort", sort);
        req.setAttribute("currentUrl", getCurrentUrl(req));
        req.getRequestDispatcher("/WEB-INF/views/post/list.jsp").forward(req, resp);
    }

    /**
     * GET /post/detail.do?id={postId}
     * → 게시글 상세 (조회수 자동 증가, 댓글 목록 포함)
     * forward: /WEB-INF/views/post/detail.jsp
     */
    private void handleDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LikeService likeService = new LikeService();

        // id 파라미터가 없거나 숫자가 아니면 400 Bad Request 반환
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "게시글 ID가 필요합니다.");
            return;
        }

        int postId;
        try {
            postId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
            return;
        }

        // getPostDetail() 내부에서 조회수 +1도 함께 처리됨
        // 삭제된 게시글(deleted_at IS NOT NULL)은 null이 반환됨
        PostDto post = postService.getPostDetail(postId);
        if (post == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 게시글입니다.");
            return;
        }

        // 댓글 목록 조회: CommentService가 아직 TODO 상태라 null을 반환할 수 있음
        // null이면 빈 리스트로 대체해서 JSP에서 NullPointerException 발생하지 않도록 처리
        List<CommentDto> comments = commentService.getCommentsByPost(postId);
        if (comments == null) comments = Collections.emptyList();

        // --- 좋아요 상태 조회 로직 추가 ---
        // 세션에서 로그인한 사용자 정보를 가져옵니다.
        // (UserDto 타입은 프로젝트의 실제 클래스명에 맞춰 확인이 필요할 수 있습니다.)
        HttpSession session = req.getSession();
        UserDto loginUser = (UserDto) session.getAttribute("loginUser");

        // 로그인한 사용자라면 현재 글에 대한 좋아요 상태("L", "D", null)를 조회해서 JSP로 넘김
        if (loginUser != null) {
            String myLike = likeService.getPostLikeStatus(postId, loginUser.getUserId());
            // 주의: LikeService에 findPostLike를 호출하는 getPostLikeStatus 메서드가 있어야 함!
            req.setAttribute("myLike", myLike);
        }

        req.setAttribute("post",     post);
        req.setAttribute("comments", comments);
        req.setAttribute("backUrl", resolveParentUrl(req, getPostFallbackUrl(req, post)));
        req.getRequestDispatcher("/WEB-INF/views/post/detail.jsp").forward(req, resp);
    }

    /**
     * GET /l_check/post/write.do
     * → 게시글 작성 폼 (로그인 필수 — LoginCheckFilter가 이미 보장)
     * forward: /WEB-INF/views/post/write.jsp
     */
    private void handleWriteForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserDto loginUser = getLoginUser(req);

        // 글 작성 폼에서 "시리즈 선택" 드롭다운에 본인 시리즈 목록을 보여줘야 함
        // SeriesService가 아직 TODO라서 null을 반환할 수 있으므로 빈 리스트로 방어
        List<SeriesDto> seriesList = seriesService.getSeriesByUser(loginUser.getUserId());
        if (seriesList == null) seriesList = Collections.emptyList();

        req.setAttribute("seriesList", seriesList);
        req.getRequestDispatcher("/WEB-INF/views/post/write.jsp").forward(req, resp);
    }

    /**
     * GET /l_check/post/update.do?id={postId}
     * → 게시글 수정 폼 (본인 또는 관리자만)
     * forward: /WEB-INF/views/post/update.jsp
     */
    private void handleUpdateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // getPostOrError()가 null을 반환하면 이미 에러 응답을 보낸 상태이므로 즉시 return
        PostDto post = getPostOrError(req, resp);
        if (post == null) return;

        UserDto loginUser = getLoginUser(req);

        // 작성자 본인이거나 ADMIN 역할이 아니면 수정 불가 (403 Forbidden)
        // 이 체크를 Controller에서 하는 이유: Service는 "권한이 있다고 가정"하고 수정만 담당하기 때문
        if (!isOwnerOrAdmin(loginUser, post.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "수정 권한이 없습니다.");
            return;
        }

        // 수정 폼에서도 시리즈 선택 드롭다운 필요
        List<SeriesDto> seriesList = seriesService.getSeriesByUser(loginUser.getUserId());
        if (seriesList == null) seriesList = Collections.emptyList();

        // tagList는 List<TagDto>이므로 JSP input value에 바로 쓸 수 없음
        // 쉼표 구분 문자열로 미리 변환해서 전달: [java, oracle] → "java, oracle"
        String tagStr = (post.getTagList() == null) ? "" :
                post.getTagList().stream()
                        .map(com.acorncampus_studylog.dto.TagDto::getName)
                        .collect(Collectors.joining(", "));

        req.setAttribute("post",       post);
        req.setAttribute("tagStr",     tagStr);
        req.setAttribute("seriesList", seriesList);
        // write.jsp에서 ${post} 유무로 생성/수정 모드를 구분하므로 write.jsp로 통합
        req.getRequestDispatcher("/WEB-INF/views/post/write.jsp").forward(req, resp);
    }

    // ── POST 핸들러 ─────────────────────────────────────────────────────────

    /**
     * POST /l_check/post/write.do
     * → 게시글 저장 후 상세 페이지로 리다이렉트
     * 파라미터: title, content, isPublic(Y/N), seriesId(optional), tags(쉼표 구분)
     */
    private void handleWrite(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserDto loginUser = getLoginUser(req);
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/user/login.do");
            return; // 반드시 리턴
        }

        String title    = req.getParameter("title");
        String content  = req.getParameter("content");
        String isPublic = normalizeIsPublic(req.getParameter("isPublic"));
        Integer seriesId = parseNullableInt(req.getParameter("seriesId"));
        List<String> tagNames = parseTags(req.getParameter("tags"));

        // 추가: 프론트에서 보낸 썸네일 URL 파라미터 받기
        String thumbnailUrl = req.getParameter("thumbnailUrl");

        // 필수 입력값 검증
        if (title == null || title.trim().isEmpty()) {
            req.setAttribute("errorMsg", "제목을 입력해 주세요.");
            handleWriteForm(req, resp);
            return; // 포워드 후 리턴
        }
        if (content == null || content.trim().isEmpty()) {
            req.setAttribute("errorMsg", "내용을 입력해 주세요.");
            handleWriteForm(req, resp);
            return; // 포워드 후 리턴
        }

        try {
            // 단 한 번만 호출하고 모든 파라미터를 넘깁니다.
            int newPostId = postService.createPost(
                    loginUser.getUserId(), seriesId,
                    title.trim(), content,
                    thumbnailUrl, // 추출된 URL 전달
                    isPublic, tagNames);

            resp.sendRedirect(req.getContextPath() + "/post/detail.do?id=" + newPostId);
            return; // 리다이렉트 후 리턴
        } catch (IllegalArgumentException e) {
            req.setAttribute("errorMsg", e.getMessage());
            handleWriteForm(req, resp);
            return; // 포워드 후 리턴
        }
    }

    /**
     * POST /l_check/post/update.do
     * → 게시글 수정 후 상세 페이지로 리다이렉트
     * 파라미터: postId, title, content, isPublic, seriesId(optional), tags(쉼표 구분)
     */
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        UserDto loginUser = getLoginUser(req);
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/user/login.do");
            return;
        }

        int postId;
        try {
            postId = Integer.parseInt(req.getParameter("postId"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
            return;
        }

        PostDto post = postService.getPostDetail(postId);
        if (post == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 게시글입니다.");
            return;
        }
        if (!isOwnerOrAdmin(loginUser, post.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "수정 권한이 없습니다.");
            return;
        }

        String title    = req.getParameter("title");
        String content  = req.getParameter("content");
        String isPublic = normalizeIsPublic(req.getParameter("isPublic"));
        Integer seriesId  = parseNullableInt(req.getParameter("seriesId"));
        List<String> tagNames = parseTags(req.getParameter("tags"));

        // 추가: 수정된 본문에서 추출된 새 썸네일 URL 받기
        String thumbnailUrl = req.getParameter("thumbnailUrl");

        try {
            // 기존 썸네일을 무조건 유지하는 대신, 새로 전달받은 thumbnailUrl을 사용하도록 통합
            postService.updatePost(postId, seriesId, title.trim(), content, thumbnailUrl, isPublic, tagNames);
            resp.sendRedirect(req.getContextPath() + "/post/detail.do?id=" + postId);
            return; // 완료 후 리턴
        } catch (IllegalArgumentException e) {
            resp.sendRedirect(req.getContextPath() + "/l_check/post/update.do?id=" + postId + "&error=" + e.getMessage());
            return; // 에러 후 리턴
        }
    }

    /**
     * POST /l_check/post/delete.do
     * → 게시글 소프트 삭제 후 목록으로 리다이렉트
     * 파라미터: postId
     */
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        UserDto loginUser = getLoginUser(req);
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/user/login.do");
            return;
        }

        int postId;
        try {
            postId = Integer.parseInt(req.getParameter("postId"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
            return;
        }

        PostDto post = postService.getPostDetail(postId);

        // 이미 없는 글이면 에러 없이 목록으로 리다이렉트 (사용자가 두 번 삭제 누른 경우 대비)
        if (post == null) {
            resp.sendRedirect(req.getContextPath() + "/post/list.do");
            return;
        }
        if (!isOwnerOrAdmin(loginUser, post.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "삭제 권한이 없습니다.");
            return;
        }

        // 소프트 삭제: DB에서 행을 지우지 않고 deleted_at 컬럼에 현재 시각을 설정
        // 이후 모든 조회 쿼리는 WHERE deleted_at IS NULL 조건으로 자동 필터링됨
        postService.deletePost(postId);
        resp.sendRedirect(req.getContextPath() + "/post/list.do");
    }

    /**
     * POST /post/upload.do — Toast UI Editor 이미지 업로드 (multipart/form-data)
     * 응답: {"url": "/contextPath/resources/upload/xxx.jpg"}
     * Toast UI Editor가 이 형식의 JSON을 받아서 에디터 내 이미지를 삽입함
     */
    private void handleUpload(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 응답 타입을 먼저 설정해야 write() 이전에 헤더가 나감
        resp.setContentType("application/json; charset=UTF-8");
        Map<String, Object> result = new LinkedHashMap<>();

        // Content-Type이 multipart/form-data가 아니면 잘못된 요청
        if (!ServletFileUpload.isMultipartContent(req)) {
            result.put("error", "multipart 요청이 아닙니다.");
            resp.getWriter().write(new Gson().toJson(result));
            return;
        }

        try {
            // DiskFileItemFactory: 업로드된 파일을 임시 디스크에 버퍼링하는 팩토리
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            // 파일 크기 10MB 제한 (초과 시 FileUploadBase.FileSizeLimitExceededException)
            upload.setFileSizeMax(10 * 1024 * 1024L);

            List<FileItem> items = upload.parseRequest(req);

            // ServletContext.getRealPath()로 서버 디스크의 실제 절대 경로를 구함
            // 예: C:/tomcat/webapps/acorncampus_studyLog/resources/upload
            String uploadDir = getServletContext().getRealPath("/resources/upload");

            for (FileItem item : items) {
                // isFormField() == false → 실제 파일 파트
                // isFormField() == true  → 일반 텍스트 파트 (파일 아님, 스킵)
                if (!item.isFormField()) {
                    String originalName = item.getName();
                    byte[] data = item.get();

                    // PostService.saveUploadedImage()에서 UUID 파일명 생성 + 저장 처리
                    // 반환값: "/resources/upload/abc123.jpg" (웹 경로)
                    String url = postService.saveUploadedImage(uploadDir, originalName, data);

                    // Toast UI Editor는 {"url": "..."} 형식의 JSON을 기대함
                    // contextPath를 붙여야 배포 경로가 바뀌어도 동작함
                    result.put("url", req.getContextPath() + url);
                    resp.getWriter().write(new Gson().toJson(result));
                    return; // 파일 하나만 처리하고 종료
                }
            }

            result.put("error", "업로드된 파일이 없습니다.");
            resp.getWriter().write(new Gson().toJson(result));

        } catch (Exception e) {
            result.put("error", "업로드 실패: " + e.getMessage());
            resp.getWriter().write(new Gson().toJson(result));
        }
    }

    /** POST /l_check/post/file-upload.do — 첨부파일 업로드, {"url","name"} 반환 */
    private void handleFileUpload(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        Map<String, Object> result = new LinkedHashMap<>();

        if (!ServletFileUpload.isMultipartContent(req)) {
            result.put("error", "multipart 요청이 아닙니다.");
            resp.getWriter().write(new Gson().toJson(result));
            return;
        }

        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(20 * 1024 * 1024L); // 20MB

            List<FileItem> items = upload.parseRequest(req);
            String uploadDir = getServletContext().getRealPath("/resources/upload");

            for (FileItem item : items) {
                if (!item.isFormField()) {
                    String originalName = item.getName();
                    String url = postService.saveUploadedImage(uploadDir, originalName, item.get());
                    result.put("url",  req.getContextPath() + url);
                    result.put("name", originalName);
                    resp.getWriter().write(new Gson().toJson(result));
                    return;
                }
            }

            result.put("error", "업로드된 파일이 없습니다.");
            resp.getWriter().write(new Gson().toJson(result));

        } catch (Exception e) {
            result.put("error", "업로드 실패: " + e.getMessage());
            resp.getWriter().write(new Gson().toJson(result));
        }
    }

    // ── 공통 헬퍼 ───────────────────────────────────────────────────────────

    /** 세션에서 로그인 사용자 반환 (세션 없으면 null) */
    private UserDto getLoginUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        // getSession(false): 세션이 없어도 새로 만들지 않음 (getSession(true)와 반대)
        if (session == null) return null;
        return (UserDto) session.getAttribute("loginUser");
    }

    /**
     * URL 파라미터 "id"로 게시글 조회
     * 파라미터 없거나 숫자가 아니거나 게시글이 없으면 에러 응답 후 null 반환
     * null 반환 시 호출부에서 즉시 return 해야 함
     */
    private PostDto getPostOrError(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "게시글 ID가 필요합니다.");
            return null;
        }
        int postId;
        try {
            postId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
            return null;
        }
        PostDto post = postService.getPostDetail(postId);
        if (post == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 게시글입니다.");
            return null;
        }
        return post;
    }

    /**
     * 작성자 본인 또는 ADMIN 역할인지 확인
     * true이면 수정/삭제 허용, false이면 Controller에서 403 처리
     */
    private boolean isOwnerOrAdmin(UserDto loginUser, int postOwnerId) {
        return loginUser.getUserId() == postOwnerId
                || "ADMIN".equals(loginUser.getRole());
    }

    private String getPostFallbackUrl(HttpServletRequest req, PostDto post) {
        if (post.getSeriesId() != null) {
            return req.getContextPath() + "/series/detail.do?id=" + post.getSeriesId();
        }
        return req.getContextPath() + "/post/list.do";
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

    /**
     * page 파라미터 파싱
     * null이거나 숫자가 아니거나 0 이하이면 1 반환
     */
    private int parsePageNo(String pageParam) {
        try {
            int page = Integer.parseInt(pageParam);
            return page > 0 ? page : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * seriesId 파라미터 파싱
     * 비어 있거나 0 이하이면 null 반환 → postDao.insert()에서 DB NULL로 저장됨
     */
    private Integer parseNullableInt(String param) {
        if (param == null || param.trim().isEmpty()) return null;
        try {
            int val = Integer.parseInt(param.trim());
            return val > 0 ? val : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeIsPublic(String isPublic) {
        return "Y".equalsIgnoreCase(isPublic) ? "Y" : "N";
    }

    /**
     * 쉼표 구분 태그 문자열 → List 변환
     * 예: "Java, Spring, Oracle " → ["Java", "Spring", "Oracle"]
     * 각 항목을 trim()하고 빈 문자열은 필터링
     */
    private List<String> parseTags(String tagsParam) {
        if (tagsParam == null || tagsParam.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(tagsParam.split(","))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
    }
}
