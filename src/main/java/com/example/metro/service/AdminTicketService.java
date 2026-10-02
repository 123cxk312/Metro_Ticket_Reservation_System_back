package com.example.metro.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.metro.domain.entity.LineStation;
import com.example.metro.domain.entity.MetroLine;
import com.example.metro.domain.entity.Station;
import com.example.metro.domain.entity.Ticket;
import com.example.metro.domain.enums.TicketDirection;
import com.example.metro.domain.enums.TicketStatus;
import com.example.metro.dto.ticket.CreateTicketRequest;
import com.example.metro.dto.ticket.TicketSearchItem;
import com.example.metro.exception.BusinessException;
import com.example.metro.mapper.LineStationMapper;
import com.example.metro.mapper.MetroLineMapper;
import com.example.metro.mapper.StationMapper;
import com.example.metro.mapper.TicketMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class AdminTicketService {

    private static final DateTimeFormatter TICKET_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MetroLineMapper metroLineMapper;
    private final StationMapper stationMapper;
    private final LineStationMapper lineStationMapper;
    private final TicketMapper ticketMapper;

    public AdminTicketService(
            MetroLineMapper metroLineMapper,
            StationMapper stationMapper,
            LineStationMapper lineStationMapper,
            TicketMapper ticketMapper
    ) {
        this.metroLineMapper = metroLineMapper;
        this.stationMapper = stationMapper;
        this.lineStationMapper = lineStationMapper;
        this.ticketMapper = ticketMapper;
    }

    @Transactional
    public TicketSearchItem createTicket(Long adminId, CreateTicketRequest request) {
        validateRequest(request);

        MetroLine line = metroLineMapper.selectById(request.lineId());

        if (line == null || line.getStatus() == null || line.getStatus() != 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "线路不存在或已经停用");
        }

        Station startStation = requireEnabledStation(request.startStationId());
        Station endStation = requireEnabledStation(request.endStationId());

        LineStation startLineStation = findLineStation(line.getId(), startStation.getId());
        LineStation endLineStation = findLineStation(line.getId(), endStation.getId());

        if (startLineStation == null || endLineStation == null) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "出发站和到达站必须属于所选线路"
            );
        }

        validateDirection(
                request.direction(),
                startLineStation.getSequenceNo(),
                endLineStation.getSequenceNo()
        );

        LocalDateTime departureDateTime = request.travelDate().atTime(request.departureTime());
        LocalDateTime saleEndAt = departureDateTime.minusMinutes(5);
        LocalDateTime now = LocalDateTime.now();

        if (!saleEndAt.isAfter(now)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "发车时间必须晚于当前时间");
        }

        Ticket ticket = new Ticket();
        ticket.setTicketNo(createTicketNo(line.getLineCode()));
        ticket.setLineId(line.getId());
        ticket.setStartStationId(startStation.getId());
        ticket.setEndStationId(endStation.getId());
        ticket.setDirection(request.direction());
        ticket.setTravelDate(request.travelDate());
        ticket.setDepartureTime(request.departureTime());
        ticket.setArrivalTime(request.arrivalTime());
        ticket.setPrice(request.price());
        ticket.setTotalStock(request.totalStock());
        ticket.setRemainingStock(request.totalStock());
        ticket.setSaleStartAt(now);
        ticket.setSaleEndAt(saleEndAt);
        ticket.setStatus(TicketStatus.ON_SALE);
        ticket.setVersion(0);
        ticket.setCreatedBy(adminId);
        ticket.setCreatedAt(now);

        ticketMapper.insert(ticket);

        TicketSearchItem createdTicket = ticketMapper.findTicketDetail(ticket.getId());

        if (createdTicket == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "车票创建失败");
        }

        return createdTicket;
    }

    @Transactional(readOnly = true)
    public List<TicketSearchItem> listTickets(TicketStatus status, String keyword) {
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        return ticketMapper.findAllForAdmin(status, normalizedKeyword);
    }

    @Transactional
    public TicketSearchItem updateStatus(Long ticketId, TicketStatus status) {
        Ticket ticket = ticketMapper.selectById(ticketId);

        if (ticket == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "车票不存在");
        }

        if (status == TicketStatus.ON_SALE && ticket.getRemainingStock() <= 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "已售罄的车票不能直接重新销售");
        }

        ticketMapper.update(
                null,
                Wrappers.<Ticket>lambdaUpdate()
                        .eq(Ticket::getId, ticketId)
                        .set(Ticket::getStatus, status)
                        .set(Ticket::getVersion, ticket.getVersion() + 1)
        );

        TicketSearchItem updatedTicket = ticketMapper.findTicketDetail(ticketId);

        if (updatedTicket == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "车票不存在");
        }

        return updatedTicket;
    }

    private void validateRequest(CreateTicketRequest request) {
        if (request.startStationId().equals(request.endStationId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "出发站和到达站不能相同");
        }

        if (!request.arrivalTime().isAfter(request.departureTime())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "到达时间必须晚于发车时间");
        }
    }

    private Station requireEnabledStation(Long stationId) {
        Station station = stationMapper.selectById(stationId);

        if (station == null || station.getStatus() == null || station.getStatus() != 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "站点不存在或已经停用");
        }

        return station;
    }

    private LineStation findLineStation(Long lineId, Long stationId) {
        return lineStationMapper.selectOne(
                Wrappers.<LineStation>lambdaQuery()
                        .eq(LineStation::getLineId, lineId)
                        .eq(LineStation::getStationId, stationId)
        );
    }

    private void validateDirection(
            TicketDirection direction,
            Integer startSequence,
            Integer endSequence
    ) {
        if (direction == TicketDirection.UP && startSequence >= endSequence) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "上行方向要求出发站在到达站之前");
        }

        if (direction == TicketDirection.DOWN && startSequence <= endSequence) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "下行方向要求出发站在到达站之后");
        }
    }

    private String createTicketNo(String lineCode) {
        String timePart = LocalDateTime.now().format(TICKET_TIME_FORMAT);
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 4)
                .toUpperCase();

        return "T-" + lineCode + "-" + timePart + randomPart;
    }
}
