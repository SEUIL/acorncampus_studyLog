package com.acorncampus_studylog.service;

import com.acorncampus_studylog.dao.ReportDao;
import com.acorncampus_studylog.dto.PageDto;
import com.acorncampus_studylog.dto.ReportDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 신고 비즈니스 로직 (신고 접수, 관리자 처리) */
public class ReportService {

    private final ReportDao reportDao = new ReportDao();

    /**
     * 게시글 신고
     * 중복 신고 방지 포함
     * @return {"status":"ok"} 또는 {"status":"error", "message":"이미 신고한 게시글입니다."}
     */
    public Map<String, Object> reportPost(int reporterId, int postId, String reason) {
        Map<String, Object> result = new HashMap<>();

        // 1. 중복 신고 여부 확인[cite: 5]
        boolean isDuplicate = reportDao.existsDuplicate(reporterId, "POST", postId);

        if (isDuplicate) {
            result.put("status", "error");
            result.put("message", "이미 신고한 게시글입니다.");
        } else {
            // 2. 중복이 아니면 신고 접수 (INSERT)[cite: 5]
            reportDao.insert(reporterId, "POST", postId, reason);
            result.put("status", "ok");
        }

        return result;
    }

    /**
     * 댓글 신고
     * 중복 신고 방지 포함
     * @return {"status":"ok"} 또는 {"status":"error", "message":"이미 신고한 댓글입니다."}
     */
    public Map<String, Object> reportComment(int reporterId, int commentId, String reason) {
        Map<String, Object> result = new HashMap<>();

        // 1. 중복 신고 여부 확인[cite: 5]
        boolean isDuplicate = reportDao.existsDuplicate(reporterId, "COMMENT", commentId);

        if (isDuplicate) {
            result.put("status", "error");
            result.put("message", "이미 신고한 댓글입니다.");
        } else {
            // 2. 중복이 아니면 신고 접수 (INSERT)[cite: 5]
            reportDao.insert(reporterId, "COMMENT", commentId, reason);
            result.put("status", "ok");
        }

        return result;
    }

    // ── 관리자 전용 ──────────────────────────────────────────────────────────

    /**
     * 관리자 - 신고 목록 조회
     * @param status "PENDING" / "RESOLVED" / "DISMISSED" / null(전체)
     * @param pageNo 현재 페이지
     */
    public List<ReportDto> getReportList(String status, int pageNo) {
        int limit = 10; // 페이지당 10개 출력
        int offset = (pageNo - 1) * limit;

        // DAO 호출: 상태 필터 적용 및 페이징 처리된 목록 반환
        return reportDao.findAll(status, offset, limit);
    }

    /** 관리자 - 신고 목록 페이지 정보 */
    public PageDto getReportPage(String status, int pageNo) {
        int limit = 10;

        // DAO 호출: 상태 필터가 적용된 전체 데이터 수 반환
        int totalCount = reportDao.countAll(status);

        return new PageDto(pageNo, limit, totalCount);
    }

    /**
     * 관리자 - 신고 처리 (RESOLVED)
     * 신고 처리 후 대상 컨텐츠 삭제는 별도로 PostService/CommentService에서 처리
     */
    public void resolveReport(int reportId) {
        // DAO 호출: 해당 신고의 상태를 '처리됨'으로 변경
        reportDao.updateStatus(reportId, "RESOLVED");
    }

    /** 관리자 - 신고 기각 (DISMISSED) */
    public void dismissReport(int reportId) {
        // DAO 호출: 해당 신고의 상태를 '기각됨(반려)'으로 변경
        reportDao.updateStatus(reportId, "DISMISSED");
    }
}
