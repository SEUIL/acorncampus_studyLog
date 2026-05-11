package com.acorncampus_studylog.service.ai;

import java.time.Duration;

interface OpenAiClientSettings {
    String requireApiKey();

    String getModel();

    Duration getTimeout();
}
