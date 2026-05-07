package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.SeriesDto;
import com.acorncampus_studylog.dto.UserDto;
import com.acorncampus_studylog.service.PostService;
import com.acorncampus_studylog.service.SeriesService;
import com.acorncampus_studylog.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@WebServlet("/l_check/user/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,       // 1MB 초과 시 디스크에 임시 저장
    maxFileSize       = 5 * 1024 * 1024,   // 파일 하나 최대 5MB
    maxRequestSize    = 10 * 1024 * 1024   // 요청 전체 최대 10MB
)
public class MypageController extends HttpServlet {

    private final UserService   userService   = new UserService();
    private final PostService   postService   = new PostService();
    private final SeriesService seriesService = new SeriesService();

    /** 프로필 이미지 저장 경로 (webapp 기준 상대 경로) */
    private static final String UPLOAD_DIR = "resources/upload/profile";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/mypage.do";

        switch (path) {
            case "/mypage.do":
                handleMypage(req, resp);
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
        if (path == null) path = "";

        switch (path) {
            case "/update.do":
                handleUpdate(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * GET /l_check/user/mypage.do?page={page}
     * → 마이페이지 (내 게시글 목록 + 시리즈 목록)
     */
    private void handleMypage(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // LoginCheckFilter가 /l_check/* 패턴에 걸려 있어서
        // 여기까지 왔다면 loginUser가 세션에 반드시 있음
        UserDto loginUser = getLoginUser(req);

        // page 파라미터 없으면 1페이지
        int pageNo;
        try {
            pageNo = Integer.parseInt(req.getParameter("page"));
            if (pageNo < 1) pageNo = 1;
        } catch (Exception e) {
            pageNo = 1;
        }

        req.setAttribute("postList", postService.getPostsByUser(loginUser.getUserId(), pageNo));
        req.setAttribute("postPage", postService.getPostPageByUser(loginUser.getUserId(), pageNo));

        List<SeriesDto> seriesList = seriesService.getSeriesByUser(loginUser.getUserId());
        if (seriesList == null) seriesList = Collections.emptyList();
        req.setAttribute("seriesList", seriesList);

        req.getRequestDispatcher("/WEB-INF/views/user/mypage.jsp").forward(req, resp);
    }

    /**
     * GET /l_check/user/update.do → 프로필 수정 폼
     * bio/avatarUrl은 UserDto 세션에 없으므로 DB에서 직접 조회해서 넘김
     */
    private void handleUpdateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserDto loginUser = getLoginUser(req);
        req.setAttribute("userDetail", userService.getUserById(loginUser.getUserId()));
        req.getRequestDispatcher("/WEB-INF/views/user/update.jsp").forward(req, resp);
    }

    /**
     * POST /l_check/user/update.do → 프로필 수정 처리
     * 파라미터: nickname, bio, avatarUrl(URL 입력) / avatarFile(파일 업로드)
     * 우선순위: 파일 업로드 > URL 입력 > 기존값 유지
     */
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserDto loginUser = getLoginUser(req);

        String nickname = req.getParameter("nickname");
        String bio      = req.getParameter("bio");

        // 닉네임 필수 검증
        if (nickname == null || nickname.trim().isEmpty()) {
            req.setAttribute("errorMsg", "닉네임을 입력해 주세요.");
            req.setAttribute("userDetail", userService.getUserById(loginUser.getUserId()));
            req.getRequestDispatcher("/WEB-INF/views/user/update.jsp").forward(req, resp);
            return;
        }

        // 닉네임 중복 시 서비스에서 RuntimeException 발생 → 에러 메시지 처리
        String avatarUrl;
        try {
            // 1. 파일 업로드 우선 처리
            avatarUrl = saveUploadedFile(req, loginUser.getUserId());

            // 2. 파일 없으면 URL 입력값 사용
            if (avatarUrl == null) {
                avatarUrl = req.getParameter("avatarUrl");
                if (avatarUrl != null && avatarUrl.trim().isEmpty()) {
                    avatarUrl = null;
                }
            }

            userService.updateProfile(loginUser.getUserId(), nickname.trim(), bio, avatarUrl);

        } catch (RuntimeException e) {
            req.setAttribute("errorMsg", e.getMessage());
            req.setAttribute("userDetail", userService.getUserById(loginUser.getUserId()));
            req.getRequestDispatcher("/WEB-INF/views/user/update.jsp").forward(req, resp);
            return;
        }

        // 세션 닉네임·아바타 업데이트 → 사이드바 즉시 반영
        loginUser.setUsername(nickname.trim());
        if (avatarUrl != null) loginUser.setAvatarUrl(avatarUrl);
        req.getSession(false).setAttribute("loginUser", loginUser);

        resp.sendRedirect(req.getContextPath() + "/l_check/user/mypage.do");
    }

    /**
     * 프로필 이미지 파일 저장
     * @return 저장된 파일의 컨텍스트 경로 (예: /resources/upload/profile/uuid.jpg),
     *         업로드된 파일이 없으면 null
     */
    private String saveUploadedFile(HttpServletRequest req, int userId) throws IOException, ServletException {
        Part filePart = req.getPart("avatarFile");

        // 파일이 없거나 크기가 0이면 null 반환
        if (filePart == null || filePart.getSize() == 0) return null;

        // 원본 파일명에서 확장자 추출
        String originalName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        String ext = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.'))
                : ".jpg";

        // UUID로 고유 파일명 생성 (충돌 방지)
        String savedName = userId + "_" + UUID.randomUUID().toString().replace("-", "") + ext;

        // 저장 디렉토리 생성 (없으면)
        String realPath = getServletContext().getRealPath(UPLOAD_DIR);
        File dir = new File(realPath);
        if (!dir.exists()) dir.mkdirs();

        // 파일 저장
        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, new File(dir, savedName).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // DB/세션에 저장할 경로 반환 (컨텍스트 루트 기준)
        return "/" + UPLOAD_DIR + "/" + savedName;
    }

    /** 세션에서 로그인 사용자 반환 */
    private UserDto getLoginUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        return (UserDto) session.getAttribute("loginUser");
    }
}
