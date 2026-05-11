package com.acorncampus_studylog.service.ai;

public class OpenAiRequest {

    private final String model;
    private final String prompt;
    private final int maxOutputTokens;

    public OpenAiRequest(String model, String prompt, int maxOutputTokens) {
        this.model = model;
        this.prompt = prompt;
        this.maxOutputTokens = maxOutputTokens;
    }

    public String getModel() {
        return model;
    }

    public String getPrompt() {
        return prompt;
    }

    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }
}
