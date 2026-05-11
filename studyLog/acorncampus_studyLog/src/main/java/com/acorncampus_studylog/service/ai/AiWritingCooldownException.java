package com.acorncampus_studylog.service.ai;

public class AiWritingCooldownException extends RuntimeException {

    private final int remainingSeconds;

    public AiWritingCooldownException(int remainingSeconds) {
        super("AI 요청은 15초에 한 번만 가능합니다.");
        this.remainingSeconds = remainingSeconds;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }
}
