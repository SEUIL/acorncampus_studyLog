package com.acorncampus_studylog.dao;

import com.acorncampus_studylog.dto.*;
import com.acorncampus_studylog.util.DBUtil;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 실제 Oracle DB에 연결하여 schema.sql ↔ DAO ↔ DTO 간 매핑을 검증하는 통합 테스트.
 * 전제조건: Oracle testdb 인스턴스 실행 중, blog 계정 + schema.sql 테이블 생성 완료.
 * 실행 후 @AfterAll에서 테스트 데이터를 하드 삭제하여 DB를 원래 상태로 복원한다.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DAO-DTO-Schema 통합 검증 (실 DB)")
class DaoIntegrationTest {

    // ── DAO 인스턴스 ────────────────────────────────────────────────────────
    private static final UserDao    userDao    = new UserDao();
    private static final SeriesDao  seriesDao  = new SeriesDao();
    private static final PostDao    postDao    = new PostDao();
    private static final TagDao     tagDao     = new TagDao();
    private static final CommentDao commentDao = new CommentDao();
    private static final LikeDao    likeDao    = new LikeDao();
    private static final ReportDao  reportDao  = new ReportDao();

    // ── 테스트 데이터 식별자 (테스트 간 공유, 정리용) ─────────────────────
    private static int testUserId;
    private static int testSeriesId;
    private static int testPostId;
    private static int testCommentId;
    private static int testReplyId;
    private static int testReportId;

    // 태그명은 중복 방지를 위해 타임스탬프 접두어 사용
    private static final String TAG_PREFIX = "tst" + (System.currentTimeMillis() % 100000) + "_";

    // 사용자 email / nickname 도 중복 방지
    private static final String TEST_EMAIL    = "dao_test_" + System.currentTimeMillis() + "@test.com";
    private static final String TEST_NICKNAME = "테스터" + (System.currentTimeMillis() % 100000);

    // ── 01: DB 연결 ─────────────────────────────────────────────────────────

