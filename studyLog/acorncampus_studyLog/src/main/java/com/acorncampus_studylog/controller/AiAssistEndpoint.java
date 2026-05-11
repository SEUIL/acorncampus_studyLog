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
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class AiAssistEndpoint {

    private final AiWritingService aiWritingService;
    private final Gson gson;

    AiAssistEndpoint(AiWritingService aiWritingService, Gson gson) {
        this.aiWritingService = aiWritingService;
        this.gson = gson;
    }

    void handleGet(HttpServletResponse resp) throws IOException {
        writeError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "AI 글쓰기 보조는 POST 요청만 지원합니다.", null);
    }

    void handlePost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (!"/assist.do".equals(path)) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "요청한 AI API를 찾을 수 없습니다.", null);
            return;
        }

        handleAssist(req, resp);
    }

    private void handleAssist(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDto loginUser = getLoginUser(req);
        if (loginUser == null || loginUser.getUserId() <= 0) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다.", null);
            return;
        }

        AiWritingRequest request;
        try {
            request = parseRequest(req);
        } catch (RequestBodyTooLargeException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "REQUEST_BODY_TOO_LARGE", "요청 본문은 8KB 이하로 보내 주세요.", null);
            return;
        } catch (JsonParseException | IllegalStateException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_JSON", "JSON 요청 형식이 올바르지 않습니다.", null);
            return;
        }

        try {
            AiWritingResult result = aiWritingService.assist(loginUser.getUserId(), request);
            writeJson(resp, HttpServletResponse.SC_OK, AiResponse.success(result.getAction(), result.getGeneratedText()));
        } catch (AiWritingValidationException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "VALIDATION_ERROR", e.getMessage(), null);
        } catch (AiWritingCooldownException e) {
            resp.setHeader("Retry-After", String.valueOf(e.getRemainingSeconds()));
            writeError(resp, AiController.HTTP_TOO_MANY_REQUESTS, "COOLDOWN", e.getMessage(), e.getRemainingSeconds());
        } catch (OpenAiClientException e) {
            writeOpenAiError(resp, e.getCode());
        } catch (RuntimeException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "AI_SERVICE_ERROR", "AI 글쓰기 보조 처리 중 오류가 발생했습니다.", null);
        }
    }

    private AiWritingRequest parseRequest(HttpServletRequest req) throws IOException {
        String body = readRequestBody(req);
        if (body.isBlank()) {
            throw new JsonSyntaxException("empty body");
        }
        return gson.fromJson(body, AiWritingRequest.class);
    }

    private String readRequestBody(HttpServletRequest req) throws IOException {
        long contentLength = req.getContentLengthLong();
        if (contentLength > AiController.MAX_REQUEST_BODY_BYTES) {
            throw new RequestBodyTooLargeException();
        }

        try (ServletInputStream inputStream = req.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int totalBytes = 0;
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytes += bytesRead;
                if (totalBytes > AiController.MAX_REQUEST_BODY_BYTES) {
                    throw new RequestBodyTooLargeException();
                }
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toString(StandardCharsets.UTF_8);
        }
    }

    private UserDto getLoginUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object loginUser = session.getAttribute("loginUser");
        return loginUser instanceof UserDto ? (UserDto) loginUser : null;
    }

    private void writeOpenAiError(HttpServletResponse resp, OpenAiErrorCode errorCode) throws IOException {
        switch (errorCode) {
            case OPENAI_CONFIG_MISSING:
                writeError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "AI_CONFIG_MISSING", "AI 서비스 설정이 준비되지 않았습니다.", null);
                break;
            case OPENAI_TIMEOUT:
                writeError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "AI_TIMEOUT", "AI 서비스 응답 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.", null);
                break;
            case OPENAI_RATE_LIMIT:
                writeError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "AI_RATE_LIMIT", "AI 서비스 요청이 일시적으로 많습니다. 잠시 후 다시 시도해 주세요.", null);
                break;
            case OPENAI_SERVER_ERROR:
                writeError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "AI_SERVICE_UNAVAILABLE", "AI 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해 주세요.", null);
                break;
            case OPENAI_BAD_RESPONSE:
            default:
                writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "AI_BAD_RESPONSE", "AI 서비스 응답을 처리하지 못했습니다.", null);
                break;
        }
    }

    private void writeError(HttpServletResponse resp, int statusCode, String code, String message, Integer retryAfterSeconds) throws IOException {
        writeJson(resp, statusCode, AiResponse.error(code, message, retryAfterSeconds));
    }

    private void writeJson(HttpServletResponse resp, int statusCode, AiResponse response) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(response));
    }

    private static class AiResponse {
        private final String status;
        private final String code;
        private final String message;
        private final String action;
        private final String result;
        private final Integer retryAfterSeconds;

        private AiResponse(String status, String code, String message, String action, String result, Integer retryAfterSeconds) {
            this.status = status;
            this.code = code;
            this.message = message;
            this.action = action;
            this.result = result;
            this.retryAfterSeconds = retryAfterSeconds;
        }

        private static AiResponse success(String action, String result) {
            return new AiResponse("ok", null, null, action, result, null);
        }

        private static AiResponse error(String code, String message, Integer retryAfterSeconds) {
            return new AiResponse("error", code, message, null, null, retryAfterSeconds);
        }
    }

    private static class RequestBodyTooLargeException extends RuntimeException {
    }
}
