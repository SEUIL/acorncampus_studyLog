package com.acorncampus_studylog.service.ai;

public class AiWritingResult {

    private final String action;
    private final String generatedText;

    public AiWritingResult(String action, String generatedText) {
        this.action = action;
        this.generatedText = generatedText;
    }

    public String getAction() {
        return action;
    }

    public String getGeneratedText() {
        return generatedText;
    }
}
