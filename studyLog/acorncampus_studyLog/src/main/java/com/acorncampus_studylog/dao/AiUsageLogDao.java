package com.acorncampus_studylog.dao;

import com.acorncampus_studylog.dto.AiUsageLogDto;
import com.acorncampus_studylog.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** ai_usage_log 테이블 CRUD 및 AI 요청 쿨다운 조회 */
public class AiUsageLogDao {

    public int insertPending(int userId, String action, int inputChars, int customPromptChars, int maxOutputTokens) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();

            pstmt = conn.prepareStatement("SELECT ai_usage_log_seq.NEXTVAL FROM DUAL");
            rs = pstmt.executeQuery();
            rs.next();
            int usageId = rs.getInt(1);
            rs.close(); rs = null;
            pstmt.close(); pstmt = null;

            pstmt = conn.prepareStatement(
                    "INSERT INTO ai_usage_log " +
                    "(usage_id, user_id, action, status, input_chars, custom_prompt_chars, max_output_tokens, requested_at) " +
                    "VALUES (?, ?, ?, 'PENDING', ?, ?, ?, SYSTIMESTAMP)"
            );
            pstmt.setInt(1, usageId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, action);
            pstmt.setInt(4, inputChars);
            pstmt.setInt(5, customPromptChars);
            pstmt.setInt(6, maxOutputTokens);
            pstmt.executeUpdate();
            return usageId;
        } catch (SQLException e) {
            throw new RuntimeException("AiUsageLogDao.insertPending 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    public void markSuccess(int usageId) {
        String sql = "UPDATE ai_usage_log SET status = 'SUCCESS', completed_at = SYSTIMESTAMP, error_code = NULL WHERE usage_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, usageId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("AiUsageLogDao.markSuccess 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    public void markFailed(int usageId, String errorCode) {
        String sql = "UPDATE ai_usage_log SET status = 'FAILED', completed_at = SYSTIMESTAMP, error_code = ? WHERE usage_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, errorCode);
            pstmt.setInt(2, usageId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("AiUsageLogDao.markFailed 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    public AiUsageLogDto findRecentPendingOrSuccess(int userId, int seconds) {
        String sql = """
                SELECT usage_id, user_id, action, status, input_chars, custom_prompt_chars,
                       max_output_tokens, requested_at, completed_at, error_code
                  FROM ai_usage_log
                 WHERE user_id = ?
                   AND status IN ('PENDING', 'SUCCESS')
                   AND requested_at >= SYSTIMESTAMP - NUMTODSINTERVAL(?, 'SECOND')
                 ORDER BY requested_at DESC
                 FETCH FIRST 1 ROW ONLY
                """;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, seconds);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("AiUsageLogDao.findRecentPendingOrSuccess 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    private AiUsageLogDto mapRow(ResultSet rs) throws SQLException {
        return new AiUsageLogDto(
                rs.getInt("usage_id"),
                rs.getInt("user_id"),
                rs.getString("action"),
                rs.getString("status"),
                rs.getInt("input_chars"),
                rs.getInt("custom_prompt_chars"),
                rs.getInt("max_output_tokens"),
                rs.getTimestamp("requested_at"),
                rs.getTimestamp("completed_at"),
                rs.getString("error_code")
        );
    }
}
