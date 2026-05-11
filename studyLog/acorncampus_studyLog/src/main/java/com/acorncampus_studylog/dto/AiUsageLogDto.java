package com.acorncampus_studylog.dto;

import java.sql.Timestamp;

/** AI 사용 로그 — DB ai_usage_log 행과 1:1 대응 */
public record AiUsageLogDto(
        int usageId,
        int userId,
        String action,
        String status,
        int inputChars,
        int customPromptChars,
        int maxOutputTokens,
        Timestamp requestedAt,
        Timestamp completedAt,
        String errorCode
) {}
