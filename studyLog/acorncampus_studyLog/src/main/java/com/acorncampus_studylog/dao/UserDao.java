package com.acorncampus_studylog.dao;

import com.acorncampus_studylog.dto.UserDetailDto;
import com.acorncampus_studylog.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** users 테이블 CRUD — 모든 쿼리는 PreparedStatement 사용 */
public class UserDao {

    // ── 조회 ────────────────────────────────────────────────────────────────

    /** 이메일로 사용자 조회 (비밀번호 포함, 로그인 검증용) */
    public UserDetailDto findByEmail(String email) {
        String sql = "SELECT user_id, email, nickname, password, bio, avatar_url, role, is_banned, created_at, deleted_at " +
                     "FROM users WHERE email = ? AND deleted_at IS NULL";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException("UserDao.findByEmail 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** user_id로 사용자 조회 */
    public UserDetailDto findById(int userId) {
        String sql = "SELECT user_id, email, nickname, password, bio, avatar_url, role, is_banned, created_at, deleted_at " +
                     "FROM users WHERE user_id = ? AND deleted_at IS NULL";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException("UserDao.findById 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    // 추가
    /** user_nickname로 사용자 조회 */
    public UserDetailDto findByNickname(String nickname) {
        String sql = "SELECT user_id, email, nickname, password, bio, avatar_url, role, is_banned, created_at, deleted_at " +
                "FROM users WHERE nickname = ? AND deleted_at IS NULL";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nickname);
            rs = pstmt.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException("UserDao.findByNickname 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }


    /** 관리자 - 사용자 목록 조회 (keyword: 이메일/닉네임 검색, null이면 전체) */
    public List<UserDetailDto> findAll(String keyword, int offset, int limit) {
        return findAll(keyword, null, offset, limit);
    }

    /** 관리자 - 사용자 목록 조회 (검색어와 상태 필터 적용) */
    public List<UserDetailDto> findAll(String keyword, String status, int offset, int limit) {
        String like = (keyword != null && !keyword.trim().isEmpty()) ? "%" + keyword.trim() + "%" : "%";
        String statusCondition = getStatusCondition(status);
        String sql = "SELECT user_id, email, nickname, bio, avatar_url, role, is_banned, created_at, deleted_at " +
                     "FROM users WHERE (email LIKE ? OR nickname LIKE ?) " + statusCondition +
                     "ORDER BY created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<UserDetailDto> list = new ArrayList<>();
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
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("UserDao.findAll 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 관리자 - 검색 조건 포함 전체 사용자 수 */
    public int countAll(String keyword) {
        return countAll(keyword, null);
    }

    /** 관리자 - 검색 조건과 상태 필터를 포함한 사용자 수 */
    public int countAll(String keyword, String status) {
        String like = (keyword != null && !keyword.trim().isEmpty()) ? "%" + keyword.trim() + "%" : "%";
        String statusCondition = getStatusCondition(status);
        String sql = "SELECT COUNT(*) FROM users WHERE (email LIKE ? OR nickname LIKE ?) " + statusCondition;
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
            throw new RuntimeException("UserDao.countAll 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 관리자 회원관리 통계용 상태별 사용자 수 */
    public int countByStatus(String status) {
        String statusCondition = getStatusCondition(status);
        String sql = "SELECT COUNT(*) FROM users WHERE 1 = 1 " + statusCondition;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("UserDao.countByStatus 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    // ── 삽입 ────────────────────────────────────────────────────────────────

    /** 회원가입 — 생성된 user_id 반환 */
    /** Admin dashboard - count users registered today. */
    public int countTodayCreated() {
        String sql = "SELECT COUNT(*) FROM users " +
                     "WHERE deleted_at IS NULL AND TRUNC(created_at) = TRUNC(SYSTIMESTAMP)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("UserDao.countTodayCreated failed", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** Admin dashboard - daily registered user counts for the last 7 days. */
    public List<Map<String, Object>> countDailyCreatedLast7Days() {
        String sql =
                "SELECT TO_CHAR(d.stat_date, 'MM/DD') AS label, NVL(u.user_count, 0) AS user_count " +
                "FROM ( " +
                "    SELECT TRUNC(CAST(SYSTIMESTAMP AS DATE)) - 7 + LEVEL AS stat_date " +
                "    FROM dual CONNECT BY LEVEL <= 7 " +
                ") d " +
                "LEFT JOIN ( " +
                "    SELECT TRUNC(CAST(created_at AS DATE)) AS stat_date, COUNT(*) AS user_count " +
                "    FROM users " +
                "    WHERE deleted_at IS NULL " +
                "      AND created_at >= TRUNC(CAST(SYSTIMESTAMP AS DATE)) - 6 " +
                "    GROUP BY TRUNC(CAST(created_at AS DATE)) " +
                ") u ON u.stat_date = d.stat_date " +
                "ORDER BY d.stat_date";
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("label", rs.getString("label"));
                item.put("count", rs.getInt("user_count"));
                list.add(item);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("UserDao.countDailyCreatedLast7Days failed", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    public int insert(String email, String nickname, String hashedPassword) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();

            // 시퀀스로 새 ID 채번
            pstmt = conn.prepareStatement("SELECT users_seq.NEXTVAL FROM DUAL");
            rs = pstmt.executeQuery();
            rs.next();
            int newId = rs.getInt(1);
            rs.close(); rs = null;
            pstmt.close(); pstmt = null;

            // 사용자 등록
            pstmt = conn.prepareStatement(
                "INSERT INTO users (user_id, email, nickname, password, role, is_banned, created_at) " +
                "VALUES (?, ?, ?, ?, 'USER', 'N', SYSTIMESTAMP)"
            );
            pstmt.setInt(1, newId);
            pstmt.setString(2, email);
            pstmt.setString(3, nickname);
            pstmt.setString(4, hashedPassword);
            pstmt.executeUpdate();
            return newId;
        } catch (SQLException e) {
            throw new RuntimeException("UserDao.insert 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    // ── 수정 ────────────────────────────────────────────────────────────────

    /** 프로필 수정 (닉네임, 자기소개, 아바타 URL) */
    public void updateProfile(int userId, String nickname, String bio, String avatarUrl) {
        String sql = "UPDATE users SET nickname = ?, bio = ?, avatar_url = ? WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nickname);
            pstmt.setString(2, bio);
            pstmt.setString(3, avatarUrl);
            pstmt.setInt(4, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("UserDao.updateProfile 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 비밀번호 변경 (hashedPassword: BCrypt 해시값) */
    public void updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("UserDao.updatePassword 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 계정 소프트 삭제 (deleted_at 설정, 실제 데이터는 보존) */
    public void softDelete(int userId) {
        String sql = "UPDATE users SET deleted_at = SYSTIMESTAMP WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("UserDao.softDelete 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 관리자 - 계정 정지 */
    public void ban(int userId) {
        String sql = "UPDATE users SET is_banned = 'Y' WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("UserDao.ban 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 관리자 - 계정 정지 해제 */
    public void unban(int userId) {
        String sql = "UPDATE users SET is_banned = 'N' WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("UserDao.unban 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /** 관리자 - 계정 강제 삭제 (하드 삭제) */
    public void hardDelete(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("UserDao.hardDelete 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    // ── 내부 헬퍼 ───────────────────────────────────────────────────────────

    /** ResultSet 한 행 → UserDetailDto 변환 */
    private UserDetailDto mapRow(ResultSet rs) throws SQLException {
        UserDetailDto u = new UserDetailDto();
        u.setUserId(rs.getInt("user_id"));
        u.setEmail(rs.getString("email"));
        u.setNickname(rs.getString("nickname"));
        try { u.setPassword(rs.getString("password")); } catch (SQLException ignored) {}
        u.setBio(rs.getString("bio"));
        u.setAvatarUrl(rs.getString("avatar_url"));
        u.setRole(rs.getString("role"));
        u.setIsBanned(rs.getString("is_banned"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setDeletedAt(rs.getTimestamp("deleted_at"));
        return u;
    }

    private String getStatusCondition(String status) {
        if ("active".equals(status)) {
            return "AND deleted_at IS NULL AND is_banned = 'N' ";
        }
        if ("banned".equals(status)) {
            return "AND deleted_at IS NULL AND is_banned = 'Y' ";
        }
        if ("deleted".equals(status)) {
            return "AND deleted_at IS NOT NULL ";
        }
        return "AND deleted_at IS NULL ";
    }
}
