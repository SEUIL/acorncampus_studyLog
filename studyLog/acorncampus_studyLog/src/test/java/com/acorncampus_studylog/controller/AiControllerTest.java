package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.dto.UserDto;
import com.acorncampus_studylog.service.ai.AiWritingCooldownException;
import com.acorncampus_studylog.service.ai.AiWritingRequest;
import com.acorncampus_studylog.service.ai.AiWritingResult;
import com.acorncampus_studylog.service.ai.AiWritingService;
import com.acorncampus_studylog.service.ai.AiWritingValidationException;
import com.acorncampus_studylog.service.ai.OpenAiClientException;
import com.acorncampus_studylog.service.ai.OpenAiErrorCode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiControllerTest {

    @Test
    void assistReturnsSuccessJsonForLoggedInUser() throws Exception {
        FakeAiWritingService service = new FakeAiWritingService(new AiWritingResult("SUMMARY", "요약 결과"));
        AiAssistEndpoint endpoint = new AiAssistEndpoint(service, new Gson());
        TestResponse response = new TestResponse();

        endpoint.handlePost(request("/assist.do", "{\"action\":\"SUMMARY\",\"draftText\":\"원문\"}", loginUser(42)), response.proxy());

        JsonObject json = response.json();
        assertEquals(HttpServletResponse.SC_OK, response.status);
        assertEquals("application/json; charset=UTF-8", response.contentType);
        assertEquals("ok", json.get("status").getAsString());
        assertEquals("SUMMARY", json.get("action").getAsString());
        assertEquals("요약 결과", json.get("result").getAsString());
        assertEquals(42, service.userId);
        assertEquals("SUMMARY", service.request.getAction());
        assertEquals("원문", service.request.getDraftText());
    }

    @Test
    void assistReturnsUnauthorizedJsonWhenSessionUserMissing() throws Exception {
        FakeAiWritingService service = new FakeAiWritingService(new AiWritingResult("SUMMARY", "unused"));
        AiAssistEndpoint endpoint = new AiAssistEndpoint(service, new Gson());
        TestResponse response = new TestResponse();

        endpoint.handlePost(request("/assist.do", "{\"action\":\"SUMMARY\",\"draftText\":\"원문\"}", null), response.proxy());

        JsonObject json = response.json();
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.status);
        assertEquals("error", json.get("status").getAsString());
        assertEquals("UNAUTHORIZED", json.get("code").getAsString());
        assertFalse(service.called);
    }

    @Test
    void assistRejectsBodyLargerThanEightKbBeforeServiceCall() throws Exception {
        FakeAiWritingService service = new FakeAiWritingService(new AiWritingResult("SUMMARY", "unused"));
        AiAssistEndpoint endpoint = new AiAssistEndpoint(service, new Gson());
        TestResponse response = new TestResponse();

        endpoint.handlePost(request("/assist.do", "a".repeat(AiController.MAX_REQUEST_BODY_BYTES + 1), loginUser(7)), response.proxy());

        JsonObject json = response.json();
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status);
        assertEquals("REQUEST_BODY_TOO_LARGE", json.get("code").getAsString());
        assertFalse(service.called);
    }

    @Test
    void assistMapsValidationErrorsToBadRequestJson() throws Exception {
        FakeAiWritingService service = new FakeAiWritingService(new AiWritingValidationException("초안 내용은 필수입니다."));
        AiAssistEndpoint endpoint = new AiAssistEndpoint(service, new Gson());
        TestResponse response = new TestResponse();

        endpoint.handlePost(request("/assist.do", "{\"action\":\"SUMMARY\",\"draftText\":\"\"}", loginUser(7)), response.proxy());

        JsonObject json = response.json();
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.status);
        assertEquals("VALIDATION_ERROR", json.get("code").getAsString());
        assertEquals("초안 내용은 필수입니다.", json.get("message").getAsString());
    }

    @Test
    void assistMapsCooldownToTooManyRequestsJson() throws Exception {
        FakeAiWritingService service = new FakeAiWritingService(new AiWritingCooldownException(12));
        AiAssistEndpoint endpoint = new AiAssistEndpoint(service, new Gson());
        TestResponse response = new TestResponse();

        endpoint.handlePost(request("/assist.do", "{\"action\":\"SUMMARY\",\"draftText\":\"원문\"}", loginUser(7)), response.proxy());

        JsonObject json = response.json();
        assertEquals(AiController.HTTP_TOO_MANY_REQUESTS, response.status);
        assertEquals("COOLDOWN", json.get("code").getAsString());
        assertEquals(12, json.get("retryAfterSeconds").getAsInt());
        assertEquals("12", response.headers.get("Retry-After"));
    }

    @Test
    void assistMapsOpenAiTransportErrorsToServiceUnavailableJson() throws Exception {
        FakeAiWritingService service = new FakeAiWritingService(
                new OpenAiClientException(OpenAiErrorCode.OPENAI_RATE_LIMIT, "raw upstream detail"));
        AiAssistEndpoint endpoint = new AiAssistEndpoint(service, new Gson());
        TestResponse response = new TestResponse();

        endpoint.handlePost(request("/assist.do", "{\"action\":\"SUMMARY\",\"draftText\":\"원문\"}", loginUser(7)), response.proxy());

        JsonObject json = response.json();
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.status);
        assertEquals("AI_RATE_LIMIT", json.get("code").getAsString());
        assertFalse(json.get("message").getAsString().contains("raw upstream detail"));
    }

    @Test
    void assistMapsRuntimeErrorsToInternalServerErrorJson() throws Exception {
        FakeAiWritingService service = new FakeAiWritingService(new RuntimeException("database detail"));
        AiAssistEndpoint endpoint = new AiAssistEndpoint(service, new Gson());
        TestResponse response = new TestResponse();

        endpoint.handlePost(request("/assist.do", "{\"action\":\"SUMMARY\",\"draftText\":\"원문\"}", loginUser(7)), response.proxy());

        JsonObject json = response.json();
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.status);
        assertEquals("AI_SERVICE_ERROR", json.get("code").getAsString());
        assertFalse(json.get("message").getAsString().contains("database detail"));
    }

    @Test
    void doGetDoesNotAcceptGeneration() throws Exception {
        AiAssistEndpoint endpoint = new AiAssistEndpoint(new FakeAiWritingService(new AiWritingResult("SUMMARY", "unused")), new Gson());
        TestResponse response = new TestResponse();

        endpoint.handleGet(response.proxy());

        JsonObject json = response.json();
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.status);
        assertEquals("METHOD_NOT_ALLOWED", json.get("code").getAsString());
    }

    private static HttpServletRequest request(String pathInfo, String body, UserDto loginUser) {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        HttpSession session = loginUser == null ? null : session(loginUser);
        InvocationHandler handler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "getPathInfo":
                    return pathInfo;
                case "getSession":
                    if (args != null && args.length == 1 && Boolean.FALSE.equals(args[0])) {
                        return session;
                    }
                    return session;
                case "getContentLengthLong":
                    return (long) bodyBytes.length;
                case "getInputStream":
                    return new TestServletInputStream(bodyBytes);
                default:
                    return defaultValue(method.getReturnType());
            }
        };
        return (HttpServletRequest) Proxy.newProxyInstance(
                AiControllerTest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                handler);
    }

    private static HttpSession session(UserDto loginUser) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("getAttribute".equals(method.getName()) && "loginUser".equals(args[0])) {
                return loginUser;
            }
            return defaultValue(method.getReturnType());
        };
        return (HttpSession) Proxy.newProxyInstance(
                AiControllerTest.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                handler);
    }

    private static UserDto loginUser(int userId) {
        UserDto user = new UserDto();
        user.setUserId(userId);
        return user;
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0f;
        }
        if (returnType == double.class) {
            return 0d;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == char.class) {
            return (char) 0;
        }
        return null;
    }

    private static class FakeAiWritingService extends AiWritingService {
        private final AiWritingResult result;
        private final RuntimeException exception;
        private boolean called;
        private int userId;
        private AiWritingRequest request;

        private FakeAiWritingService(AiWritingResult result) {
            this.result = result;
            this.exception = null;
        }

        private FakeAiWritingService(RuntimeException exception) {
            this.result = null;
            this.exception = exception;
        }

        @Override
        public AiWritingResult assist(int userId, AiWritingRequest request) {
            this.called = true;
            this.userId = userId;
            this.request = request;
            if (exception != null) {
                throw exception;
            }
            assertNotNull(result);
            return result;
        }
    }

    private static class TestResponse {
        private final StringWriter body = new StringWriter();
        private final Map<String, String> headers = new HashMap<>();
        private int status;
        private String contentType;
        private String characterEncoding;

        private HttpServletResponse proxy() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "setStatus":
                        status = (Integer) args[0];
                        return null;
                    case "setContentType":
                        contentType = (String) args[0];
                        return null;
                    case "setCharacterEncoding":
                        characterEncoding = (String) args[0];
                        return null;
                    case "setHeader":
                        headers.put((String) args[0], (String) args[1]);
                        return null;
                    case "getWriter":
                        return new PrintWriter(body);
                    default:
                        return defaultValue(method.getReturnType());
                }
            };
            return (HttpServletResponse) Proxy.newProxyInstance(
                    AiControllerTest.class.getClassLoader(),
                    new Class<?>[]{HttpServletResponse.class},
                    handler);
        }

        private JsonObject json() {
            assertEquals("UTF-8", characterEncoding);
            return JsonParser.parseString(body.toString()).getAsJsonObject();
        }
    }

    private static class TestServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        private TestServletInputStream(byte[] body) {
            this.inputStream = new ByteArrayInputStream(body);
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
    }
}
