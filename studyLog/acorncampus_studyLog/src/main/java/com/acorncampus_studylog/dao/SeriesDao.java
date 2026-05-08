package com.acorncampus_studylog.dao;

import com.acorncampus_studylog.dto.PostDto;
import com.acorncampus_studylog.dto.SeriesDto;
import com.acorncampus_studylog.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** series 테이블 CRUD */
public class SeriesDao {

    // ── 조회 ────────────────────────────────────────────────────────────────

    /** series_id로 시리즈 단건 조회 (작성자명, 게시글 수 포함) */
    public SeriesDto findById(int seriesId) {
        String sql =
            "SELECT s.series_id, s.user_id, s.name, s.description, s.is_public, s.created_at, " +
            "       u.nickname AS author_name, " +
            "       NVL((SELECT COUNT(*) FROM posts WHERE series_id = s.series_id AND deleted_at IS NULL), 0) AS post_count " +
            "FROM series s JOIN users u ON s.user_id = u.user_id " +
            "WHERE s.series_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, seriesId);
            rs = pstmt.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException("SeriesDao.findById 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 특정 사용자의 시리즈 목록 (마이페이지/프로필) */
    public List<SeriesDto> findByUserId(int userId) {
        String sql =
            "SELECT s.series_id, s.user_id, s.name, s.description, s.is_public, s.created_at, " +
            "       u.nickname AS author_name, " +
            "       NVL((SELECT COUNT(*) FROM posts WHERE series_id = s.series_id AND deleted_at IS NULL), 0) AS post_count " +
            "FROM series s JOIN users u ON s.user_id = u.user_id " +
            "WHERE s.user_id = ? ORDER BY s.created_at DESC";
        List<SeriesDto> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("SeriesDao.findByUserId 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 공개 시리즈 전체 목록 (페이지네이션) */
    public List<SeriesDto> findAll(int offset, int limit) {
        String sql =
            "SELECT s.series_id, s.user_id, s.name, s.description, s.is_public, s.created_at, " +
            "       u.nickname AS author_name, u.avatar_url AS author_avatar_url, " +
            "       NVL((SELECT COUNT(*) FROM posts WHERE series_id = s.series_id AND deleted_at IS NULL), 0) AS post_count " +
            "FROM series s JOIN users u ON s.user_id = u.user_id " +
            "WHERE s.is_public = 'Y' ORDER BY s.created_at DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<SeriesDto> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, offset);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("SeriesDao.findAll 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /**
     * 시리즈에 포함된 게시글 목록 (시리즈 상세 페이지용, 공개 순서)
     * @return 게시글 기본 정보 목록 (content 제외)
     */
    public List<PostDto> findPostsBySeries(int seriesId) {
        String sql =
            "SELECT p.post_id, p.user_id, p.series_id, p.title, p.is_public, p.view_count, p.created_at, " +
            "       u.nickname AS author_name " +
            "FROM posts p JOIN users u ON p.user_id = u.user_id " +
            "WHERE p.series_id = ? AND p.deleted_at IS NULL " +
            "ORDER BY p.created_at ASC";
        List<PostDto> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, seriesId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                PostDto p = new PostDto();
                p.setPostId(rs.getInt("post_id"));
                p.setUserId(rs.getInt("user_id"));
                p.setSeriesId(seriesId);
                p.setTitle(rs.getString("title"));
                p.setIsPublic(rs.getString("is_public"));
                p.setViewCount(rs.getInt("view_count"));
                p.setCreatedAt(rs.getTimestamp("created_at"));
                p.setAuthorName(rs.getString("author_name"));
                list.add(p);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("SeriesDao.findPostsBySeries 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 공개 시리즈 전체 수 */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM series WHERE is_public = 'Y'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("SeriesDao.countAll 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    // ── 삽입 / 수정 / 삭제 ─────────────────────────────────────────────────

    /** 시리즈 생성 — 생성된 series_id 반환 */
    public int insert(int userId, String name, String description, String isPublic) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();

            pstmt = conn.prepareStatement("SELECT series_seq.NEXTVAL FROM DUAL");
            rs = pstmt.executeQuery();
            rs.next();
            int newId = rs.getInt(1);
            rs.close(); rs = null;
            pstmt.close(); pstmt = null;

            pstmt = conn.prepareStatement(
                "INSERT INTO series (series_id, user_id, name, description, is_public, created_at) " +
                "VALUES (?, ?, ?, ?, ?, SYSTIMESTAMP)"
            );
            pstmt.setInt(1, newId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, name);
            pstmt.setString(4, description);
            pstmt.setString(5, isPublic);
            pstmt.executeUpdate();
            return newId;
        } catch (SQLException e) {
            throw new RuntimeException("SeriesDao.insert 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 시리즈 정보 수정 */
    public void update(int seriesId, String name, String description, String isPublic) {
        String sql = "UPDATE series SET name = ?, description = ?, is_public = ? WHERE series_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setString(3, isPublic);
            pstmt.setInt(4, seriesId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SeriesDao.update 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 시리즈 삭제 (소속 게시글의 series_id는 NULL로 초기화 후 삭제) */
    public void delete(int seriesId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 소속 게시글 시리즈 연결 해제
            pstmt = conn.prepareStatement("UPDATE posts SET series_id = NULL WHERE series_id = ?");
            pstmt.setInt(1, seriesId);
            pstmt.executeUpdate();
            pstmt.close();

            // 시리즈 삭제
            pstmt = conn.prepareStatement("DELETE FROM series WHERE series_id = ?");
            pstmt.setInt(1, seriesId);
            pstmt.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ignored) {} }
            throw new RuntimeException("SeriesDao.delete 실패", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // ── 내부 헬퍼 ───────────────────────────────────────────────────────────

    private SeriesDto mapRow(ResultSet rs) throws SQLException {
        SeriesDto s = new SeriesDto();
        s.setSeriesId(rs.getInt("series_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setName(rs.getString("name"));
        s.setDescription(rs.getString("description"));
        s.setIsPublic(rs.getString("is_public"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        try { s.setAuthorName(rs.getString("author_name")); }          catch (SQLException ignored) {}
        try { s.setAuthorAvatarUrl(rs.getString("author_avatar_url")); } catch (SQLException ignored) {}
        try { s.setPostCount(rs.getInt("post_count")); }               catch (SQLException ignored) {}
        return s;
    }
}
