package com.acorncampus_studylog.filter;

import com.acorncampus_studylog.dao.PostDao;
import com.acorncampus_studylog.dto.UserDto;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 잔디(Contributions) 데이터 주입 필터
 *
 * <p>사이드바(sideBar.jsp)에 표시할 최근 24주(168일)치 기여도 데이터를
 * 매 페이지 요청마다 request attribute로 주입한다.</p>
 *
 * <h3>동작 흐름</h3>
 * <ol>
 *   <li>정적 리소스(css/js/img 등) 요청은 즉시 통과 — DB 조회 불필요</li>
 *   <li>세션에 loginUser가 없으면(비로그인) 통과 — 잔디는 로그인 유저만 표시</li>
 *   <li>PostDao.getActivityMap()으로 최근 1년 날짜별 게시글 수 조회</li>
 *   <li>오늘부터 167일 전까지 168칸의 레벨(0~4) 리스트를 계산해 request에 설정</li>
 *   <li>sideBar.jsp에서 ${jandiLevels}로 참조하여 동적 렌더링</li>
 * </ol>
 *
 * <h3>레벨 기준</h3>
 * <ul>
 *   <li>0 게시글 → level 0 (빈 칸)</li>
 *   <li>1 게시글 → level 1</li>
 *   <li>2 게시글 → level 2</li>
 *   <li>3 게시글 → level 3</li>
 *   <li>4 게시글 이상 → level 4</li>
 * </ul>
 *
 * <h3>web.xml 매핑</h3>
 * <pre>
 *   &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 * </pre>
 * EncodingFilter 다음, LoginCheckFilter 전에 선언할 것.
 * 정적 리소스 경로는 필터 내부에서 직접 skip 처리하므로 성능 영향 최소화.
 */
public class JandiFilter implements Filter {

    /** 표시할 일수 (12주 = 84일) */
    private static final int DISPLAY_DAYS = 84;

    /** DB 날짜 조회용 포맷 (PostDao.getActivityMap 반환 키와 동일) */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PostDao postDao = new PostDao();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String uri = req.getRequestURI();

        // ── 1. 정적 리소스는 즉시 통과 ─────────────────────────────────
        // CSS, JS, 이미지, 폰트 등은 DB 조회가 전혀 필요 없음
        if (isStaticResource(uri)) {
            chain.doFilter(request, response);
            return;
        }

        // ── 2. 비로그인 상태면 통과 (잔디는 로그인 유저 전용) ───────────
        HttpSession session = req.getSession(false);
        UserDto loginUser = (session != null)
                ? (UserDto) session.getAttribute("loginUser")
                : null;

        if (loginUser == null) {
            chain.doFilter(request, response);
            return;
        }

        // ── 3. DB에서 최근 1년 날짜별 게시글 수 조회 ────────────────────
        // 쿼리 실패 시 사이트 전체가 깨지지 않도록 try-catch로 보호
        List<Integer> jandiLevels = new ArrayList<>();
        try {
            Map<String, Integer> activityMap = postDao.getActivityMap(loginUser.getUserId());

            // ── 4. 오늘부터 (DISPLAY_DAYS - 1)일 전까지 레벨 계산 ────────
            // 인덱스 0 = 가장 오래된 날, 인덱스 83 = 오늘 (왼→오 순서)
            LocalDate today = LocalDate.now();
            for (int i = DISPLAY_DAYS - 1; i >= 0; i--) {
                String dateKey = today.minusDays(i).format(DATE_FMT);
                int count = activityMap.getOrDefault(dateKey, 0);
                jandiLevels.add(countToLevel(count));
            }
        } catch (Exception e) {
            // 잔디 데이터 조회 실패 시: 빈 리스트로 채워 사이트는 정상 동작
            // 사이드바는 빈 잔디(회색 칸만)로 표시됨
            jandiLevels.clear();
            for (int i = 0; i < DISPLAY_DAYS; i++) {
                jandiLevels.add(0);
            }
        }

        // ── 5. request attribute 설정 → sideBar.jsp에서 ${jandiLevels} 참조 ──
        req.setAttribute("jandiLevels", jandiLevels);

        chain.doFilter(request, response);
    }

    /**
     * 게시글 수 → 잔디 레벨 변환
     *
     * @param count 해당 날짜의 게시글 수
     * @return 0(없음) ~ 4(매우 활발) 레벨
     */
    private int countToLevel(int count) {
        if (count <= 0) return 0;
        if (count == 1) return 1;
        if (count == 2) return 2;
        if (count == 3) return 3;
        return 4; // 4개 이상
    }

    /**
     * 정적 리소스 경로 여부 판단
     *
     * <p>/resources/ 하위 경로(css/js/img/upload 등)와
     * favicon.ico는 DB 조회 없이 통과시킨다.</p>
     *
     * @param uri 요청 URI (컨텍스트 경로 포함)
     * @return 정적 리소스면 true
     */
    private boolean isStaticResource(String uri) {
        return uri.contains("/resources/")
            || uri.endsWith(".ico")
            || uri.endsWith(".png")
            || uri.endsWith(".jpg")
            || uri.endsWith(".gif")
            || uri.endsWith(".woff")
            || uri.endsWith(".woff2");
    }
}
