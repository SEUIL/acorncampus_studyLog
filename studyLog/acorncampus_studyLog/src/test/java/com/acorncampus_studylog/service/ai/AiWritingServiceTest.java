package com.acorncampus_studylog.service.ai;

import com.acorncampus_studylog.dao.AiUsageLogDao;
import com.acorncampus_studylog.dto.AiUsageLogDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiWritingServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-05-11T00:00:00Z"), ZoneId.of("UTC"));

    @Test
    @DisplayName("happy path inserts pending, calls OpenAI, then marks success")
    void assistImproveSuccess() {
        List<String> events = new ArrayList<>();
        FakeUsageLogDao usageLogDao = new FakeUsageLogDao(events);
        FakeOpenAiWritingClient openAiClient = new FakeOpenAiWritingClient(events, "다듬은 문장입니다.");
        AiWritingService service = new AiWritingService(usageLogDao, openAiClient, FIXED_CLOCK);

        AiWritingResult result = service.assist(10, new AiWritingRequest("IMPROVE", "  원문입니다.  ", null));

        assertEquals("IMPROVE", result.getAction());
        assertEquals("다듬은 문장입니다.", result.getGeneratedText());
        assertEquals(List.of("cooldown", "insert:IMPROVE", "openai", "success"), events);
        assertEquals(10, usageLogDao.insertedUserId);
        assertEquals(6, usageLogDao.inputChars);
        assertEquals(0, usageLogDao.customPromptChars);
        assertEquals(OpenAiWritingClient.MAX_OUTPUT_TOKENS, usageLogDao.maxOutputTokens);
        assertTrue(openAiClient.prompt.contains("문장 다듬기"));
        assertTrue(openAiClient.prompt.contains("원문입니다."));
    }

    @Test
    @DisplayName("draft text limit is validated before DAO and OpenAI calls")
    void oversizedDraftTextDoesNotCallDaoOrOpenAi() {
        List<String> events = new ArrayList<>();
        FakeUsageLogDao usageLogDao = new FakeUsageLogDao(events);
        FakeOpenAiWritingClient openAiClient = new FakeOpenAiWritingClient(events, "unused");
        AiWritingService service = new AiWritingService(usageLogDao, openAiClient, FIXED_CLOCK);

        assertThrows(AiWritingValidationException.class,
                () -> service.assist(10, new AiWritingRequest("SUMMARY", "가".repeat(3001), null)));

        assertTrue(events.isEmpty());
        assertEquals(0, openAiClient.callCount);
    }

    @Test
    @DisplayName("custom prompt limit is validated before DAO and OpenAI calls")
    void oversizedCustomPromptDoesNotCallDaoOrOpenAi() {
        List<String> events = new ArrayList<>();
        FakeUsageLogDao usageLogDao = new FakeUsageLogDao(events);
        FakeOpenAiWritingClient openAiClient = new FakeOpenAiWritingClient(events, "unused");
        AiWritingService service = new AiWritingService(usageLogDao, openAiClient, FIXED_CLOCK);

        assertThrows(AiWritingValidationException.class,
                () -> service.assist(10, new AiWritingRequest("CUSTOM", "본문", "가".repeat(501))));

        assertTrue(events.isEmpty());
        assertEquals(0, openAiClient.callCount);
    }

    @Test
    @DisplayName("unknown actions are refused before DAO and OpenAI calls")
    void unknownActionDoesNotCallDaoOrOpenAi() {
        List<String> events = new ArrayList<>();
        FakeUsageLogDao usageLogDao = new FakeUsageLogDao(events);
        FakeOpenAiWritingClient openAiClient = new FakeOpenAiWritingClient(events, "unused");
        AiWritingService service = new AiWritingService(usageLogDao, openAiClient, FIXED_CLOCK);

        assertThrows(AiWritingValidationException.class,
                () -> service.assist(10, new AiWritingRequest("TRANSLATE", "본문", null)));

        assertTrue(events.isEmpty());
        assertEquals(0, openAiClient.callCount);
    }

    @Test
    @DisplayName("cooldown returns remaining seconds before insert and OpenAI call")
    void cooldownDoesNotCallOpenAi() {
        List<String> events = new ArrayList<>();
        FakeUsageLogDao usageLogDao = new FakeUsageLogDao(events);
        usageLogDao.recentUsage = new AiUsageLogDto(
                1,
                10,
                "IMPROVE",
                "PENDING",
                10,
                0,
                600,
                Timestamp.from(FIXED_CLOCK.instant().minus(Duration.ofSeconds(5))),
                null,
                null
        );
        FakeOpenAiWritingClient openAiClient = new FakeOpenAiWritingClient(events, "unused");
        AiWritingService service = new AiWritingService(usageLogDao, openAiClient, FIXED_CLOCK);

        AiWritingCooldownException exception = assertThrows(AiWritingCooldownException.class,
                () -> service.assist(10, new AiWritingRequest("SUMMARY", "본문", null)));

        assertEquals(10, exception.getRemainingSeconds());
        assertEquals(List.of("cooldown"), events);
        assertEquals(0, openAiClient.callCount);
    }

    @Test
    @DisplayName("OpenAI failure marks pending usage as failed")
    void openAiFailureMarksUsageFailed() {
        List<String> events = new ArrayList<>();
        FakeUsageLogDao usageLogDao = new FakeUsageLogDao(events);
        FakeOpenAiWritingClient openAiClient = new FakeOpenAiWritingClient(events,
                new OpenAiClientException(OpenAiErrorCode.OPENAI_TIMEOUT, "safe timeout"));
        AiWritingService service = new AiWritingService(usageLogDao, openAiClient, FIXED_CLOCK);

        OpenAiClientException exception = assertThrows(OpenAiClientException.class,
                () -> service.assist(10, new AiWritingRequest("EXPAND", "본문", null)));

        assertEquals(OpenAiErrorCode.OPENAI_TIMEOUT, exception.getCode());
        assertEquals(List.of("cooldown", "insert:EXPAND", "openai", "failed:OPENAI_TIMEOUT"), events);
        assertEquals(100, usageLogDao.failedUsageId);
    }

    @Test
    @DisplayName("all supported actions build action-specific Korean prompts")
    void buildsActionSpecificPrompts() {
        assertPromptContains("IMPROVE", null, "문장 다듬기");
        assertPromptContains("SUMMARY", null, "요약");
        assertPromptContains("EXPAND", null, "늘려쓰기");
        assertPromptContains("TITLE", null, "제목 추천");
        assertPromptContains("TAGS", null, "태그 추천");
        assertPromptContains("CUSTOM", "표현을 더 공손하게 바꿔줘", "안전 규칙");
    }

    @Test
    @DisplayName("custom action requires a custom prompt")
    void customActionRequiresCustomPrompt() {
        List<String> events = new ArrayList<>();
        FakeUsageLogDao usageLogDao = new FakeUsageLogDao(events);
        FakeOpenAiWritingClient openAiClient = new FakeOpenAiWritingClient(events, "unused");
        AiWritingService service = new AiWritingService(usageLogDao, openAiClient, FIXED_CLOCK);

        assertThrows(AiWritingValidationException.class,
                () -> service.assist(10, new AiWritingRequest("CUSTOM", "본문", " ")));

        assertTrue(events.isEmpty());
        assertEquals(0, openAiClient.callCount);
    }

    private void assertPromptContains(String action, String customPrompt, String expectedPromptText) {
        List<String> events = new ArrayList<>();
        FakeUsageLogDao usageLogDao = new FakeUsageLogDao(events);
        FakeOpenAiWritingClient openAiClient = new FakeOpenAiWritingClient(events, "결과");
        AiWritingService service = new AiWritingService(usageLogDao, openAiClient, FIXED_CLOCK);

        service.assist(10, new AiWritingRequest(action, "본문", customPrompt));

        assertTrue(openAiClient.prompt.contains(expectedPromptText));
        assertTrue(openAiClient.prompt.contains("본문"));
        assertFalse(openAiClient.prompt.contains("model"));
    }

    private static OpenAiClientSettings testSettings() {
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

    private static class FakeUsageLogDao extends AiUsageLogDao {
        private final List<String> events;
        private AiUsageLogDto recentUsage;
        private int insertedUserId;
        private int inputChars;
        private int customPromptChars;
        private int maxOutputTokens;
        private int failedUsageId;

        private FakeUsageLogDao(List<String> events) {
            this.events = events;
        }

        @Override
        public AiUsageLogDto findRecentPendingOrSuccess(int userId, int seconds) {
            events.add("cooldown");
            return recentUsage;
        }

        @Override
        public int insertPending(int userId, String action, int inputChars, int customPromptChars, int maxOutputTokens) {
            events.add("insert:" + action);
            this.insertedUserId = userId;
            this.inputChars = inputChars;
            this.customPromptChars = customPromptChars;
            this.maxOutputTokens = maxOutputTokens;
            return 100;
        }

        @Override
        public void markSuccess(int usageId) {
            events.add("success");
        }

        @Override
        public void markFailed(int usageId, String errorCode) {
            events.add("failed:" + errorCode);
            this.failedUsageId = usageId;
        }
    }

    private static class FakeOpenAiWritingClient extends OpenAiWritingClient {
        private final List<String> events;
        private final String response;
        private final OpenAiClientException exception;
        private String prompt;
        private int callCount;

        private FakeOpenAiWritingClient(List<String> events, String response) {
            super(testSettings(), (request, apiKey, timeout) -> response);
            this.events = events;
            this.response = response;
            this.exception = null;
        }

        private FakeOpenAiWritingClient(List<String> events, OpenAiClientException exception) {
            super(testSettings(), (request, apiKey, timeout) -> "unused");
            this.events = events;
            this.response = null;
            this.exception = exception;
        }

        @Override
        public String generateText(String prompt) {
            events.add("openai");
            callCount++;
            this.prompt = prompt;
            if (exception != null) {
                throw exception;
            }
            return response;
        }
    }
}
