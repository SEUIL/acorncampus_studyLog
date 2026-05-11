package com.acorncampus_studylog.service.ai;

import com.acorncampus_studylog.util.OpenAiConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.util.Objects;

public class OpenAiWritingClient {

    public static final int MAX_OUTPUT_TOKENS = 600;

    private final OpenAiClientSettings settings;
    private final OpenAiTransport transport;

    public OpenAiWritingClient() {
        this(new OpenAiConfigSettings(OpenAiConfig.getInstance()), new OpenAiHttpTransport());
    }

    OpenAiWritingClient(OpenAiClientSettings settings, OpenAiTransport transport) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    public String generateText(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt is required");
        }

        String apiKey = requireApiKey();
        OpenAiRequest request = new OpenAiRequest(settings.getModel(), prompt, MAX_OUTPUT_TOKENS);

        try {
            String responseBody = transport.send(request, apiKey, settings.getTimeout());
            return extractGeneratedText(responseBody);
        } catch (OpenAiTransportException e) {
            throw toClientException(e.getCode(), e);
        } catch (HttpTimeoutException e) {
            throw toClientException(OpenAiErrorCode.OPENAI_TIMEOUT, e);
        } catch (IOException e) {
            throw toClientException(OpenAiErrorCode.OPENAI_SERVER_ERROR, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw toClientException(OpenAiErrorCode.OPENAI_TIMEOUT, e);
        }
    }

    private String requireApiKey() {
        try {
            return settings.requireApiKey();
        } catch (IllegalStateException e) {
            throw toClientException(OpenAiErrorCode.OPENAI_CONFIG_MISSING, e);
        }
    }

    private String extractGeneratedText(String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            String outputText = getOutputText(root);
            if (outputText != null && !outputText.isBlank()) {
                return outputText;
            }
        } catch (IllegalStateException | JsonSyntaxException e) {
            throw toClientException(OpenAiErrorCode.OPENAI_BAD_RESPONSE, e);
        }
        throw toClientException(OpenAiErrorCode.OPENAI_BAD_RESPONSE, null);
    }

    private String getOutputText(JsonObject root) {
        JsonElement outputText = root.get("output_text");
        if (isString(outputText)) {
            return outputText.getAsString();
        }

        JsonElement output = root.get("output");
        if (!isArray(output)) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (JsonElement item : output.getAsJsonArray()) {
            if (!item.isJsonObject()) {
                continue;
            }
            appendContentText(builder, item.getAsJsonObject().get("content"));
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private void appendContentText(StringBuilder builder, JsonElement content) {
        if (!isArray(content)) {
            return;
        }
        JsonArray contentArray = content.getAsJsonArray();
        for (JsonElement contentItem : contentArray) {
            if (!contentItem.isJsonObject()) {
                continue;
            }
            JsonObject contentObject = contentItem.getAsJsonObject();
            JsonElement text = contentObject.get("text");
            if (isString(text)) {
                builder.append(text.getAsString());
            }
        }
    }

    private boolean isArray(JsonElement element) {
        return element != null && element.isJsonArray();
    }

    private boolean isString(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }

    private OpenAiClientException toClientException(OpenAiErrorCode code, Throwable cause) {
        return new OpenAiClientException(code, toSafeMessage(code), cause);
    }

    private String toSafeMessage(OpenAiErrorCode code) {
        return switch (code) {
            case OPENAI_CONFIG_MISSING -> "AI service is not configured on the server.";
            case OPENAI_TIMEOUT -> "AI response generation timed out. Please try again.";
            case OPENAI_RATE_LIMIT -> "AI service is busy. Please try again later.";
            case OPENAI_SERVER_ERROR -> "AI service is temporarily unavailable.";
            case OPENAI_BAD_RESPONSE -> "AI service returned an invalid response.";
        };
    }
}
