package com.acorncampus_studylog.dao;

import com.acorncampus_studylog.dto.TagDto;
import com.acorncampus_studylog.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** tags / post_tags 테이블 CRUD — 태그 클라우드, 게시글 태그 관리 */
public class TagDao {

    // ── 조회 ────────────────────────────────────────────────────────────────

    /** 전체 태그 목록 (공개 게시글 사용 수 포함, 내림차순) — 태그 클라우드용 */
    public List<TagDto> findAll() {
        String sql =
            "SELECT t.tag_id, t.name, " +
            "       NVL((SELECT COUNT(*) FROM post_tags pt " +
            "            JOIN posts p ON pt.post_id = p.post_id " +
            "            WHERE pt.tag_id = t.tag_id AND p.deleted_at IS NULL AND p.is_public = 'Y'), 0) AS post_count " +
            "FROM tags t ORDER BY post_count DESC, t.name ASC";
        List<TagDto> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                TagDto t = new TagDto(rs.getInt("tag_id"), rs.getString("name"));
                t.setPostCount(rs.getInt("post_count"));
                list.add(t);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("TagDao.findAll 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 태그명으로 단건 조회 */
    public TagDto findByName(String name) {
        String sql = "SELECT tag_id, name FROM tags WHERE name = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            rs = pstmt.executeQuery();
            if (rs.next()) return new TagDto(rs.getInt("tag_id"), rs.getString("name"));
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("TagDao.findByName 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 게시글에 연결된 태그 목록 조회 */
    public List<TagDto> findByPostId(int postId) {
        String sql =
            "SELECT t.tag_id, t.name FROM tags t " +
            "JOIN post_tags pt ON t.tag_id = pt.tag_id " +
            "WHERE pt.post_id = ? ORDER BY t.name";
        List<TagDto> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, postId);
            rs = pstmt.executeQuery();
            while (rs.next()) list.add(new TagDto(rs.getInt("tag_id"), rs.getString("name")));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("TagDao.findByPostId 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 관리자 - 전체 태그 수 */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM tags";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("TagDao.countAll 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    // ── 삽입 / 수정 / 삭제 ─────────────────────────────────────────────────

    /**
     * 태그가 없으면 생성하고 tag_id 반환, 이미 있으면 기존 tag_id 반환
     * 게시글 저장/수정 시 사용 — replacePostTags에서 내부 호출
     */
    public int findOrCreate(String name) {
        // 먼저 조회
        TagDto existing = findByName(name);
        if (existing != null) return existing.getTagId();

        // 없으면 삽입
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT tags_seq.NEXTVAL FROM DUAL");
            rs = pstmt.executeQuery();
            rs.next();
            int newId = rs.getInt(1);
            rs.close(); rs = null;
            pstmt.close(); pstmt = null;

            pstmt = conn.prepareStatement("INSERT INTO tags (tag_id, name) VALUES (?, ?)");
            pstmt.setInt(1, newId);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
            return newId;
        } catch (SQLException e) {
            // 동시 삽입으로 UNIQUE 위반 시 재조회 (레이스 컨디션 방어)
            if (e.getErrorCode() == 1) {
                TagDto retry = findByName(name);
                if (retry != null) return retry.getTagId();
            }
            throw new RuntimeException("TagDao.findOrCreate 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /**
     * 게시글 태그를 새 목록으로 교체 (트랜잭션 처리)
     * 기존 연결 삭제 → 새 태그 findOrCreate → post_tags 삽입
     */
    public void replacePostTags(int postId, List<String> tagNames) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 기존 태그 연결 삭제
            PreparedStatement delStmt = conn.prepareStatement("DELETE FROM post_tags WHERE post_id = ?");
            delStmt.setInt(1, postId);
            delStmt.executeUpdate();
            delStmt.close();

            // 새 태그 연결 삽입 (최대 5개 제한은 Service 레이어에서 검증)
            for (String tagName : tagNames) {
                if (tagName == null || tagName.trim().isEmpty()) continue;
                int tagId = findOrCreate(tagName.trim());
                PreparedStatement insStmt = conn.prepareStatement(
                    "INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)"
                );
                insStmt.setInt(1, postId);
                insStmt.setInt(2, tagId);
                insStmt.executeUpdate();
                insStmt.close();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ignored) {} }
            throw new RuntimeException("TagDao.replacePostTags 실패", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /** 관리자 - 태그 삭제 (post_tags CASCADE 삭제 SQL 스키마에 정의됨) */
    public void deleteById(int tagId) {
        String sql = "DELETE FROM tags WHERE tag_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, tagId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("TagDao.deleteById 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }
}
