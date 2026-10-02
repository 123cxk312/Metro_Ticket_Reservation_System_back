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
@TableName("stations")
public class Station {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("station_code")
    private String stationCode;

    @TableField("station_name")
    private String stationName;

    private String city;
    private Integer status;
    private Integer version;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
