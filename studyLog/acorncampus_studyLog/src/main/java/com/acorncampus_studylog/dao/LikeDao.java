package com.acorncampus_studylog.dao;

import com.acorncampus_studylog.util.DBUtil;

import java.sql.*;

/** post_likes / comment_likes 테이블 CRUD — 좋아요/싫어요 토글 처리 */
public class LikeDao {

    // ── 게시글 좋아요/싫어요 ─────────────────────────────────────────────────

    /**
     * 특정 사용자의 게시글 좋아요 타입 조회
     * @return "L"(좋아요), "D"(싫어요), null(반응 없음)
     */
    public String findPostLike(int postId, int userId) {
        String sql = "SELECT like_type FROM post_likes WHERE post_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("like_type") : null;
        } catch (SQLException e) {
            throw new RuntimeException("LikeDao.findPostLike 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /**
     * 게시글 좋아요/싫어요 수 조회
     * @return int[0] = like_count, int[1] = dislike_count
     */
    public int[] countPostLikes(int postId) {
        String sql = "SELECT " +
                     "NVL(SUM(CASE WHEN like_type = 'L' THEN 1 ELSE 0 END), 0) AS like_count, " +
                     "NVL(SUM(CASE WHEN like_type = 'D' THEN 1 ELSE 0 END), 0) AS dislike_count " +
                     "FROM post_likes WHERE post_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, postId);
            rs = pstmt.executeQuery();
            if (rs.next()) return new int[]{rs.getInt("like_count"), rs.getInt("dislike_count")};
            return new int[]{0, 0};
        } catch (SQLException e) {
            throw new RuntimeException("LikeDao.countPostLikes 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 게시글 좋아요/싫어요 등록 */
    public void insertPostLike(int postId, int userId, String likeType) {
        String sql = "INSERT INTO post_likes (post_id, user_id, like_type) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, likeType);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("LikeDao.insertPostLike 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 게시글 좋아요/싫어요 타입 변경 (L→D 또는 D→L) */
    public void updatePostLike(int postId, int userId, String likeType) {
        String sql = "UPDATE post_likes SET like_type = ? WHERE post_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, likeType);
            pstmt.setInt(2, postId);
            pstmt.setInt(3, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("LikeDao.updatePostLike 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 게시글 좋아요/싫어요 취소 (동일 타입 재클릭) */
    public void deletePostLike(int postId, int userId) {
        String sql = "DELETE FROM post_likes WHERE post_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("LikeDao.deletePostLike 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    // ── 댓글 좋아요/싫어요 ──────────────────────────────────────────────────

    /**
     * 특정 사용자의 댓글 좋아요 타입 조회
     * @return "L"(좋아요), "D"(싫어요), null(반응 없음)
     */
    public String findCommentLike(int commentId, int userId) {
        String sql = "SELECT like_type FROM comment_likes WHERE comment_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, commentId);
            pstmt.setInt(2, userId);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("like_type") : null;
        } catch (SQLException e) {
            throw new RuntimeException("LikeDao.findCommentLike 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /**
     * 댓글 좋아요/싫어요 수 조회
     * @return int[0] = like_count, int[1] = dislike_count
     */
    public int[] countCommentLikes(int commentId) {
        String sql = "SELECT " +
                     "NVL(SUM(CASE WHEN like_type = 'L' THEN 1 ELSE 0 END), 0) AS like_count, " +
                     "NVL(SUM(CASE WHEN like_type = 'D' THEN 1 ELSE 0 END), 0) AS dislike_count " +
                     "FROM comment_likes WHERE comment_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, commentId);
            rs = pstmt.executeQuery();
            if (rs.next()) return new int[]{rs.getInt("like_count"), rs.getInt("dislike_count")};
            return new int[]{0, 0};
        } catch (SQLException e) {
            throw new RuntimeException("LikeDao.countCommentLikes 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 댓글 좋아요/싫어요 등록 */
    public void insertCommentLike(int commentId, int userId, String likeType) {
        String sql = "INSERT INTO comment_likes (comment_id, user_id, like_type) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, commentId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, likeType);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("LikeDao.insertCommentLike 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 댓글 좋아요/싫어요 타입 변경 */
    public void updateCommentLike(int commentId, int userId, String likeType) {
        String sql = "UPDATE comment_likes SET like_type = ? WHERE comment_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, likeType);
            pstmt.setInt(2, commentId);
            pstmt.setInt(3, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("LikeDao.updateCommentLike 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 댓글 좋아요/싫어요 취소 */
    public void deleteCommentLike(int commentId, int userId) {
        String sql = "DELETE FROM comment_likes WHERE comment_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, commentId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("LikeDao.deleteCommentLike 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }
}
