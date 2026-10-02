package com.example.metro.service;

import com.example.metro.config.DeepSeekProperties;
import com.example.metro.domain.enums.UserRole;
import com.example.metro.dto.ai.AiChatMessage;
import com.example.metro.dto.ai.AiChatRequest;
import com.example.metro.dto.ai.AiChatResponse;
import com.example.metro.dto.ticket.TicketSearchItem;
import com.example.metro.exception.BusinessException;
import com.example.metro.service.ai.AiToolResult;
import com.example.metro.service.ai.TicketAiToolService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiChatService {

    private static final String SYSTEM_PROMPT = """
            你是地铁票务预约系统的 AI 助手。
            你必须使用提供的工具查询实时车票或订单数据，不能编造车票、库存、价格和订单状态。
            当缺少出发站或到达站时，应先向用户追问。
            使用简洁、准确的中文回答，并说明线路、起终点、日期、时间、价格和余票。
            普通用户只能查询自己的订单，管理员可以查询待处理退票。
            """;

    private final DeepSeekProperties properties;
    private final TicketAiToolService ticketAiToolService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public AiChatService(
            DeepSeekProperties properties,
            TicketAiToolService ticketAiToolService,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.ticketAiToolService = ticketAiToolService;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public AiChatResponse chat(Long userId, UserRole role, AiChatRequest request) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new BusinessException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "AI 服务尚未配置，请设置 DEEPSEEK_API_KEY"
            );
        }

        List<Map<String, Object>> messages = createInitialMessages(request);
        List<Map<String, Object>> tools = ticketAiToolService.toolDefinitions(role);
        Map<Long, TicketSearchItem> ticketMap = new LinkedHashMap<>();

        for (int round = 0; round < properties.maxToolRounds(); round++) {
            JsonNode response = requestDeepSeek(messages, tools);
            JsonNode message = response.path("choices").path(0).path("message");
            JsonNode toolCalls = message.path("tool_calls");

            if (!toolCalls.isArray() || toolCalls.isEmpty()) {
                String answer = message.path("content").asText("暂时无法回答，请换一种方式提问。");
                return new AiChatResponse(
                        answer,
                        new ArrayList<>(ticketMap.values()),
                        false
                );
            }

            messages.add(toMap(message));

            for (JsonNode toolCall : toolCalls) {
                String toolCallId = toolCall.path("id").asText();
                JsonNode function = toolCall.path("function");
                String toolName = function.path("name").asText();
                JsonNode arguments = parseArguments(function.path("arguments"));

                AiToolResult result = ticketAiToolService.execute(
                        toolName,
                        arguments,
                        userId,
                        role
                );

                result.tickets().forEach(ticket -> ticketMap.put(ticket.getId(), ticket));
                messages.add(Map.of(
                        "role", "tool",
                        "tool_call_id", toolCallId,
                        "content", result.content()
                ));
            }
        }

        throw new BusinessException(
                HttpStatus.BAD_GATEWAY,
                "AI 连续调用工具次数过多，请简化问题后重试"
        );
    }

    private List<Map<String, Object>> createInitialMessages(AiChatRequest request) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", SYSTEM_PROMPT
        ));

        for (AiChatMessage historyMessage : request.history()) {
            String role = normalizeHistoryRole(historyMessage.role());
            messages.add(Map.of(
                    "role", role,
                    "content", historyMessage.content()
            ));
        }

        messages.add(Map.of(
                "role", "user",
                "content", request.message()
        ));

        return messages;
    }

    private String normalizeHistoryRole(String role) {
        return "assistant".equalsIgnoreCase(role) ? "assistant" : "user";
    }

    private JsonNode requestDeepSeek(
            List<Map<String, Object>> messages,
            List<Map<String, Object>> tools
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.model());
        body.put("messages", messages);
        body.put("tools", tools);
        body.put("tool_choice", "auto");
        body.put("temperature", 0.2);
        body.put("stream", false);

        try {
            JsonNode response = restClient.post()
                    .uri("/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new BusinessException(HttpStatus.BAD_GATEWAY, "AI 服务没有返回结果");
            }

            return response;
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "AI 服务调用失败，请稍后重试");
        }
    }

    private JsonNode parseArguments(JsonNode arguments) {
        if (arguments == null || arguments.isNull()) {
            return objectMapper.createObjectNode();
        }

        if (!arguments.isTextual()) {
            return arguments;
        }

        try {
            return objectMapper.readTree(arguments.asText());
        } catch (Exception exception) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "AI 工具参数解析失败");
        }
    }

    private Map<String, Object> toMap(JsonNode node) {
        return objectMapper.convertValue(node, new TypeReference<>() {
        });
    }
}
