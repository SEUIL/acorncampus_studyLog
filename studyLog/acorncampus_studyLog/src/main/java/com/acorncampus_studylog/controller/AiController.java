package com.acorncampus_studylog.controller;

import com.acorncampus_studylog.service.ai.AiWritingService;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/l_check/ai/*")
public class AiController extends HttpServlet {

    static final int MAX_REQUEST_BODY_BYTES = 8 * 1024;
    static final int HTTP_TOO_MANY_REQUESTS = 429;

    private final AiAssistEndpoint endpoint;

    public AiController() {
        this(new AiAssistEndpoint(new AiWritingService(), new Gson()));
    }

    AiController(AiAssistEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        endpoint.handleGet(resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        endpoint.handlePost(req, resp);
    }
}
