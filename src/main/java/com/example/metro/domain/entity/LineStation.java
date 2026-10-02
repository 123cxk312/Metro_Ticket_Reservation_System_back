package com.example.metro.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@TableName("line_stations")
public class LineStation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("line_id")
    private Long lineId;

    @TableField("station_id")
    private Long stationId;

    @TableField("sequence_no")
    private Integer sequenceNo;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
