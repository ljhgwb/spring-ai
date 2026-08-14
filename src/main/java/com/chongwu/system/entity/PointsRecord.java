package com.chongwu.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 积分记录实体类
 */
@Data
@TableName("points_record")
public class PointsRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String type;

    private Integer points;

    private String description;

    private Long orderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}