package com.acorncampus_studylog.service.ai;

import java.io.IOException;
import java.time.Duration;

interface OpenAiTransport {
    String send(OpenAiRequest request, String apiKey, Duration timeout)
            throws IOException, InterruptedException, OpenAiTransportException;
}
