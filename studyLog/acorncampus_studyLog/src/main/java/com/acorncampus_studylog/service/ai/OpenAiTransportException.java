package com.acorncampus_studylog.service.ai;

class OpenAiTransportException extends Exception {

    private final OpenAiErrorCode code;

    OpenAiTransportException(OpenAiErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    OpenAiErrorCode getCode() {
        return code;
    }
}
