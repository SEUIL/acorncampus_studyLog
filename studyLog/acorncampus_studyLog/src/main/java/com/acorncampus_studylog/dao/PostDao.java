package com.acorncampus_studylog.dao;

import com.acorncampus_studylog.dto.PostDto;
import com.acorncampus_studylog.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** posts 테이블 CRUD — 모든 쿼리는 PreparedStatement 사용, 소프트 삭제 적용 */
public class PostDao {

    // ── 조회 ────────────────────────────────────────────────────────────────

    /**
     * 게시글 단건 상세 조회 (작성자, 시리즈명, 좋아요/댓글 수 포함)
     * deleted_at IS NULL 조건 포함 — 삭제된 글은 null 반환
     */
    public PostDto findById(int postId) {
        String sql =
            "SELECT p.post_id, p.user_id, p.series_id, p.title, p.content, p.thumbnail_url, " +
            "       p.is_public, p.view_count, p.created_at, p.updated_at, p.deleted_at, " +
            "       u.nickname AS author_name, s.name AS series_name, " +
            "       NVL((SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id AND like_type = 'L'), 0) AS like_count, " +
            "       NVL((SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id AND like_type = 'D'), 0) AS dislike_count, " +
            "       NVL((SELECT COUNT(*) FROM comments WHERE post_id = p.post_id AND deleted_at IS NULL), 0) AS comment_count " +
            "FROM posts p " +
            "JOIN users u ON p.user_id = u.user_id " +
            "LEFT JOIN series s ON p.series_id = s.series_id " +
            "WHERE p.post_id = ? AND p.deleted_at IS NULL";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, postId);
            rs = pstmt.executeQuery();
            return rs.next() ? mapRow(rs, true) : null;
        } catch (SQLException e) {
            throw new RuntimeException("PostDao.findById 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 공개 게시글 목록 (최신순, 페이지네이션) */
    public List<PostDto> findAll(int offset, int limit) {
        String sql =
            "SELECT p.post_id, p.user_id, p.series_id, p.title, p.thumbnail_url, p.is_public, " +
            "       p.view_count, p.created_at, p.updated_at, u.nickname AS author_name, " +
            "       NVL((SELECT COUNT(*) FROM comments WHERE post_id = p.post_id AND deleted_at IS NULL), 0) AS comment_count " +
            "FROM posts p JOIN users u ON p.user_id = u.user_id " +
            "WHERE p.deleted_at IS NULL AND p.is_public = 'Y' " +
            "ORDER BY p.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return queryList(sql, offset, limit);
    }

    /** 관리자 - 전체 게시글 목록 (공개/비공개 포함, 페이지네이션) */
    public List<PostDto> findAllForAdmin(int offset, int limit) {
        String sql =
            "SELECT p.post_id, p.user_id, p.series_id, p.title, p.thumbnail_url, p.is_public, " +
            "       p.view_count, p.created_at, p.updated_at, p.deleted_at, u.nickname AS author_name, " +
            "       NVL((SELECT COUNT(*) FROM comments WHERE post_id = p.post_id AND deleted_at IS NULL), 0) AS comment_count " +
            "FROM posts p JOIN users u ON p.user_id = u.user_id " +
            "WHERE p.deleted_at IS NULL " +
            "ORDER BY p.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return queryList(sql, offset, limit);
    }

    public List<PostDto> findAllForAdmin(String keyword, String status, int offset, int limit) {
        String normalizedKeyword = normalizeKeyword(keyword);
        StringBuilder sql = new StringBuilder()
            .append("SELECT p.post_id, p.user_id, p.series_id, p.title, p.thumbnail_url, p.is_public, ")
            .append("       p.view_count, p.created_at, p.updated_at, p.deleted_at, u.nickname AS author_name, ")
            .append("       NVL((SELECT COUNT(*) FROM comments WHERE post_id = p.post_id AND deleted_at IS NULL), 0) AS comment_count ")
            .append("FROM posts p JOIN users u ON p.user_id = u.user_id ")
            .append("WHERE p.deleted_at IS NULL ");
        appendAdminPostFilters(sql, normalizedKeyword, status);
        sql.append("ORDER BY p.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        List<PostDto> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int idx = bindAdminPostFilters(pstmt, normalizedKeyword, status, 1);
            pstmt.setInt(idx++, offset);
            pstmt.setInt(idx, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs, false));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("PostDao.findAllForAdmin 검색 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 특정 사용자의 게시글 목록 (마이페이지 — 비공개 포함, 페이지네이션) */
    public List<PostDto> findByUserId(int userId, int offset, int limit) {
        String sql =
            "SELECT p.post_id, p.user_id, p.series_id, p.title, p.thumbnail_url, p.is_public, " +
            "       p.view_count, p.created_at, p.updated_at, u.nickname AS author_name " +
            "FROM posts p JOIN users u ON p.user_id = u.user_id " +
            "WHERE p.user_id = ? AND p.deleted_at IS NULL " +
            "ORDER BY p.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<PostDto> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, offset);
            pstmt.setInt(3, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs, false));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("PostDao.findByUserId 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 태그명으로 공개 게시글 목록 조회 (페이지네이션) */
    public List<PostDto> findByTag(String tagName, int offset, int limit) {
        String sql =
            "SELECT p.post_id, p.user_id, p.title, p.thumbnail_url, p.is_public, p.view_count, p.created_at, " +
            "       u.nickname AS author_name " +
            "FROM posts p " +
            "JOIN users u ON p.user_id = u.user_id " +
            "JOIN post_tags pt ON p.post_id = pt.post_id " +
            "JOIN tags t ON pt.tag_id = t.tag_id " +
            "WHERE t.name = ? AND p.deleted_at IS NULL AND p.is_public = 'Y' " +
            "ORDER BY p.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<PostDto> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, tagName);
            pstmt.setInt(2, offset);
            pstmt.setInt(3, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs, false));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("PostDao.findByTag 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 제목 + 본문 키워드 검색 (공개 게시글, 페이지네이션) */
    public List<PostDto> search(String keyword, int offset, int limit) {
        String like = "%" + keyword + "%";
        String sql =
            "SELECT p.post_id, p.user_id, p.title, p.thumbnail_url, p.is_public, p.view_count, p.created_at, " +
            "       u.nickname AS author_name " +
            "FROM posts p JOIN users u ON p.user_id = u.user_id " +
            "WHERE p.deleted_at IS NULL AND p.is_public = 'Y' " +
            "AND (LOWER(p.title) LIKE LOWER(?) OR LOWER(p.content) LIKE LOWER(?)) " +
            "ORDER BY p.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<PostDto> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, like);
            pstmt.setString(2, like);
            pstmt.setInt(3, offset);
            pstmt.setInt(4, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs, false));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("PostDao.search 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    // ── 카운트 ──────────────────────────────────────────────────────────────

    /** 공개 게시글 전체 수 */
    public int countAll() {
        return countBySql("SELECT COUNT(*) FROM posts WHERE deleted_at IS NULL AND is_public = 'Y'");
    }

    /** 전체 게시글 수 (관리자용, 비공개 포함) */
    public int countAllForAdmin() {
        return countBySql("SELECT COUNT(*) FROM posts WHERE deleted_at IS NULL");
    }

    public int countAllForAdmin(String keyword, String status) {
        String normalizedKeyword = normalizeKeyword(keyword);
        StringBuilder sql = new StringBuilder()
            .append("SELECT COUNT(*) FROM posts p JOIN users u ON p.user_id = u.user_id ")
            .append("WHERE p.deleted_at IS NULL ");
        appendAdminPostFilters(sql, normalizedKeyword, status);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            bindAdminPostFilters(pstmt, normalizedKeyword, status, 1);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("PostDao.countAllForAdmin 검색 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 특정 사용자의 게시글 수 */
    public int countByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM posts WHERE user_id = ? AND deleted_at IS NULL";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("PostDao.countByUserId 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 태그별 공개 게시글 수 */
    public int countByTag(String tagName) {
        String sql = "SELECT COUNT(*) FROM posts p " +
                     "JOIN post_tags pt ON p.post_id = pt.post_id " +
                     "JOIN tags t ON pt.tag_id = t.tag_id " +
                     "WHERE t.name = ? AND p.deleted_at IS NULL AND p.is_public = 'Y'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, tagName);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("PostDao.countByTag 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 검색 결과 게시글 수 */
    public int countSearch(String keyword) {
        String like = "%" + keyword + "%";
        String sql = "SELECT COUNT(*) FROM posts WHERE deleted_at IS NULL AND is_public = 'Y' " +
                     "AND (LOWER(title) LIKE LOWER(?) OR LOWER(content) LIKE LOWER(?))";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, like);
            pstmt.setString(2, like);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("PostDao.countSearch 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    // ── 삽입 / 수정 / 삭제 ─────────────────────────────────────────────────

    /** 게시글 등록 — 생성된 post_id 반환 */
    public int insert(int userId, Integer seriesId, String title, String content,
                      String thumbnailUrl, String isPublic) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();

            pstmt = conn.prepareStatement("SELECT posts_seq.NEXTVAL FROM DUAL");
            rs = pstmt.executeQuery();
            rs.next();
            int newId = rs.getInt(1);
            rs.close(); rs = null;
            pstmt.close(); pstmt = null;

            pstmt = conn.prepareStatement(
                "INSERT INTO posts (post_id, user_id, series_id, title, content, thumbnail_url, is_public, view_count, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 0, SYSTIMESTAMP, SYSTIMESTAMP)"
            );
            pstmt.setInt(1, newId);
            pstmt.setInt(2, userId);
            if (seriesId != null) pstmt.setInt(3, seriesId); else pstmt.setNull(3, Types.NUMERIC);
            pstmt.setString(4, title);
            pstmt.setString(5, content);   // CLOB은 setString으로 처리 (ojdbc8 지원)
            pstmt.setString(6, thumbnailUrl);
            pstmt.setString(7, isPublic);
            pstmt.executeUpdate();
            return newId;
        } catch (SQLException e) {
            throw new RuntimeException("PostDao.insert 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 게시글 수정 */
    public void update(int postId, Integer seriesId, String title, String content,
                       String thumbnailUrl, String isPublic) {
        String sql = "UPDATE posts SET series_id = ?, title = ?, content = ?, thumbnail_url = ?, " +
                     "is_public = ?, updated_at = SYSTIMESTAMP WHERE post_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            if (seriesId != null) pstmt.setInt(1, seriesId); else pstmt.setNull(1, Types.NUMERIC);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.setString(4, thumbnailUrl);
            pstmt.setString(5, isPublic);
            pstmt.setInt(6, postId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("PostDao.update 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 조회수 1 증가 */
    public void updateViewCount(int postId) {
        String sql = "UPDATE posts SET view_count = view_count + 1 WHERE post_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, postId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("PostDao.updateViewCount 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 게시글 소프트 삭제 (deleted_at 설정) */
    public void softDelete(int postId) {
        String sql = "UPDATE posts SET deleted_at = SYSTIMESTAMP WHERE post_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, postId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("PostDao.softDelete 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 관리자 - 게시글 하드 삭제 */
    public void hardDelete(int postId) {
        String sql = "DELETE FROM posts WHERE post_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, postId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("PostDao.hardDelete 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    // ── 내부 헬퍼 ───────────────────────────────────────────────────────────

    /** offset/limit 파라미터만 있는 목록 쿼리 공통 처리 */
    private List<PostDto> queryList(String sql, int offset, int limit) {
        List<PostDto> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, offset);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs, false));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("PostDao 목록 조회 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** COUNT(*) 단일 값 조회 공통 처리 */
    private int countBySql(String sql) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("PostDao COUNT 조회 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /**
     * ResultSet 한 행 → PostDto 변환
     * @param withDetail true이면 like_count, dislike_count, comment_count 포함
     */
    private void appendAdminPostFilters(StringBuilder sql, String keyword, String status) {
        if (keyword != null) {
            // 게시글 관리 검색창 기준: 제목 또는 작성자 닉네임으로 검색한다.
            sql.append("AND (LOWER(p.title) LIKE LOWER(?) OR LOWER(u.nickname) LIKE LOWER(?)) ");
        }
        if (status != null) {
            sql.append("AND p.is_public = ? ");
        }
    }

    private int bindAdminPostFilters(PreparedStatement pstmt, String keyword, String status, int startIndex)
            throws SQLException {
        int idx = startIndex;
        if (keyword != null) {
            String like = "%" + keyword + "%";
            pstmt.setString(idx++, like);
            pstmt.setString(idx++, like);
        }
        if (status != null) {
            pstmt.setString(idx++, status);
        }
        return idx;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private PostDto mapRow(ResultSet rs, boolean withDetail) throws SQLException {
        PostDto p = new PostDto();
        p.setPostId(rs.getInt("post_id"));
        p.setUserId(rs.getInt("user_id"));
        try { int sid = rs.getInt("series_id"); p.setSeriesId(rs.wasNull() ? null : sid); } catch (SQLException ignored) {}
        p.setTitle(rs.getString("title"));
        if (withDetail) p.setContent(rs.getString("content"));
        p.setThumbnailUrl(rs.getString("thumbnail_url"));
        p.setIsPublic(rs.getString("is_public"));
        p.setViewCount(rs.getInt("view_count"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        try { p.setUpdatedAt(rs.getTimestamp("updated_at")); } catch (SQLException ignored) {}
        try { p.setDeletedAt(rs.getTimestamp("deleted_at")); } catch (SQLException ignored) {}
        try { p.setAuthorName(rs.getString("author_name")); } catch (SQLException ignored) {}
        try { p.setSeriesName(rs.getString("series_name")); } catch (SQLException ignored) {}
        if (withDetail) {
            p.setLikeCount(rs.getInt("like_count"));
            p.setDislikeCount(rs.getInt("dislike_count"));
            p.setCommentCount(rs.getInt("comment_count"));
        } else {
            try { p.setCommentCount(rs.getInt("comment_count")); } catch (SQLException ignored) {}
        }
        return p;
    }
}
