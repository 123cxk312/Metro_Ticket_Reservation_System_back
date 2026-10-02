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
@TableName("metro_lines")
public class MetroLine {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("line_code")
    private String lineCode;

    @TableField("line_name")
    private String lineName;

    private String city;
    private Integer status;
    private Integer version;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
