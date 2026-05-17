package org.example.menuservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单分类更新请求DTO
 */
@Data
@Schema(description = "菜单分类更新请求")
public class MenuCategoryUpdateRequest {

    @Schema(description = "分类ID", example = "1", required = true)
    @NotNull(message = "分类ID不能为空")
    private Long id;

    @Schema(description = "分类名称", example = "热菜", required = true)
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    @Schema(description = "父分类ID，0表示一级分类", example = "0")
    private Long parentId;

    @Schema(description = "排序号，数字越小越靠前", example = "1")
    private Integer sortOrder;

    @Schema(description = "分类图标URL", example = "https://example.com/icon.jpg")
    private String iconUrl;

    @Schema(description = "是否可见：0-隐藏，1-显示", example = "1")
    private Integer isVisible;
}
