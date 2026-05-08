package com.acorncampus_studylog.dao;

import com.acorncampus_studylog.dto.ReportDto;
import com.acorncampus_studylog.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** reports 테이블 CRUD 및 신고 접수, 관리자 처리 */
public class ReportDao {

    private static final String REPORT_SELECT =
        "SELECT r.report_id, r.reporter_id, r.target_type, r.target_id, r.reason, r.status, r.created_at, " +
        "       reporter.nickname AS reporter_name, " +
        "       target_user.nickname AS target_author_name, " +
        "       target_user.email AS target_author_email, " +
        "       CASE " +
        "           WHEN r.target_type = 'POST' THEN p.title " +
        "           WHEN r.target_type = 'COMMENT' THEN SUBSTR(c.content, 1, 80) " +
        "       END AS target_summary ";

    private static final String REPORT_FROM =
        "FROM reports r " +
        "JOIN users reporter ON r.reporter_id = reporter.user_id " +
        "LEFT JOIN posts p ON r.target_type = 'POST' AND r.target_id = p.post_id " +
        "LEFT JOIN comments c ON r.target_type = 'COMMENT' AND r.target_id = c.comment_id " +
        "LEFT JOIN users target_user ON target_user.user_id = CASE " +
        "    WHEN r.target_type = 'POST' THEN p.user_id " +
        "    WHEN r.target_type = 'COMMENT' THEN c.user_id " +
        "END ";

    /** 신고 목록 조회. 신고자, 대상 요약, 대상 작성자 정보를 함께 가져온다. */
    public List<ReportDto> findAll(String status, int offset, int limit) {
        String whereStatus = (status != null && !status.isEmpty()) ? "AND r.status = ? " : "";
        String sql = REPORT_SELECT + REPORT_FROM +
            "WHERE 1=1 " + whereStatus +
            "ORDER BY r.created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<ReportDto> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            int idx = 1;
            if (!whereStatus.isEmpty()) pstmt.setString(idx++, status);
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

    /** 검색어가 포함된 신고 목록 조회. 신고자/대상 작성자/사유/대상 요약을 검색한다. */
    public List<ReportDto> findAll(String status, String keyword, int offset, int limit) {
        String normalizedKeyword = normalizeKeyword(keyword);
        StringBuilder sql = new StringBuilder()
            .append(REPORT_SELECT)
            .append(REPORT_FROM)
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

    /** 신고 단건 조회. 일반 호출용으로 내부에서 Connection을 직접 연다. */
    public ReportDto findById(int reportId) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return findById(reportId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("ReportDao.findById 실패", e);
        } finally {
            DBUtil.close(conn, null, null);
        }
    }

    /** 신고 단건 조회. 트랜잭션 중 같은 Connection을 재사용할 때 사용한다. */
    public ReportDto findById(int reportId, Connection conn) throws SQLException {
        String sql = REPORT_SELECT + REPORT_FROM + "WHERE r.report_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reportId);
            rs = pstmt.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
    }

    /** 동일 사용자가 같은 대상에 이미 신고했는지 확인한다. */
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

    /** 상태 조건에 맞는 신고 수를 조회한다. */
    public int countAll(String status) {
        String whereStatus = (status != null && !status.isEmpty()) ? " WHERE status = ?" : "";
        String sql = "SELECT COUNT(*) FROM reports" + whereStatus;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            if (!whereStatus.isEmpty()) pstmt.setString(1, status);
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("ReportDao.countAll 실패", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    /** 상태/검색 조건에 맞는 신고 수를 조회한다. */
    public int countAll(String status, String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        StringBuilder sql = new StringBuilder()
            .append("SELECT COUNT(*) ")
            .append(REPORT_FROM)
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

    /** 신고를 새로 등록하고 생성된 report_id를 반환한다. */
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

    /** 신고 상태를 변경한다. 일반 호출용으로 내부에서 Connection을 직접 연다. */
    public int updateStatus(int reportId, String status) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return updateStatus(reportId, status, conn);
        } catch (SQLException e) {
            throw new RuntimeException("ReportDao.updateStatus 실패", e);
        } finally {
            DBUtil.close(conn, null, null);
        }
    }

    /** 신고 상태를 변경한다. 트랜잭션 중 같은 Connection을 재사용할 때 사용한다. */
    public int updateStatus(int reportId, String status, Connection conn) throws SQLException {
        String sql = "UPDATE reports SET status = ? WHERE report_id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, reportId);
            return pstmt.executeUpdate();
        } finally {
            if (pstmt != null) pstmt.close();
        }
    }

    /** 신고 목록 조회 SQL에 상태/검색 조건을 추가한다. */
    private void appendReportFilters(StringBuilder sql, String status, String keyword) {
        if (status != null && !status.isEmpty()) {
            sql.append("AND r.status = ? ");
        }
        if (keyword != null) {
            sql.append("AND (LOWER(reporter.nickname) LIKE LOWER(?) ")
               .append("OR LOWER(r.reason) LIKE LOWER(?) ")
               .append("OR LOWER(r.target_type) LIKE LOWER(?) ")
               .append("OR TO_CHAR(r.target_id) LIKE ? ")
               .append("OR LOWER(target_user.nickname) LIKE LOWER(?) ")
               .append("OR LOWER(target_user.email) LIKE LOWER(?) ")
               .append("OR LOWER(CASE WHEN r.target_type = 'POST' THEN p.title WHEN r.target_type = 'COMMENT' THEN SUBSTR(c.content, 1, 80) END) LIKE LOWER(?)) ");
        }
    }

    /** appendReportFilters에서 추가한 ? 파라미터에 값을 순서대로 바인딩한다. */
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
            pstmt.setString(idx++, like);
            pstmt.setString(idx++, like);
            pstmt.setString(idx++, like);
        }
        return idx;
    }

    /** 빈 검색어를 null로 정리한다. */
    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** ResultSet 한 행을 ReportDto로 변환한다. */
    private ReportDto mapRow(ResultSet rs) throws SQLException {
        ReportDto r = new ReportDto();
        r.setReportId(rs.getInt("report_id"));
        r.setReporterId(rs.getInt("reporter_id"));
        r.setTargetType(rs.getString("target_type"));
        r.setTargetId(rs.getInt("target_id"));
        r.setReason(rs.getString("reason"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setReporterName(rs.getString("reporter_name"));
        r.setTargetSummary(rs.getString("target_summary"));
        r.setTargetAuthorName(rs.getString("target_author_name"));
        r.setTargetAuthorEmail(rs.getString("target_author_email"));
        return r;
    }
}
