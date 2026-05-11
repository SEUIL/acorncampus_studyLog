package com.acorncampus_studylog.util;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

public final class OpenAiConfig {

    private static final String RESOURCE_NAME = "openai.properties";
    private static final String DEFAULT_MODEL = "gpt-4o-mini";
    private static final int DEFAULT_TIMEOUT_SECONDS = 25;

    private static final OpenAiConfig INSTANCE = load();

    private final String apiKey;
    private final String model;
    private final Duration timeout;

    private OpenAiConfig(String apiKey, String model, Duration timeout) {
        this.apiKey = apiKey;
        this.model = model;
        this.timeout = timeout;
    }

    public static OpenAiConfig getInstance() {
        return INSTANCE;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String requireApiKey() {
        if (!isConfigured()) {
            throw new IllegalStateException("OpenAI API key is not configured on the server.");
        }
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public Duration getTimeout() {
        return timeout;
    }

    private static OpenAiConfig load() {
        Properties properties = new Properties();
        try (InputStream inputStream = OpenAiConfig.class.getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("openai.properties load failed", e);
        }

        String apiKey = blankToNull(properties.getProperty("openai.api.key"));
        String model = valueOrDefault(properties.getProperty("openai.model"), DEFAULT_MODEL);
        int timeoutSeconds = parseTimeoutSeconds(properties.getProperty("openai.timeout.seconds"));
        return new OpenAiConfig(apiKey, model, Duration.ofSeconds(timeoutSeconds));
    }

    private static String valueOrDefault(String value, String defaultValue) {
        String trimmed = blankToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static int parseTimeoutSeconds(String value) {
        String trimmed = blankToNull(value);
        if (trimmed == null) {
            return DEFAULT_TIMEOUT_SECONDS;
        }
        try {
            int timeoutSeconds = Integer.parseInt(trimmed);
            return timeoutSeconds > 0 ? timeoutSeconds : DEFAULT_TIMEOUT_SECONDS;
        } catch (NumberFormatException e) {
            return DEFAULT_TIMEOUT_SECONDS;
        }
    }
}
