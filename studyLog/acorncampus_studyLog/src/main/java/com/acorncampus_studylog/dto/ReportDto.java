package com.acorncampus_studylog.dto;

import java.sql.Timestamp;

/** 신고 데이터 전송 객체 (reports 테이블 + 신고자/대상 요약 포함) */
public class ReportDto {

    // --- DB 저장 필드 ---
    private int reportId;
    private int reporterId;
    private String targetType;   // "POST" / "COMMENT"
    private int targetId;
    private String reason;
    private String status;       // "PENDING" / "RESOLVED" / "DISMISSED"
    private Integer processedBy;
    private Timestamp processedAt;
    private Timestamp createdAt;

    // --- JOIN 필드 (DB 저장 X, 조회 시 채움) ---
    private String reporterName;   // users.nickname (신고자)
    private String targetSummary;  // 게시글 제목 또는 댓글 내용 앞 50자
    private String processorName;
    private String targetAuthorName;
    private String targetAuthorEmail;

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getReporterId() { return reporterId; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public int getTargetId() { return targetId; }
    public void setTargetId(int targetId) { this.targetId = targetId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProcessedBy() { return processedBy; }
    public void setProcessedBy(Integer processedBy) { this.processedBy = processedBy; }

    public Timestamp getProcessedAt() { return processedAt; }
    public void setProcessedAt(Timestamp processedAt) { this.processedAt = processedAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getProcessorName() { return processorName; }
    public void setProcessorName(String processorName) { this.processorName = processorName; }

    public String getTargetSummary() { return targetSummary; }
    public void setTargetSummary(String targetSummary) { this.targetSummary = targetSummary; }

    public String getTargetAuthorName() { return targetAuthorName; }
    public void setTargetAuthorName(String targetAuthorName) { this.targetAuthorName = targetAuthorName; }

    public String getTargetAuthorEmail() { return targetAuthorEmail; }
    public void setTargetAuthorEmail(String targetAuthorEmail) { this.targetAuthorEmail = targetAuthorEmail; }
}