    @Test @Order(1)
    @DisplayName("01. DB 연결 — DBUtil.getConnection()")
    void dbConnection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            assertNotNull(conn, "DBUtil.getConnection()이 null 반환");
            assertFalse(conn.isClosed(), "Connection이 이미 닫혀 있음");
        }
    }

    // ── 02: User INSERT + findByEmail ────────────────────────────────────────

    @Test @Order(2)
    @DisplayName("02. UserDao.insert + findByEmail — users 테이블 컬럼 매핑")
    void userInsertAndFindByEmail() {
        // BCrypt 해시 형식의 더미 비밀번호 (200자 이내 VARCHAR2 검증)
        String hashedPw = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

        testUserId = userDao.insert(TEST_EMAIL, TEST_NICKNAME, hashedPw);
        assertTrue(testUserId > 0, "users_seq.NEXTVAL 채번 실패 또는 INSERT 실패");

        UserDetailDto u = userDao.findByEmail(TEST_EMAIL);
        assertNotNull(u,     "findByEmail 결과 null — email 컬럼명 또는 deleted_at IS NULL 조건 확인");
        assertEquals(testUserId,    u.getUserId(),   "user_id 매핑 불일치");
        assertEquals(TEST_EMAIL,    u.getEmail(),    "email 컬럼 매핑 불일치");
        assertEquals(TEST_NICKNAME, u.getNickname(), "nickname 컬럼 매핑 불일치");
        assertEquals("USER", u.getRole(),            "role 기본값 'USER' 또는 컬럼 매핑 불일치");
        assertEquals("N",    u.getIsBanned().trim(), "is_banned 기본값 'N' 또는 CHAR(1) 매핑 불일치");
        assertNotNull(u.getCreatedAt(), "created_at 컬럼 매핑 불일치");
        assertNull(u.getDeletedAt(),    "신규 사용자 deleted_at은 null이어야 함");
    }

    // ── 03: findById ─────────────────────────────────────────────────────────

    @Test @Order(3)
    @DisplayName("03. UserDao.findById — user_id 기반 조회")
    void userFindById() {
        UserDetailDto u = userDao.findById(testUserId);
        assertNotNull(u, "findById 결과 null — user_id 컬럼명 확인");
        assertEquals(testUserId, u.getUserId());
        assertEquals(TEST_EMAIL, u.getEmail());
    }

    // ── 04: updateProfile ────────────────────────────────────────────────────

    @Test @Order(4)
    @DisplayName("04. UserDao.updateProfile — nickname/bio/avatar_url 업데이트")
    void userUpdateProfile() {
        String newNick = TEST_NICKNAME + "_수정";
        userDao.updateProfile(testUserId, newNick, "자기소개 내용", "/upload/avatar.png");

        UserDetailDto u = userDao.findById(testUserId);
        assertNotNull(u);
        assertEquals(newNick,           u.getNickname(), "nickname 업데이트 실패 또는 컬럼 매핑 불일치");
        assertEquals("자기소개 내용",   u.getBio(),      "bio 컬럼 매핑 불일치");
        assertEquals("/upload/avatar.png", u.getAvatarUrl(), "avatar_url 컬럼 매핑 불일치");
    }

    // ── 05: ban / unban ──────────────────────────────────────────────────────

    @Test @Order(5)
    @DisplayName("05. UserDao.ban / unban — is_banned CHAR(1) 토글")
    void userBanUnban() {
        userDao.ban(testUserId);
        assertEquals("Y", userDao.findById(testUserId).getIsBanned().trim(),
                "ban() 후 is_banned='Y' 아님");

        userDao.unban(testUserId);
        assertEquals("N", userDao.findById(testUserId).getIsBanned().trim(),
                "unban() 후 is_banned='N' 아님");
    }

    // ── 06: findAll / countAll ───────────────────────────────────────────────

    @Test @Order(6)
    @DisplayName("06. UserDao.countAll + findAll — 페이지네이션 / 검색")
    void userFindAll() {
        int total = userDao.countAll(null);
        assertTrue(total >= 1, "countAll이 0 반환 — users 테이블 조회 실패");

        List<UserDetailDto> page = userDao.findAll(null, 0, 10);
        assertFalse(page.isEmpty(), "findAll 결과 비어 있음");
        page.forEach(u -> assertNotNull(u.getNickname(), "목록 조회 시 nickname null"));
    }

    // ── 07: Series INSERT + findById ─────────────────────────────────────────

    @Test @Order(7)
    @DisplayName("07. SeriesDao.insert + findById — series 테이블 컬럼 매핑 + JOIN")
    void seriesInsertAndFindById() {
        testSeriesId = seriesDao.insert(testUserId, "테스트 시리즈", "시리즈 설명입니다.", "Y");
        assertTrue(testSeriesId > 0, "series_seq.NEXTVAL 채번 실패");

        SeriesDto s = seriesDao.findById(testSeriesId);
        assertNotNull(s, "findById 결과 null — series_id 컬럼명 확인");
        assertEquals(testSeriesId, s.getSeriesId(),       "series_id 매핑 불일치");
        assertEquals(testUserId,   s.getUserId(),          "user_id FK 매핑 불일치");
        assertEquals("테스트 시리즈",   s.getName(),       "name 컬럼 매핑 불일치");
        assertEquals("시리즈 설명입니다.", s.getDescription(), "description 컬럼 매핑 불일치");
        assertEquals("Y",          s.getIsPublic().trim(), "is_public CHAR(1) 매핑 불일치");
        assertNotNull(s.getCreatedAt(), "created_at 매핑 불일치");
        // JOIN 검증: users.nickname AS author_name
        assertNotNull(s.getAuthorName(), "JOIN 별칭 author_name 매핑 실패 — SELECT u.nickname AS author_name 확인");
        assertEquals(0, s.getPostCount(), "신규 시리즈 post_count는 0이어야 함");
    }

    // ── 08: Series update ────────────────────────────────────────────────────

    @Test @Order(8)
    @DisplayName("08. SeriesDao.update — name/description/is_public 업데이트")
    void seriesUpdate() {
        seriesDao.update(testSeriesId, "수정된 시리즈명", "수정된 설명", "N");
        SeriesDto s = seriesDao.findById(testSeriesId);
        assertNotNull(s);
        assertEquals("수정된 시리즈명", s.getName());
        assertEquals("N", s.getIsPublic().trim(), "is_public 'N' 업데이트 실패");
    }

    // ── 09: Post INSERT + findById ───────────────────────────────────────────

    @Test @Order(9)
    @DisplayName("09. PostDao.insert + findById — posts 테이블 컬럼 매핑 + JOIN 집계 필드")
    void postInsertAndFindById() {
        testPostId = postDao.insert(
                testUserId, testSeriesId,
                "테스트 게시글 제목", "**마크다운** 본문 내용",
                "/upload/thumb.png", "Y");
        assertTrue(testPostId > 0, "posts_seq.NEXTVAL 채번 실패");

        PostDto p = postDao.findById(testPostId);
        assertNotNull(p, "findById 결과 null — post_id 컬럼명 또는 deleted_at IS NULL 조건 확인");
        assertEquals(testPostId,   p.getPostId(),          "post_id 매핑 불일치");
        assertEquals(testUserId,   p.getUserId(),           "user_id FK 매핑 불일치");
        assertEquals(testSeriesId, p.getSeriesId(),         "series_id FK 매핑 불일치 (Integer nullable 확인)");
        assertEquals("테스트 게시글 제목", p.getTitle(),     "title 컬럼 매핑 불일치");
        assertNotNull(p.getContent(),                        "CLOB content 컬럼 매핑 실패");
        assertTrue(p.getContent().contains("마크다운"),      "CLOB content 값 불일치 — setString/getString 처리 확인");
        assertEquals("/upload/thumb.png", p.getThumbnailUrl(), "thumbnail_url 컬럼 매핑 불일치");
        assertEquals("Y",   p.getIsPublic().trim(),          "is_public CHAR(1) 매핑 불일치");
        assertEquals(0,     p.getViewCount(),                "신규 게시글 view_count는 0이어야 함");
        assertNotNull(p.getCreatedAt(), "created_at 매핑 불일치");
        assertNotNull(p.getUpdatedAt(), "updated_at 매핑 불일치");
        assertNull(p.getDeletedAt(),    "신규 게시글 deleted_at은 null이어야 함");
        // JOIN 집계 필드 검증
        assertNotNull(p.getAuthorName(), "JOIN 별칭 author_name 매핑 실패");
        assertNotNull(p.getSeriesName(), "JOIN 별칭 series_name 매핑 실패 — LEFT JOIN series s 확인");
        assertEquals(0, p.getLikeCount(),    "신규 게시글 like_count는 0이어야 함");
        assertEquals(0, p.getDislikeCount(), "신규 게시글 dislike_count는 0이어야 함");
        assertEquals(0, p.getCommentCount(), "신규 게시글 comment_count는 0이어야 함");
    }

    // ── 10: Post NULL series_id ───────────────────────────────────────────────

    @Test @Order(10)
    @DisplayName("10. PostDao — series_id=null (시리즈 없는 게시글) wasNull 처리")
    void postWithNullSeriesId() {
        int noSeriesPostId = postDao.insert(
                testUserId, null,
                "시리즈없는 글", "내용", null, "Y");
        assertTrue(noSeriesPostId > 0);

        PostDto p = postDao.findById(noSeriesPostId);
        assertNotNull(p);
        assertNull(p.getSeriesId(), "series_id=null 일 때 rs.wasNull() 처리 실패 — Integer가 0으로 반환됨");

        // 이 게시글은 테스트 후 바로 정리
        postDao.hardDelete(noSeriesPostId);
    }

    // ── 11: updateViewCount ──────────────────────────────────────────────────

    @Test @Order(11)
    @DisplayName("11. PostDao.updateViewCount — view_count +1 증가")
    void postUpdateViewCount() {
        postDao.updateViewCount(testPostId);
        postDao.updateViewCount(testPostId);
        PostDto p = postDao.findById(testPostId);
        assertNotNull(p);
        assertEquals(2, p.getViewCount(), "view_count 누적 증가 실패");
    }

    // ── 12: findAll 페이지네이션 ─────────────────────────────────────────────

    @Test @Order(12)
    @DisplayName("12. PostDao.findAll — OFFSET/FETCH NEXT 페이지네이션 문법")
    void postFindAllPagination() {
        int total = postDao.countAll();
        assertTrue(total >= 1, "countAll이 0 반환");

        List<PostDto> page = postDao.findAll(0, 5);
        assertFalse(page.isEmpty(), "findAll 결과 비어 있음 — OFFSET/FETCH NEXT 문법 오류 또는 is_public='Y' 조건 확인");
        page.forEach(p -> assertNotNull(p.getTitle(), "목록 조회 시 title null"));
    }

    // ── 13: search ───────────────────────────────────────────────────────────

    @Test @Order(13)
    @DisplayName("13. PostDao.search — 제목/본문 LIKE 검색")
    void postSearch() {
        List<PostDto> results = postDao.search("테스트 게시글", 0, 10);
        assertFalse(results.isEmpty(), "search 결과 비어 있음 — LIKE 쿼리 또는 content CLOB 검색 확인");
        assertTrue(results.stream().anyMatch(p -> p.getPostId() == testPostId),
                "방금 등록한 게시글이 검색 결과에 없음");
    }

    // ── 14: Tag findOrCreate + replacePostTags ───────────────────────────────

    @Test @Order(14)
    @DisplayName("14. TagDao.findOrCreate + replacePostTags — tags/post_tags 컬럼 매핑")
    void tagOperations() {
        String tagName = TAG_PREFIX + "java";
        int tagId = tagDao.findOrCreate(tagName);
        assertTrue(tagId > 0, "tags_seq.NEXTVAL 채번 실패");

        // 동일 이름 재호출 시 같은 ID 반환 (idempotent)
        assertEquals(tagId, tagDao.findOrCreate(tagName),
                "같은 태그명 재호출 시 다른 ID 반환 — UNIQUE 제약 또는 findByName 쿼리 확인");

        // post_tags 연결 (3개 태그)
        List<String> names = Arrays.asList(
                TAG_PREFIX + "java",
                TAG_PREFIX + "oracle",
                TAG_PREFIX + "servlet");
        tagDao.replacePostTags(testPostId, names);

        List<TagDto> postTags = tagDao.findByPostId(testPostId);
        assertEquals(3, postTags.size(), "post_tags 연결 수 불일치 — FK 또는 INSERT 실패");
        assertTrue(postTags.stream().anyMatch(t -> t.getName().equals(TAG_PREFIX + "java")),
                "태그 name 컬럼 매핑 불일치");

        // 태그 클라우드 전체 조회
        List<TagDto> all = tagDao.findAll();
        assertFalse(all.isEmpty(), "TagDao.findAll() 결과 비어 있음");
        all.forEach(t -> assertTrue(t.getPostCount() >= 0, "post_count 음수"));
    }

    // ── 15: Comment INSERT + findByPostId ────────────────────────────────────

    @Test @Order(15)
    @DisplayName("15. CommentDao.insert + findByPostId — comments 테이블 컬럼 매핑")
    void commentInsertAndFindByPostId() {
        testCommentId = commentDao.insert(testPostId, testUserId, null, "최상위 댓글입니다.");
        assertTrue(testCommentId > 0, "comments_seq.NEXTVAL 채번 실패");

        List<CommentDto> list = commentDao.findByPostId(testPostId);
        assertFalse(list.isEmpty(), "findByPostId 결과 비어 있음");

        CommentDto c = list.stream()
                .filter(x -> x.getCommentId() == testCommentId)
                .findFirst().orElse(null);
        assertNotNull(c, "방금 등록한 댓글이 목록에 없음");
        assertEquals(testPostId, c.getPostId(),  "post_id FK 매핑 불일치");
        assertEquals(testUserId, c.getUserId(),  "user_id FK 매핑 불일치");
        assertNull(c.getParentCommentId(),        "최상위 댓글 parent_comment_id는 null이어야 함 — rs.wasNull() 확인");
        assertEquals("최상위 댓글입니다.", c.getContent(), "content 컬럼 매핑 불일치");
        assertNotNull(c.getAuthorName(), "JOIN 별칭 author_name 매핑 실패");
        assertNotNull(c.getCreatedAt(), "created_at 매핑 불일치");
    }

    // ── 16: Comment 대댓글 (parent_comment_id) ───────────────────────────────

    @Test @Order(16)
    @DisplayName("16. CommentDao — 대댓글 parent_comment_id FK 연결 및 rs.wasNull() 처리")
    void commentReply() {
        testReplyId = commentDao.insert(testPostId, testUserId, testCommentId, "대댓글입니다.");
        assertTrue(testReplyId > 0, "대댓글 INSERT 실패");

        CommentDto reply = commentDao.findById(testReplyId);
        assertNotNull(reply, "대댓글 findById 결과 null");
        assertNotNull(reply.getParentCommentId(),
                "대댓글 parent_comment_id가 null — rs.wasNull() 처리 오류 또는 FK 컬럼명 확인");
        assertEquals(testCommentId, (int) reply.getParentCommentId(),
                "parent_comment_id 값 불일치");
    }

    // ── 17: Comment update ───────────────────────────────────────────────────

    @Test @Order(17)
    @DisplayName("17. CommentDao.update — content/updated_at 업데이트")
    void commentUpdate() {
        commentDao.update(testCommentId, "수정된 댓글 내용");
        CommentDto c = commentDao.findById(testCommentId);
        assertNotNull(c);
        assertEquals("수정된 댓글 내용", c.getContent(), "content 업데이트 실패");
    }

    // ── 18: PostLike 토글 ────────────────────────────────────────────────────

    @Test @Order(18)
    @DisplayName("18. LikeDao (post_likes) — insert/update/delete 토글 및 countPostLikes")
    void postLike() {
        assertNull(likeDao.findPostLike(testPostId, testUserId),
                "초기 like_type은 null이어야 함");

        likeDao.insertPostLike(testPostId, testUserId, "L");
        assertEquals("L", likeDao.findPostLike(testPostId, testUserId),
                "like_type 'L' 저장 실패 — like_type 컬럼 또는 CHAR(1) 확인");

        int[] counts = likeDao.countPostLikes(testPostId);
        assertEquals(1, counts[0], "like_count 불일치");
        assertEquals(0, counts[1], "dislike_count 불일치");

        likeDao.updatePostLike(testPostId, testUserId, "D");
        assertEquals("D", likeDao.findPostLike(testPostId, testUserId),
                "like_type 'L'→'D' 변경 실패");

        likeDao.deletePostLike(testPostId, testUserId);
        assertNull(likeDao.findPostLike(testPostId, testUserId),
                "삭제 후 findPostLike는 null이어야 함");
    }

    // ── 19: CommentLike 토글 ────────────────────────────────────────────────

    @Test @Order(19)
    @DisplayName("19. LikeDao (comment_likes) — insert/count/delete 토글")
    void commentLike() {
        likeDao.insertCommentLike(testCommentId, testUserId, "L");
        assertEquals("L", likeDao.findCommentLike(testCommentId, testUserId),
                "댓글 like_type 'L' 저장 실패");

        int[] counts = likeDao.countCommentLikes(testCommentId);
        assertEquals(1, counts[0], "댓글 like_count 불일치");
        assertEquals(0, counts[1], "댓글 dislike_count 불일치");

        likeDao.deleteCommentLike(testCommentId, testUserId);
        assertNull(likeDao.findCommentLike(testCommentId, testUserId),
                "댓글 좋아요 삭제 후 null이어야 함");
    }

    // ── 20: Report INSERT + findById ─────────────────────────────────────────

    @Test @Order(20)
    @DisplayName("20. ReportDao.insert + findById — reports 테이블 컬럼 매핑")
    void reportInsertAndFindById() {
        testReportId = reportDao.insert(testUserId, "POST", testPostId, "테스트 신고 사유");
        assertTrue(testReportId > 0, "reports_seq.NEXTVAL 채번 실패");

        ReportDto r = reportDao.findById(testReportId);
        assertNotNull(r, "findById 결과 null — report_id 컬럼명 확인");
        assertEquals(testReportId, r.getReportId(),    "report_id 매핑 불일치");
        assertEquals(testUserId,   r.getReporterId(),  "reporter_id FK 매핑 불일치");
        assertEquals("POST",       r.getTargetType(),   "target_type 컬럼 매핑 불일치");
        assertEquals(testPostId,   r.getTargetId(),    "target_id 컬럼 매핑 불일치");
        assertEquals("테스트 신고 사유", r.getReason(), "reason 컬럼 매핑 불일치");
        assertEquals("PENDING",    r.getStatus(),       "status 기본값 'PENDING' 또는 컬럼 매핑 불일치");
        assertNotNull(r.getCreatedAt(), "created_at 매핑 불일치");
        assertNotNull(r.getReporterName(), "JOIN 별칭 reporter_name 매핑 실패");
    }

    // ── 21: Report 중복 감지 + 상태 변경 ─────────────────────────────────────

    @Test @Order(21)
    @DisplayName("21. ReportDao.existsDuplicate + updateStatus + findAll")
    void reportDuplicateAndStatus() {
        assertTrue(reportDao.existsDuplicate(testUserId, "POST", testPostId),
                "동일 신고 중복 감지 실패 — reporter_id/target_type/target_id 쿼리 확인");

        reportDao.updateStatus(testReportId, "RESOLVED");
        assertEquals("RESOLVED", reportDao.findById(testReportId).getStatus(),
                "status 'RESOLVED' 업데이트 실패");

        List<ReportDto> list = reportDao.findAll(null, 0, 10);
        assertFalse(list.isEmpty(), "findAll 결과 비어 있음");

        int pending = reportDao.countAll("PENDING");
        assertTrue(pending >= 0, "countAll(status) 음수 반환");
    }

    // ── 22: Soft Delete 검증 ─────────────────────────────────────────────────

    @Test @Order(22)
    @DisplayName("22. SoftDelete — deleted_at IS NULL 필터가 올바르게 동작하는지 검증")
    void softDelete() {
        // 댓글 소프트 삭제 → findById null
        commentDao.softDelete(testCommentId);
        assertNull(commentDao.findById(testCommentId),
                "소프트 삭제된 댓글이 findById에서 반환됨 — deleted_at IS NULL 조건 확인");

        // 게시글 소프트 삭제 → findById null
        postDao.softDelete(testPostId);
        assertNull(postDao.findById(testPostId),
                "소프트 삭제된 게시글이 findById에서 반환됨 — deleted_at IS NULL 조건 확인");

        // 사용자 소프트 삭제 → findById null
        userDao.softDelete(testUserId);
        assertNull(userDao.findById(testUserId),
                "소프트 삭제된 사용자가 findById에서 반환됨 — deleted_at IS NULL 조건 확인");
    }

    // ── 정리: 하드 삭제 (FK 역순) ───────────────────────────────────────────

    @AfterAll
    static void cleanUp() {
        // 1. reports (reporter_id → users FK 때문에 users 삭제 전에)
        deleteById("DELETE FROM reports WHERE report_id = ?", testReportId);

        // 2. 대댓글(child) → 부모 댓글(parent) 순서 삭제 (self-referencing FK: no cascade)
        deleteById("DELETE FROM comments WHERE comment_id = ?", testReplyId);
        deleteById("DELETE FROM comments WHERE comment_id = ?", testCommentId);

        // 3. posts — ON DELETE CASCADE로 post_likes, post_tags 자동 삭제
        deleteById("DELETE FROM posts WHERE post_id = ?", testPostId);

        // 4. series
        deleteById("DELETE FROM series WHERE series_id = ?", testSeriesId);

        // 5. tags (post_tags는 post 삭제 시 CASCADE 완료됨)
        for (String suffix : new String[]{"java", "oracle", "servlet"}) {
            String tagName = TAG_PREFIX + suffix;
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM tags WHERE name = ?")) {
                ps.setString(1, tagName);
                ps.executeUpdate();
            } catch (Exception ignored) {}
        }

        // 6. users (마지막)
        deleteById("DELETE FROM users WHERE user_id = ?", testUserId);
    }

    private static void deleteById(String sql, int id) {
        if (id <= 0) return;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }
}
