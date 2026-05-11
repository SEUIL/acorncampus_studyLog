package com.acorncampus_studylog.service.ai;

import java.util.Locale;

public enum AiWritingAction {
    IMPROVE,
    SUMMARY,
    EXPAND,
    TITLE,
    TAGS,
    CUSTOM;

    public static AiWritingAction from(String value) {
        if (value == null || value.isBlank()) {
            throw new AiWritingValidationException("AI 작업은 필수입니다.");
        }

        try {
            return AiWritingAction.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new AiWritingValidationException("지원하지 않는 AI 작업입니다.");
        }
    }
}
