package com.acorncampus_studylog.dto;

import java.sql.Timestamp;

/** 비밀번호 재설정 토큰 — DB password_reset_tokens 행과 1:1 대응 */
public record PasswordResetTokenDto(
        String    token,
        int       userId,
        Timestamp expiresAt,
        String    usedYn
) {}
