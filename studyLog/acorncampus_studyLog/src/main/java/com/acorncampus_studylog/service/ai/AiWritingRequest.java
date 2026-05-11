package com.acorncampus_studylog.service.ai;

public class AiWritingRequest {

    private String action;
    private String draftText;
    private String customPrompt;

    public AiWritingRequest() {
    }

    public AiWritingRequest(String action, String draftText, String customPrompt) {
        this.action = action;
        this.draftText = draftText;
        this.customPrompt = customPrompt;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDraftText() {
        return draftText;
    }

    public void setDraftText(String draftText) {
        this.draftText = draftText;
    }

    public String getCustomPrompt() {
        return customPrompt;
    }

    public void setCustomPrompt(String customPrompt) {
        this.customPrompt = customPrompt;
    }
}
