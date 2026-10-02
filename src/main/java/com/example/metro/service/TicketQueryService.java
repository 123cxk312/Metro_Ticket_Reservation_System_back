package com.example.metro.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.metro.domain.entity.MetroLine;
import com.example.metro.domain.entity.Station;
import com.example.metro.domain.enums.TicketDirection;
import com.example.metro.dto.ticket.LineOptionResponse;
import com.example.metro.dto.ticket.StationOptionResponse;
import com.example.metro.dto.ticket.TicketSearchItem;
import com.example.metro.exception.BusinessException;
import com.example.metro.mapper.MetroLineMapper;
import com.example.metro.mapper.StationMapper;
import com.example.metro.mapper.TicketMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TicketQueryService {

    private final MetroLineMapper metroLineMapper;
    private final StationMapper stationMapper;
    private final TicketMapper ticketMapper;

    public TicketQueryService(
            MetroLineMapper metroLineMapper,
            StationMapper stationMapper,
            TicketMapper ticketMapper
    ) {
        this.metroLineMapper = metroLineMapper;
        this.stationMapper = stationMapper;
        this.ticketMapper = ticketMapper;
    }

    @Transactional(readOnly = true)
    public List<LineOptionResponse> listLines() {
        return metroLineMapper.selectList(
                        Wrappers.<MetroLine>lambdaQuery()
                                .eq(MetroLine::getStatus, 1)
                                .orderByAsc(MetroLine::getLineCode)
                )
                .stream()
                .map(line -> new LineOptionResponse(
                        line.getId(),
                        line.getLineCode(),
                        line.getLineName(),
                        line.getCity()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StationOptionResponse> listStations() {
        return stationMapper.selectList(
                        Wrappers.<Station>lambdaQuery()
                                .eq(Station::getStatus, 1)
                                .orderByAsc(Station::getStationCode)
                )
                .stream()
                .map(station -> new StationOptionResponse(
                        station.getId(),
                        station.getStationCode(),
                        station.getStationName(),
                        station.getCity()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketSearchItem> searchTickets(
            Long startStationId,
            Long endStationId,
            LocalDate travelDate,
            TicketDirection direction
    ) {
        if (startStationId != null && startStationId.equals(endStationId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "出发站和到达站不能相同");
        }

        return ticketMapper.searchTickets(
                startStationId,
                endStationId,
                travelDate,
                direction
        );
    }
}
