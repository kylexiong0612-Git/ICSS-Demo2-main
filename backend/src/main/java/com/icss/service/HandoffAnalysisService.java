package com.icss.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icss.dto.ConversationMessageDto;
import com.icss.dto.HandoffRequest;
import com.icss.model.AgentWorkflowRouteRule;
import com.icss.model.AgentWorkflowStage;
import com.icss.model.HandoffAnalysis;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HandoffAnalysisService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final WorkflowConfigService workflowConfigService;
    private final OkHttpClient httpClient;

    @Value("${zhipu.api-key}")
    private String apiKey;

    @Value("${zhipu.base-url}")
    private String baseUrl;

    public HandoffAnalysisService(WorkflowConfigService workflowConfigService) {
        this.workflowConfigService = workflowConfigService;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public HandoffAnalysis analyzeAndRoute(HandoffRequest request) {
        HandoffAnalysis analysis = analyzeByAi(request.getMessages());
        if (analysis == null) {
            analysis = fallbackAnalysis(request.getMessages());
            analysis.setRouteReason("fallback:ai-error");
        }

        normalizeAnalysis(analysis, request.getMessages());
        AgentWorkflowRouteRule routeRule = workflowConfigService.getRouteRule(analysis.getIntentCode());
        if (routeRule == null || Boolean.FALSE.equals(routeRule.getEnabled())) {
            routeRule = workflowConfigService.getRouteRule("general-service");
            analysis.setRouteReason(defaultReason(analysis.getRouteReason(), "fallback:route-rule"));
        }

        if (routeRule != null) {
            analysis.setTargetWorkflowCode(routeRule.getTargetWorkflowCode());
            analysis.setTargetStageCode(routeRule.getEntryStageCode());
        }

        AgentWorkflowStage entryStage = workflowConfigService.resolveStage(
                analysis.getTargetWorkflowCode(),
                analysis.getTargetStageCode()
        );
        if (entryStage == null) {
            entryStage = workflowConfigService.resolveStage("ops-service-flow", "L1");
            analysis.setTargetWorkflowCode("ops-service-flow");
            analysis.setTargetStageCode("L1");
            analysis.setRouteReason(defaultReason(analysis.getRouteReason(), "fallback:missing-stage"));
        } else {
            analysis.setTargetWorkflowCode(entryStage.getWorkflowCode());
            analysis.setTargetStageCode(entryStage.getCode());
        }

        if (analysis.getConfidence() == null) {
            analysis.setConfidence(0.60D);
        }
        if (analysis.getRouteReason() == null || analysis.getRouteReason().isBlank()) {
            analysis.setRouteReason("route:rule-match");
        }
        return analysis;
    }

    private HandoffAnalysis analyzeByAi(List<ConversationMessageDto> messages) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }

        String transcript = messages.stream()
                .map(message -> message.getRole() + ": " + message.getContent())
                .collect(Collectors.joining("\n"));
        Map<String, Object> body = Map.of(
                "model", "glm-4-flash",
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "你是保险客服任务路由助手。请识别客户转人工后的服务意图，并仅返回 JSON。格式为："
                                        + "{\"handoffNeeded\":true,\"intentCode\":\"general-service\",\"intentName\":\"通用服务\","
                                        + "\"summary\":\"...\",\"suggestion\":\"...\",\"tags\":[\"...\"],\"confidence\":0.0}"
                                        + " intentCode 仅允许：policy-service, claims-service, underwriting-service, complaint-service, channel-support, system-fault, general-service"
                        ),
                        Map.of("role", "user", "content", "聊天记录如下：\n" + transcript)
                ),
                "response_format", Map.of("type", "json_object")
        );

        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .post(RequestBody.create(toJson(body), JSON_MEDIA_TYPE))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.warn("[HandoffAnalysis] AI analysis failed with code {}", response.code());
                return null;
            }
            JsonNode root = MAPPER.readTree(response.body().string());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                return null;
            }
            JsonNode parsed = MAPPER.readTree(contentNode.asText());
            HandoffAnalysis analysis = new HandoffAnalysis();
            analysis.setHandoffNeeded(parsed.path("handoffNeeded").asBoolean(true));
            analysis.setIntentCode(parsed.path("intentCode").asText(null));
            analysis.setIntentName(parsed.path("intentName").asText(null));
            analysis.setSummary(parsed.path("summary").asText(null));
            analysis.setSuggestion(parsed.path("suggestion").asText(null));
            analysis.setConfidence(parsed.path("confidence").isNumber() ? parsed.path("confidence").asDouble() : null);
            if (parsed.path("tags").isArray()) {
                List<String> tags = new ArrayList<>();
                parsed.path("tags").forEach(tag -> tags.add(tag.asText()));
                analysis.setTags(tags);
            }
            return analysis;
        } catch (IOException e) {
            log.warn("[HandoffAnalysis] AI analysis exception: {}", e.getMessage());
            return null;
        }
    }

    private HandoffAnalysis fallbackAnalysis(List<ConversationMessageDto> messages) {
        String combined = messages.stream()
                .map(ConversationMessageDto::getContent)
                .collect(Collectors.joining(" "));
        String intentCode = detectIntentByKeywords(combined);
        HandoffAnalysis analysis = new HandoffAnalysis();
        analysis.setHandoffNeeded(Boolean.TRUE);
        analysis.setIntentCode(intentCode);
        analysis.setIntentName(intentNameOf(intentCode));
        analysis.setSummary(extractSummary(messages));
        analysis.setSuggestion(suggestionOf(intentCode));
        analysis.setTags(List.of(intentNameOf(intentCode)));
        analysis.setConfidence("general-service".equals(intentCode) ? 0.45D : 0.78D);
        return analysis;
    }

    private void normalizeAnalysis(HandoffAnalysis analysis, List<ConversationMessageDto> messages) {
        if (analysis.getHandoffNeeded() == null) {
            analysis.setHandoffNeeded(Boolean.TRUE);
        }
        String intentCode = sanitizeIntentCode(analysis.getIntentCode());
        analysis.setIntentCode(intentCode);
        if (analysis.getIntentName() == null || analysis.getIntentName().isBlank()) {
            analysis.setIntentName(intentNameOf(intentCode));
        }
        if (analysis.getSummary() == null || analysis.getSummary().isBlank()) {
            analysis.setSummary(extractSummary(messages));
        }
        if (analysis.getSuggestion() == null || analysis.getSuggestion().isBlank()) {
            analysis.setSuggestion(suggestionOf(intentCode));
        }
        if (analysis.getTags() == null || analysis.getTags().isEmpty()) {
            analysis.setTags(List.of(intentNameOf(intentCode)));
        }
    }

    private String sanitizeIntentCode(String intentCode) {
        if (intentCode == null || intentCode.isBlank()) {
            return "general-service";
        }
        return switch (intentCode) {
            case "policy-service", "claims-service", "underwriting-service",
                    "complaint-service", "channel-support", "system-fault", "general-service" -> intentCode;
            default -> "general-service";
        };
    }

    private String detectIntentByKeywords(String text) {
        String value = text == null ? "" : text.toLowerCase(Locale.ROOT);
        if (containsAny(value, "报障", "故障", "报错", "异常", "登录不了", "打不开", "导出失败", "卡顿", "系统")) {
            return "system-fault";
        }
        if (containsAny(value, "理赔", "报销", "赔付", "住院", "事故")) {
            return "claims-service";
        }
        if (containsAny(value, "核保", "体检", "加费", "拒保")) {
            return "underwriting-service";
        }
        if (containsAny(value, "投诉", "建议", "不满意")) {
            return "complaint-service";
        }
        if (containsAny(value, "渠道", "代理人", "佣金", "工号", "展业")) {
            return "channel-support";
        }
        if (containsAny(value, "保单", "续费", "保全", "受益人", "退保", "变更")) {
            return "policy-service";
        }
        return "general-service";
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String intentNameOf(String intentCode) {
        return switch (intentCode) {
            case "policy-service" -> "保单服务";
            case "claims-service" -> "理赔服务";
            case "underwriting-service" -> "核保服务";
            case "complaint-service" -> "投诉建议";
            case "channel-support" -> "渠道支持";
            case "system-fault" -> "系统报障";
            default -> "通用服务";
        };
    }

    private String suggestionOf(String intentCode) {
        return switch (intentCode) {
            case "system-fault" -> "建议优先收集报错现象、影响范围与复现路径，并转交系统报障链路。";
            case "complaint-service" -> "建议优先安抚客户情绪，明确诉求并由专席持续跟进。";
            case "claims-service" -> "建议核对理赔材料、事故时间及保单状态后再进入人工服务。";
            case "underwriting-service" -> "建议准备核保资料、既往病史与体检报告供人工坐席研判。";
            default -> "建议由人工坐席接入，结合历史对话继续处理。";
        };
    }

    private String extractSummary(List<ConversationMessageDto> messages) {
        if (messages == null || messages.isEmpty()) {
            return "客户请求转接人工服务";
        }
        for (int i = messages.size() - 1; i >= 0; i--) {
            ConversationMessageDto message = messages.get(i);
            if ("user".equals(message.getRole()) && message.getContent() != null && !message.getContent().isBlank()) {
                String content = message.getContent().trim();
                return content.length() > 60 ? content.substring(0, 60) + "..." : content;
            }
        }
        return "客户请求转接人工服务";
    }

    private String defaultReason(String currentReason, String fallbackReason) {
        return (currentReason == null || currentReason.isBlank()) ? fallbackReason : currentReason;
    }

    private String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize handoff request", e);
        }
    }
}
