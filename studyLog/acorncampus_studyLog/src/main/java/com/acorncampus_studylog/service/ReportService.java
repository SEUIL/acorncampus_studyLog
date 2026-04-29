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
        // TODO: reportDao.existsDuplicate(reporterId, "POST", postId) → 중복이면 error 반환
        //       → reportDao.insert(reporterId, "POST", postId, reason) → ok 반환
        return new HashMap<>();
    }

    /**
     * 댓글 신고
     * 중복 신고 방지 포함
     * @return {"status":"ok"} 또는 {"status":"error", "message":"이미 신고한 댓글입니다."}
     */
    public Map<String, Object> reportComment(int reporterId, int commentId, String reason) {
        // TODO: reportDao.existsDuplicate(reporterId, "COMMENT", commentId) → 중복이면 error 반환
        //       → reportDao.insert(reporterId, "COMMENT", commentId, reason) → ok 반환
        return new HashMap<>();
    }

    // ── 관리자 전용 ──────────────────────────────────────────────────────────

    /**
     * 관리자 - 신고 목록 조회
     * @param status "PENDING" / "RESOLVED" / "DISMISSED" / null(전체)
     * @param pageNo 현재 페이지
     */
    public List<ReportDto> getReportList(String status, int pageNo) {
        // TODO: reportDao.countAll(status) → PageDto → reportDao.findAll(status, offset, pageSize)
        return null;
    }

    /** 관리자 - 신고 목록 페이지 정보 */
    public PageDto getReportPage(String status, int pageNo) {
        // TODO: new PageDto(pageNo, 10, reportDao.countAll(status))
        return null;
    }

    /**
     * 관리자 - 신고 처리 (RESOLVED)
     * 신고 처리 후 대상 컨텐츠 삭제는 별도로 PostService/CommentService에서 처리
     */
    public void resolveReport(int reportId) {
        // TODO: reportDao.updateStatus(reportId, "RESOLVED")
    }

    /** 관리자 - 신고 기각 (DISMISSED) */
    public void dismissReport(int reportId) {
        // TODO: reportDao.updateStatus(reportId, "DISMISSED")
    }
}
