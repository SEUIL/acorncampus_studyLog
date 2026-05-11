package com.acorncampus_studylog.service.ai;

import com.acorncampus_studylog.util.OpenAiConfig;

import java.time.Duration;

final class OpenAiConfigSettings implements OpenAiClientSettings {

    private final OpenAiConfig config;

    OpenAiConfigSettings(OpenAiConfig config) {
        this.config = config;
    }

    @Override
    public String requireApiKey() {
        return config.requireApiKey();
    }

    @Override
    public String getModel() {
        return config.getModel();
    }

    @Override
    public Duration getTimeout() {
        return config.getTimeout();
    }
}
