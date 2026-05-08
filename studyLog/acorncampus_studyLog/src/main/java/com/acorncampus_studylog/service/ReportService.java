package com.acorncampus_studylog.service;

import com.acorncampus_studylog.dao.CommentDao;
import com.acorncampus_studylog.dao.PostDao;
import com.acorncampus_studylog.dao.ReportDao;
import com.acorncampus_studylog.dto.PageDto;
import com.acorncampus_studylog.dto.ReportDto;
import com.acorncampus_studylog.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 신고 비즈니스 로직 */
public class ReportService {

    private final ReportDao reportDao = new ReportDao();
    private final PostDao postDao = new PostDao();
    private final CommentDao commentDao = new CommentDao();

    /** 게시글 신고 접수. 동일 사용자의 같은 게시글 중복 신고는 막는다. */
    public Map<String, Object> reportPost(int reporterId, int postId, String reason) {
        Map<String, Object> result = new HashMap<>();

        if (reportDao.existsDuplicate(reporterId, "POST", postId)) {
            result.put("status", "error");
            result.put("message", "이미 신고한 게시글입니다.");
        } else {
            reportDao.insert(reporterId, "POST", postId, reason);
            result.put("status", "ok");
        }

        return result;
    }

    /** 댓글 신고 접수. 동일 사용자의 같은 댓글 중복 신고는 막는다. */
    public Map<String, Object> reportComment(int reporterId, int commentId, String reason) {
        Map<String, Object> result = new HashMap<>();

        if (reportDao.existsDuplicate(reporterId, "COMMENT", commentId)) {
            result.put("status", "error");
            result.put("message", "이미 신고한 댓글입니다.");
        } else {
            reportDao.insert(reporterId, "COMMENT", commentId, reason);
            result.put("status", "ok");
        }

        return result;
    }

    /** 관리자 신고 목록 조회. 상태 필터만 적용한다. */
    public List<ReportDto> getReportList(String status, int pageNo) {
        int limit = 10;
        int offset = (pageNo - 1) * limit;
        return reportDao.findAll(status, offset, limit);
    }

    /** 관리자 신고 목록 조회. 상태 필터와 검색어를 함께 적용한다. */
    public List<ReportDto> getReportList(String status, String keyword, int pageNo) {
        int limit = 10;
        int offset = (pageNo - 1) * limit;
        return reportDao.findAll(status, keyword, offset, limit);
    }

    /** 관리자 신고 목록 페이지 정보를 만든다. */
    public PageDto getReportPage(String status, int pageNo) {
        int limit = 10;
        return new PageDto(pageNo, limit, reportDao.countAll(status));
    }

    /** 검색 조건이 포함된 관리자 신고 목록 페이지 정보를 만든다. */
    public PageDto getReportPage(String status, String keyword, int pageNo) {
        int limit = 10;
        return new PageDto(pageNo, limit, reportDao.countAll(status, keyword));
    }

    /** 신고 승인 처리. 대상 게시글/댓글 삭제와 신고 상태 변경을 하나의 트랜잭션으로 묶는다. */
    public void resolveReport(int reportId) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            ReportDto report = reportDao.findById(reportId, conn);
            if (report == null) {
                throw new RuntimeException("존재하지 않는 신고입니다.");
            }

            int deletedRows;
            if ("POST".equals(report.getTargetType())) {
                deletedRows = postDao.softDelete(report.getTargetId(), conn);
            } else if ("COMMENT".equals(report.getTargetType())) {
                deletedRows = commentDao.softDelete(report.getTargetId(), conn);
            } else {
                throw new RuntimeException("알 수 없는 신고 대상입니다.");
            }

            if (deletedRows == 0) {
                throw new RuntimeException("신고 대상이 존재하지 않습니다.");
            }
            if (reportDao.updateStatus(reportId, "RESOLVED", conn) == 0) {
                throw new RuntimeException("신고 상태 변경에 실패했습니다.");
            }

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RuntimeException("신고 처리 중 DB 오류가 발생했습니다.", e);
        } catch (RuntimeException e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            closeTransactionConnection(conn);
        }
    }

    /** 신고 기각 처리. 대상 콘텐츠는 건드리지 않고 신고 상태만 DISMISSED로 변경한다. */
    public void dismissReport(int reportId) {
        if (reportDao.updateStatus(reportId, "DISMISSED") == 0) {
            throw new RuntimeException("존재하지 않는 신고입니다.");
        }
    }

    /** 트랜잭션 실패 시 rollback을 시도하고, rollback 실패는 원래 예외를 가리지 않도록 무시한다. */
    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    /** 트랜잭션용 Connection을 기본 auto-commit 상태로 되돌리고 닫는다. */
    private void closeTransactionConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
