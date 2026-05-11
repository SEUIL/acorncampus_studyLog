package com.acorncampus_studylog.service.ai;

import com.acorncampus_studylog.util.OpenAiConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class OpenAiRealApiTest {

    @Test
    @DisplayName("opt-in real OpenAI smoke test")
    void generateTextWithRealOpenAiWhenExplicitlyEnabled() {
        assumeTrue(Boolean.getBoolean("openai.realTest"), "Set -Dopenai.realTest=true to run the real OpenAI smoke test.");

        URL configResource = OpenAiRealApiTest.class.getClassLoader().getResource("openai.properties");
        assumeTrue(configResource != null, "Create ignored src/main/resources/openai.properties before running the real OpenAI smoke test.");

        OpenAiConfig config = OpenAiConfig.getInstance();
        assumeTrue(config.isConfigured(), "openai.properties must include openai.api.key for the real OpenAI smoke test.");

        String result = new OpenAiWritingClient().generateText("한국어로 '테스트 성공'만 답해 주세요.");

        assertFalse(result.isBlank());
    }
}
