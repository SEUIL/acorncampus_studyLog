package com.acorncampus_studylog.service.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenAiWritingClientTest {

    @Test
    @DisplayName("fake OpenAI success returns generated text and fixes model/token settings")
    void generateTextReturnsGeneratedText() {
        FakeTransport transport = new FakeTransport("{\"output_text\":\"다듬은 문장입니다.\"}");
        OpenAiWritingClient client = new OpenAiWritingClient(testSettings(), transport);

        String result = client.generateText("문장을 다듬어 주세요.");

        assertEquals("다듬은 문장입니다.", result);
        assertEquals("gpt-5.4-mini", transport.request.getModel());
        assertEquals(600, transport.request.getMaxOutputTokens());
        assertEquals("문장을 다듬어 주세요.", transport.request.getPrompt());
        assertEquals("test-api-key", transport.apiKey);
        assertEquals(Duration.ofSeconds(25), transport.timeout);
    }

    @Test
    @DisplayName("fake timeout maps to safe OpenAI timeout exception")
    void timeoutMapsToSafeException() {
        FakeTransport transport = new FakeTransport(new HttpTimeoutException("raw timeout body sk-test-api-key"));
        OpenAiWritingClient client = new OpenAiWritingClient(testSettings(), transport);

        OpenAiClientException exception = assertThrows(OpenAiClientException.class,
                () -> client.generateText("문장을 다듬어 주세요."));

        assertEquals(OpenAiErrorCode.OPENAI_TIMEOUT, exception.getCode());
        assertFalse(exception.getMessage().contains("sk-test-api-key"));
        assertFalse(exception.getMessage().contains("문장을 다듬어 주세요."));
    }

    @Test
    @DisplayName("rate limit transport error maps to safe application exception")
    void rateLimitMapsToSafeException() {
        FakeTransport transport = new FakeTransport(
                new OpenAiTransportException(OpenAiErrorCode.OPENAI_RATE_LIMIT, "raw provider body"));
        OpenAiWritingClient client = new OpenAiWritingClient(testSettings(), transport);

        OpenAiClientException exception = assertThrows(OpenAiClientException.class,
                () -> client.generateText("문장을 다듬어 주세요."));

        assertEquals(OpenAiErrorCode.OPENAI_RATE_LIMIT, exception.getCode());
        assertFalse(exception.getMessage().contains("raw provider body"));
    }

    @Test
    @DisplayName("missing config maps to safe application exception")
    void missingConfigMapsToSafeException() {
        OpenAiClientSettings settings = new OpenAiClientSettings() {
            @Override
            public String requireApiKey() {
                throw new IllegalStateException("missing raw config value");
            }

            @Override
            public String getModel() {
                return "gpt-5.4-mini";
            }

            @Override
            public Duration getTimeout() {
                return Duration.ofSeconds(25);
            }
        };
        OpenAiWritingClient client = new OpenAiWritingClient(settings, new FakeTransport("{}"));

        OpenAiClientException exception = assertThrows(OpenAiClientException.class,
                () -> client.generateText("문장을 다듬어 주세요."));

        assertEquals(OpenAiErrorCode.OPENAI_CONFIG_MISSING, exception.getCode());
        assertFalse(exception.getMessage().contains("missing raw config value"));
    }

    @Test
    @DisplayName("bad OpenAI body maps to safe bad response exception")
    void badBodyMapsToSafeException() {
        OpenAiWritingClient client = new OpenAiWritingClient(testSettings(), new FakeTransport("{\"output\":[]}"));

        OpenAiClientException exception = assertThrows(OpenAiClientException.class,
                () -> client.generateText("문장을 다듬어 주세요."));

        assertEquals(OpenAiErrorCode.OPENAI_BAD_RESPONSE, exception.getCode());
    }

    private OpenAiClientSettings testSettings() {
        return new OpenAiClientSettings() {
            @Override
            public String requireApiKey() {
                return "test-api-key";
            }

            @Override
            public String getModel() {
                return "gpt-5.4-mini";
            }

            @Override
            public Duration getTimeout() {
                return Duration.ofSeconds(25);
            }
        };
    }

    private static class FakeTransport implements OpenAiTransport {
        private final String responseBody;
        private final Exception exception;
        private OpenAiRequest request;
        private String apiKey;
        private Duration timeout;

        private FakeTransport(String responseBody) {
            this.responseBody = responseBody;
            this.exception = null;
        }

        private FakeTransport(Exception exception) {
            this.responseBody = null;
            this.exception = exception;
        }

        @Override
        public String send(OpenAiRequest request, String apiKey, Duration timeout)
                throws IOException, InterruptedException, OpenAiTransportException {
            this.request = request;
            this.apiKey = apiKey;
            this.timeout = timeout;
            if (exception instanceof IOException) {
                throw (IOException) exception;
            }
            if (exception instanceof InterruptedException) {
                throw (InterruptedException) exception;
            }
            if (exception instanceof OpenAiTransportException) {
                throw (OpenAiTransportException) exception;
            }
            return responseBody;
        }
    }
}
