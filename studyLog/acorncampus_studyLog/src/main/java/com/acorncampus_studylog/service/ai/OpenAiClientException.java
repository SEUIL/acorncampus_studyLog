package com.acorncampus_studylog.service.ai;

public class OpenAiClientException extends RuntimeException {

    private final OpenAiErrorCode code;

    public OpenAiClientException(OpenAiErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public OpenAiClientException(OpenAiErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public OpenAiErrorCode getCode() {
        return code;
    }
}
