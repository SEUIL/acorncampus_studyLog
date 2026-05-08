package com.acorncampus_studylog.dao;

import com.acorncampus_studylog.dto.ReportDto;
import com.acorncampus_studylog.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** reports 테이블 CRUD — 신고 접수, 관리자 처리 */
public class ReportDao {

    // ── 조회 ────────────────────────────────────────────────────────────────

    /** 신고 목록 조회 (상태 필터 가능, null이면 전체, 페이지네이션) */
    public List<ReportDto> findAll(String status, int offset, int limit) {
        String whereStatus = (status != null && !status.isEmpty()) ? " AND r.status = ?" : "";
        String sql =
            "SELECT r.report_id, r.reporter_id, r.target_type, r.target_id, r.reason, r.status, r.created_at, " +
            "       u.nickname AS reporter_name " +
            "FROM reports r JOIN users u ON r.reporter_id = u.user_id " +
            "WHERE 1=1" + whereStatus + " " +
            "ORDER BY r.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<ReportDto> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            int idx = 1;
            if (whereStatus.length() > 0) pstmt.setString(idx++, status);
            pstmt.setInt(idx++, offset);
            pstmt.setInt(idx, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("ReportDao.findAll 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    public List<ReportDto> findAll(String status, String keyword, int offset, int limit) {
        String normalizedKeyword = normalizeKeyword(keyword);
        StringBuilder sql = new StringBuilder()
            .append("SELECT r.report_id, r.reporter_id, r.target_type, r.target_id, r.reason, r.status, r.created_at, ")
            .append("       u.nickname AS reporter_name ")
            .append("FROM reports r JOIN users u ON r.reporter_id = u.user_id ")
            .append("WHERE 1=1 ");
        appendReportFilters(sql, status, normalizedKeyword);
        sql.append("ORDER BY r.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        List<ReportDto> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int idx = bindReportFilters(pstmt, status, normalizedKeyword, 1);
            pstmt.setInt(idx++, offset);
            pstmt.setInt(idx, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("ReportDao.findAll 검색 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** report_id로 신고 단건 조회 */
    public ReportDto findById(int reportId) {
        String sql =
            "SELECT r.report_id, r.reporter_id, r.target_type, r.target_id, r.reason, r.status, r.created_at, " +
            "       u.nickname AS reporter_name " +
            "FROM reports r JOIN users u ON r.reporter_id = u.user_id " +
            "WHERE r.report_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reportId);
            rs = pstmt.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            throw new RuntimeException("ReportDao.findById 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /**
     * 중복 신고 여부 확인
     * @return 동일 대상에 대한 신고가 이미 존재하면 true
     */
    public boolean existsDuplicate(int reporterId, String targetType, int targetId) {
        String sql = "SELECT COUNT(*) FROM reports " +
                     "WHERE reporter_id = ? AND target_type = ? AND target_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reporterId);
            pstmt.setString(2, targetType);
            pstmt.setInt(3, targetId);
            rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("ReportDao.existsDuplicate 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 상태별 신고 수 (status=null이면 전체 수) */
    public int countAll(String status) {
        String whereStatus = (status != null && !status.isEmpty()) ? " WHERE status = ?" : "";
        String sql = "SELECT COUNT(*) FROM reports" + whereStatus;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            if (whereStatus.length() > 0) pstmt.setString(1, status);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("ReportDao.countAll 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    public int countAll(String status, String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        StringBuilder sql = new StringBuilder()
            .append("SELECT COUNT(*) ")
            .append("FROM reports r JOIN users u ON r.reporter_id = u.user_id ")
            .append("WHERE 1=1 ");
        appendReportFilters(sql, status, normalizedKeyword);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            bindReportFilters(pstmt, status, normalizedKeyword, 1);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("ReportDao.countAll 검색 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    // ── 삽입 / 수정 ─────────────────────────────────────────────────────────

    /** 신고 등록 — 생성된 report_id 반환 */
    public int insert(int reporterId, String targetType, int targetId, String reason) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();

            pstmt = conn.prepareStatement("SELECT reports_seq.NEXTVAL FROM DUAL");
            rs = pstmt.executeQuery();
            rs.next();
            int newId = rs.getInt(1);
            rs.close(); rs = null;
            pstmt.close(); pstmt = null;

            pstmt = conn.prepareStatement(
                "INSERT INTO reports (report_id, reporter_id, target_type, target_id, reason, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, 'PENDING', SYSTIMESTAMP)"
            );
            pstmt.setInt(1, newId);
            pstmt.setInt(2, reporterId);
            pstmt.setString(3, targetType);
            pstmt.setInt(4, targetId);
            pstmt.setString(5, reason);
            pstmt.executeUpdate();
            return newId;
        } catch (SQLException e) {
            throw new RuntimeException("ReportDao.insert 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /**
     * 신고 상태 변경 (관리자 처리)
     * @param status "RESOLVED"(처리) 또는 "DISMISSED"(기각)
     */
    public void updateStatus(int reportId, String status) {
        String sql = "UPDATE reports SET status = ? WHERE report_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, reportId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("ReportDao.updateStatus 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    // ── 내부 헬퍼 ───────────────────────────────────────────────────────────

    private void appendReportFilters(StringBuilder sql, String status, String keyword) {
        if (status != null && !status.isEmpty()) {
            sql.append("AND r.status = ? ");
        }
        if (keyword != null) {
            // 신고 검색은 신고자, 사유, 대상 타입, 대상 ID를 기준으로 처리한다.
            sql.append("AND (LOWER(u.nickname) LIKE LOWER(?) ")
               .append("OR LOWER(r.reason) LIKE LOWER(?) ")
               .append("OR LOWER(r.target_type) LIKE LOWER(?) ")
               .append("OR TO_CHAR(r.target_id) LIKE ?) ");
        }
    }

    private int bindReportFilters(PreparedStatement pstmt, String status, String keyword, int startIndex)
            throws SQLException {
        int idx = startIndex;
        if (status != null && !status.isEmpty()) {
            pstmt.setString(idx++, status);
        }
        if (keyword != null) {
            String like = "%" + keyword + "%";
            pstmt.setString(idx++, like);
            pstmt.setString(idx++, like);
            pstmt.setString(idx++, like);
            pstmt.setString(idx++, like);
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

    private ReportDto mapRow(ResultSet rs) throws SQLException {
        ReportDto r = new ReportDto();
        r.setReportId(rs.getInt("report_id"));
        r.setReporterId(rs.getInt("reporter_id"));
        r.setTargetType(rs.getString("target_type"));
        r.setTargetId(rs.getInt("target_id"));
        r.setReason(rs.getString("reason"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        try { r.setReporterName(rs.getString("reporter_name")); } catch (SQLException ignored) {}
        return r;
    }
}
