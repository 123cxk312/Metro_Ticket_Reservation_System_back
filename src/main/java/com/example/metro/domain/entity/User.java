package com.example.metro.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.metro.domain.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@TableName("users")
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    @JsonIgnore
    @TableField("password_hash")
    private String passwordHash;

    @TableField("real_name")
    private String realName;

    private String phone;

    private UserRole role;

    private Integer status;

    private Integer version;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
