package com.acorncampus_studylog.service.ai;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

class OpenAiHttpTransport implements OpenAiTransport {

    private static final URI RESPONSES_API_URI = URI.create("https://api.openai.com/v1/responses");

    private final Gson gson;

    OpenAiHttpTransport() {
        this(new Gson());
    }

    OpenAiHttpTransport(Gson gson) {
        this.gson = gson;
    }

    @Override
    public String send(OpenAiRequest request, String apiKey, Duration timeout)
            throws IOException, InterruptedException, OpenAiTransportException {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();

        HttpRequest httpRequest = HttpRequest.newBuilder(RESPONSES_API_URI)
                .timeout(timeout)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(toRequestBody(request)), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return response.body();
        }
        throw new OpenAiTransportException(toErrorCode(statusCode), "OpenAI request failed with status " + statusCode);
    }

    private Map<String, Object> toRequestBody(OpenAiRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", request.getModel());
        body.put("input", request.getPrompt());
        body.put("max_output_tokens", request.getMaxOutputTokens());
        return body;
    }

    private OpenAiErrorCode toErrorCode(int statusCode) {
        if (statusCode == 401 || statusCode == 403) {
            return OpenAiErrorCode.OPENAI_CONFIG_MISSING;
        }
        if (statusCode == 429) {
            return OpenAiErrorCode.OPENAI_RATE_LIMIT;
        }
        if (statusCode >= 500) {
            return OpenAiErrorCode.OPENAI_SERVER_ERROR;
        }
        return OpenAiErrorCode.OPENAI_BAD_RESPONSE;
    }
}
