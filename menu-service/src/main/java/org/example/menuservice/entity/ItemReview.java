package org.example.menuservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜品评价实体类
 */
@Data
@TableName("item_review")
@Schema(description = "菜品评价")
public class ItemReview implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "评价ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "菜品ID", example = "1")
    private Long itemId;

    @Schema(description = "关联订单ID", example = "1001")
    private Long orderId;

    @Schema(description = "用户ID", example = "100")
    private Long userId;

    @Schema(description = "评分，1-5星", example = "5")
    private Integer rating;

    @Schema(description = "评价内容", example = "味道很好，分量足")
    private String comment;

    @Schema(description = "评价图片URL，多个用逗号分隔", example = "https://example.com/review1.jpg")
    private String images;

    @Schema(description = "是否匿名：0-否，1-是", example = "0")
    private Integer isAnonymous;

    @Schema(description = "点赞数", example = "10")
    private Integer likeCount;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
