package com.acorncampus_studylog.dao;

import com.acorncampus_studylog.dto.PasswordResetTokenDto;
import com.acorncampus_studylog.util.DBUtil;

import java.sql.*;

/** password_reset_tokens 테이블 CRUD */
public class PasswordResetDao {

    /**
     * 토큰 저장
     *
     * @param userId    토큰 소유자 user_id
     * @param token     SecureRandom으로 생성한 64자리 hex 문자열
     * @param expiresAt 만료 시각 (생성 시각 + 30분)
     */
    public void insertToken(int userId, String token, Timestamp expiresAt) {
        String sql = "INSERT INTO password_reset_tokens (token, user_id, expires_at, used_yn) VALUES (?, ?, ?, 'N')";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            pstmt.setInt(2, userId);
            pstmt.setTimestamp(3, expiresAt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("PasswordResetDao.insertToken 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /**
     * 유효한 토큰 조회 — 미사용(used_yn='N') + 미만료(expires_at > 현재) 조건
     *
     * @param token 조회할 토큰 값
     * @return 유효한 토큰이면 PasswordResetTokenDto, 없거나 만료/사용 완료면 null
     */
    public PasswordResetTokenDto findValidToken(String token) {
        String sql = """
                SELECT token, user_id, expires_at, used_yn
                  FROM password_reset_tokens
                 WHERE token = ?
                   AND used_yn = 'N'
                   AND expires_at > SYSTIMESTAMP
                """;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return new PasswordResetTokenDto(
                        rs.getString("token"),
                        rs.getInt("user_id"),
                        rs.getTimestamp("expires_at"),
                        rs.getString("used_yn")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("PasswordResetDao.findValidToken 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /**
     * 토큰을 사용 완료 처리 (used_yn = 'Y')
     * 비밀번호 변경 성공 후 반드시 호출해 재사용을 방지한다.
     *
     * @param token 무효화할 토큰 값
     */
    public void markUsed(String token) {
        String sql = "UPDATE password_reset_tokens SET used_yn = 'Y' WHERE token = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, token);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("PasswordResetDao.markUsed 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /**
     * 만료된 토큰 일괄 삭제 — 선택적 배치 작업용
     * 주기적으로 호출하여 테이블이 비대해지는 것을 방지한다.
     */
    public void deleteExpired() {
        String sql = "DELETE FROM password_reset_tokens WHERE expires_at <= SYSTIMESTAMP";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("PasswordResetDao.deleteExpired 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }
}
