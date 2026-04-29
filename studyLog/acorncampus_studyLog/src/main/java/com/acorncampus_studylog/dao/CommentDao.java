package com.acorncampus_studylog.dao;

import com.acorncampus_studylog.dto.CommentDto;
import com.acorncampus_studylog.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** comments 테이블 CRUD — 2depth 댓글/대댓글 지원, 소프트 삭제 적용 */
public class CommentDao {

    // ── 조회 ────────────────────────────────────────────────────────────────

    /**
     * 게시글의 댓글 전체 조회 (삭제되지 않은 것만, 작성 시간 오름차순)
     * 반환 목록은 평탄(flat) 리스트 — Service에서 부모/자식 구조로 조립
     */
    public List<CommentDto> findByPostId(int postId) {
        String sql =
            "SELECT c.comment_id, c.post_id, c.user_id, c.parent_comment_id, c.content, " +
            "       c.created_at, c.updated_at, c.deleted_at, u.nickname AS author_name, " +
            "       NVL((SELECT COUNT(*) FROM comment_likes WHERE comment_id = c.comment_id AND like_type = 'L'), 0) AS like_count, " +
            "       NVL((SELECT COUNT(*) FROM comment_likes WHERE comment_id = c.comment_id AND like_type = 'D'), 0) AS dislike_count " +
            "FROM comments c " +
            "JOIN users u ON c.user_id = u.user_id " +
            "WHERE c.post_id = ? AND c.deleted_at IS NULL " +
            "ORDER BY c.created_at ASC";
        List<CommentDto> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, postId);
            rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("CommentDao.findByPostId 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 댓글 단건 조회 (수정/삭제 권한 확인용) */
    public CommentDto findById(int commentId) {
        String sql =
            "SELECT c.comment_id, c.post_id, c.user_id, c.parent_comment_id, c.content, " +
            "       c.created_at, c.updated_at, c.deleted_at, u.nickname AS author_name " +
            "FROM comments c " +
            "JOIN users u ON c.user_id = u.user_id " +
            "WHERE c.comment_id = ? AND c.deleted_at IS NULL";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, commentId);
            rs = pstmt.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException("CommentDao.findById 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 관리자 - 전체 댓글 목록 (페이지네이션) */
    public List<CommentDto> findAllForAdmin(int offset, int limit) {
        String sql =
            "SELECT c.comment_id, c.post_id, c.user_id, c.parent_comment_id, c.content, " +
            "       c.created_at, c.updated_at, c.deleted_at, u.nickname AS author_name " +
            "FROM comments c JOIN users u ON c.user_id = u.user_id " +
            "WHERE c.deleted_at IS NULL " +
            "ORDER BY c.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<CommentDto> list = new ArrayList<>();
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
            throw new RuntimeException("CommentDao.findAllForAdmin 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 게시글의 댓글 수 (삭제되지 않은 것만) */
    public int countByPostId(int postId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE post_id = ? AND deleted_at IS NULL";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, postId);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("CommentDao.countByPostId 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 관리자 - 전체 댓글 수 */
    public int countAllForAdmin() {
        String sql = "SELECT COUNT(*) FROM comments WHERE deleted_at IS NULL";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("CommentDao.countAllForAdmin 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    // ── 삽입 / 수정 / 삭제 ─────────────────────────────────────────────────

    /**
     * 댓글/대댓글 등록 — 생성된 comment_id 반환
     * @param parentCommentId 최상위 댓글이면 null
     */
    public int insert(int postId, int userId, Integer parentCommentId, String content) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();

            pstmt = conn.prepareStatement("SELECT comments_seq.NEXTVAL FROM DUAL");
            rs = pstmt.executeQuery();
            rs.next();
            int newId = rs.getInt(1);
            rs.close(); rs = null;
            pstmt.close(); pstmt = null;

            pstmt = conn.prepareStatement(
                "INSERT INTO comments (comment_id, post_id, user_id, parent_comment_id, content, created_at) " +
                "VALUES (?, ?, ?, ?, ?, SYSTIMESTAMP)"
            );
            pstmt.setInt(1, newId);
            pstmt.setInt(2, postId);
            pstmt.setInt(3, userId);
            if (parentCommentId != null) pstmt.setInt(4, parentCommentId);
            else pstmt.setNull(4, Types.NUMERIC);
            pstmt.setString(5, content);
            pstmt.executeUpdate();
            return newId;
        } catch (SQLException e) {
            throw new RuntimeException("CommentDao.insert 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 댓글 내용 수정 */
    public void update(int commentId, String content) {
        String sql = "UPDATE comments SET content = ?, updated_at = SYSTIMESTAMP WHERE comment_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, content);
            pstmt.setInt(2, commentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CommentDao.update 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 댓글 소프트 삭제 */
    public void softDelete(int commentId) {
        String sql = "UPDATE comments SET deleted_at = SYSTIMESTAMP WHERE comment_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, commentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CommentDao.softDelete 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 관리자 - 댓글 하드 삭제 */
    public void hardDelete(int commentId) {
        String sql = "DELETE FROM comments WHERE comment_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, commentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CommentDao.hardDelete 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    // ── 내부 헬퍼 ───────────────────────────────────────────────────────────

    private CommentDto mapRow(ResultSet rs) throws SQLException {
        CommentDto c = new CommentDto();
        c.setCommentId(rs.getInt("comment_id"));
        c.setPostId(rs.getInt("post_id"));
        c.setUserId(rs.getInt("user_id"));
        int pid = rs.getInt("parent_comment_id");
        c.setParentCommentId(rs.wasNull() ? null : pid);
        c.setContent(rs.getString("content"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        try { c.setUpdatedAt(rs.getTimestamp("updated_at")); } catch (SQLException ignored) {}
        try { c.setDeletedAt(rs.getTimestamp("deleted_at")); } catch (SQLException ignored) {}
        try { c.setAuthorName(rs.getString("author_name")); } catch (SQLException ignored) {}
        try { c.setLikeCount(rs.getInt("like_count")); } catch (SQLException ignored) {}
        try { c.setDislikeCount(rs.getInt("dislike_count")); } catch (SQLException ignored) {}
        return c;
    }
}
