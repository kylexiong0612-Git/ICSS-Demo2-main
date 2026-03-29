package com.icss.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 智谱 AI 代理控制器。
 * 接收前端的 /api/ai/** 请求，注入 API Key 后转发到智谱接口，原样透传响应。
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Value("${zhipu.api-key}")
    private String apiKey;

    @Value("${zhipu.base-url}")
    private String baseUrl;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    @RequestMapping("/**")
    public ResponseEntity<String> proxy(HttpServletRequest servletRequest,
                                        @RequestBody(required = false) String body) {
        // 提取 /api/ai 之后的路径，如 /chat/completions
        String suffix = servletRequest.getRequestURI()
                .replaceFirst("/api/ai", "");
        String targetUrl = baseUrl + suffix;

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[AiProxy] ZHIPU_API_KEY 未配置");
            return ResponseEntity.status(500).body("{\"error\":\"ZHIPU_API_KEY not configured\"}");
        }

        String requestBody = body != null ? body : "{}";
        Request request = new Request.Builder()
                .url(targetUrl)
                .method(servletRequest.getMethod(),
                        okhttp3.RequestBody.create(requestBody,
                                MediaType.parse("application/json; charset=utf-8")))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            return ResponseEntity.status(response.code())
                    .header("Content-Type", "application/json")
                    .body(responseBody);
        } catch (IOException e) {
            log.error("[AiProxy] 转发请求失败: {}", e.getMessage());
            return ResponseEntity.status(502)
                    .body("{\"error\":\"Failed to reach Zhipu API: " + e.getMessage() + "\"}");
        }
    }
}
