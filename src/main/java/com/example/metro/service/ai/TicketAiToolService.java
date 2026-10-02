package com.example.metro.service.ai;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.metro.domain.entity.Station;
import com.example.metro.domain.enums.RefundStatus;
import com.example.metro.domain.enums.TicketDirection;
import com.example.metro.domain.enums.UserRole;
import com.example.metro.dto.order.OrderDetailItem;
import com.example.metro.dto.refund.RefundDetailItem;
import com.example.metro.dto.ticket.TicketSearchItem;
import com.example.metro.exception.BusinessException;
import com.example.metro.mapper.RefundRequestMapper;
import com.example.metro.mapper.StationMapper;
import com.example.metro.mapper.TicketMapper;
import com.example.metro.mapper.TicketOrderMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketAiToolService {

    public static final String SEARCH_TICKETS = "searchTickets";
    public static final String GET_MY_ORDERS = "getMyOrders";
    public static final String GET_PENDING_REFUNDS = "getPendingRefunds";

    private final StationMapper stationMapper;
    private final TicketMapper ticketMapper;
    private final TicketOrderMapper ticketOrderMapper;
    private final RefundRequestMapper refundRequestMapper;
    private final ObjectMapper objectMapper;

    public TicketAiToolService(
            StationMapper stationMapper,
            TicketMapper ticketMapper,
            TicketOrderMapper ticketOrderMapper,
            RefundRequestMapper refundRequestMapper,
            ObjectMapper objectMapper
    ) {
        this.stationMapper = stationMapper;
        this.ticketMapper = ticketMapper;
        this.ticketOrderMapper = ticketOrderMapper;
        this.refundRequestMapper = refundRequestMapper;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> toolDefinitions(UserRole role) {
        List<Map<String, Object>> tools = new ArrayList<>();
        tools.add(functionTool(
                SEARCH_TICKETS,
                "按出发站、到达站、日期和方向查询实时车票",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "startStation", Map.of(
                                        "type", "string",
                                        "description", "出发站名称"
                                ),
                                "endStation", Map.of(
                                        "type", "string",
                                        "description", "到达站名称"
                                ),
                                "travelDate", Map.of(
                                        "type", "string",
                                        "description", "乘车日期，格式 yyyy-MM-dd；不明确时可以省略"
                                ),
                                "direction", Map.of(
                                        "type", "string",
                                        "enum", List.of("UP", "DOWN"),
                                        "description", "上行或下行；不明确时可以省略"
                                )
                        ),
                        "required", List.of("startStation", "endStation")
                )
        ));

        tools.add(functionTool(
                GET_MY_ORDERS,
                "查询当前登录用户自己的订单",
                Map.of(
                        "type", "object",
                        "properties", Map.of(),
                        "required", List.of()
                )
        ));

        if (role == UserRole.ADMIN) {
            tools.add(functionTool(
                    GET_PENDING_REFUNDS,
                    "查询所有待管理员处理的退票申请",
                    Map.of(
                            "type", "object",
                            "properties", Map.of(),
                            "required", List.of()
                    )
            ));
        }

        return tools;
    }

    @Transactional(readOnly = true)
    public AiToolResult execute(
            String toolName,
            JsonNode arguments,
            Long userId,
            UserRole role
    ) {
        return switch (toolName) {
            case SEARCH_TICKETS -> searchTickets(arguments);
            case GET_MY_ORDERS -> getMyOrders(userId);
            case GET_PENDING_REFUNDS -> getPendingRefunds(role);
            default -> throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "不支持的 AI 工具：" + toolName
            );
        };
    }

    private AiToolResult searchTickets(JsonNode arguments) {
        String startStationName = text(arguments, "startStation");
        String endStationName = text(arguments, "endStation");

        if (startStationName == null || endStationName == null) {
            return new AiToolResult(
                    "请补充出发站和到达站，例如：明天从 Central Station 到 University 有哪些票？",
                    List.of()
            );
        }

        Station startStation = resolveStation(startStationName);
        Station endStation = resolveStation(endStationName);

        if (startStation == null || endStation == null) {
            return new AiToolResult(
                    "没有找到匹配的站点，请确认出发站和到达站名称。",
                    List.of()
            );
        }

        LocalDate travelDate = parseDate(text(arguments, "travelDate"));
        TicketDirection direction = parseDirection(text(arguments, "direction"));
        List<TicketSearchItem> tickets = ticketMapper.searchTickets(
                startStation.getId(),
                endStation.getId(),
                travelDate,
                direction
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", tickets.isEmpty()
                ? "没有查询到符合条件的车票"
                : "查询到 " + tickets.size() + " 个车票班次");
        result.put("tickets", tickets);

        return new AiToolResult(toJson(result), tickets);
    }

    private AiToolResult getMyOrders(Long userId) {
        List<OrderDetailItem> orders = ticketOrderMapper.findOrdersByUserId(userId);
        return new AiToolResult(toJson(Map.of(
                "message", "共查询到 " + orders.size() + " 条订单",
                "orders", orders
        )), List.of());
    }

    private AiToolResult getPendingRefunds(UserRole role) {
        if (role != UserRole.ADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }

        List<RefundDetailItem> refunds = refundRequestMapper.findRefundsForAdmin(
                RefundStatus.PENDING
        );

        return new AiToolResult(toJson(Map.of(
                "message", "共查询到 " + refunds.size() + " 条待处理退票申请",
                "refunds", refunds
        )), List.of());
    }

    private Station resolveStation(String stationName) {
        List<Station> exactMatches = stationMapper.selectList(
                Wrappers.<Station>lambdaQuery()
                        .eq(Station::getStatus, 1)
                        .eq(Station::getStationName, stationName)
                        .last("LIMIT 2")
        );

        if (exactMatches.size() == 1) {
            return exactMatches.getFirst();
        }

        List<Station> fuzzyMatches = stationMapper.selectList(
                Wrappers.<Station>lambdaQuery()
                        .eq(Station::getStatus, 1)
                        .like(Station::getStationName, stationName)
                        .last("LIMIT 2")
        );

        if (fuzzyMatches.size() == 1) {
            return fuzzyMatches.getFirst();
        }

        return null;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "AI 返回的日期格式不正确");
        }
    }

    private TicketDirection parseDirection(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return TicketDirection.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "AI 返回的方向不正确");
        }
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "AI 工具结果处理失败");
        }
    }

    private Map<String, Object> functionTool(
            String name,
            String description,
            Map<String, Object> parameters
    ) {
        return Map.of(
                "type", "function",
                "function", Map.of(
                        "name", name,
                        "description", description,
                        "parameters", parameters
                )
        );
    }
}
