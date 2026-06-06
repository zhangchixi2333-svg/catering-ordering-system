package org.example.menuservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.menuservice.common.Result;
import org.example.menuservice.dto.MenuCategoryCreateRequest;
import org.example.menuservice.dto.MenuCategoryUpdateRequest;
import org.example.menuservice.entity.MenuCategory;
import org.example.menuservice.feign.ShopFeignClient;
import org.example.menuservice.service.MenuCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单分类控制器
 */
@Tag(name = "菜单分类管理", description = "菜品分类的增删改查")
@RestController
@RequestMapping("/api/menu/category")
@RequiredArgsConstructor
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;
    private final ShopFeignClient shopFeignClient;

    @Operation(summary = "获取所有分类列表")
    @GetMapping("/list")
    public Result<List<MenuCategory>> listCategories() {
        List<MenuCategory> categories = menuCategoryService.list();
        return Result.success(categories);
    }

    @Operation(summary = "根据店铺ID获取分类列表")
    @GetMapping("/shop/{shopId}")
    public Result<List<MenuCategory>> getCategoriesByShop(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        List<MenuCategory> categories = menuCategoryService.getByShopId(shopId);
        return Result.success(categories);
    }

    @Operation(summary = "获取可见的分类列表")
    @GetMapping("/visible/{shopId}")
    public Result<List<MenuCategory>> getVisibleCategories(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        List<MenuCategory> categories = menuCategoryService.getVisibleCategories(shopId);
        return Result.success(categories);
    }

    @Operation(summary = "根据ID获取分类详情")
    @GetMapping("/{id}")
    public Result<MenuCategory> getCategoryById(
            @Parameter(description = "分类ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        MenuCategory category = menuCategoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        return Result.success(category);
    }

    @Operation(
        summary = "创建分类（验证店铺）",
        description = "<font color='red'>【优化】</font><br/>" +
                "创建新的菜品分类，系统会自动验证店铺信息<br/><br/>" +
                "<font color='green'>业务规则：</font><br/>" +
                "1. 调用 shop-service 验证店铺是否存在 - 不存在则返回错误<br/>" +
                "2. 设置默认值：isVisible=1, parentId=0<br/>" +
                "3. 一个店铺可以有多个分类"
    )
    @PostMapping
    public Result<Boolean> createCategory(@RequestBody @Valid MenuCategoryCreateRequest request) {
        // 1. 验证店铺是否存在
        Result<ShopFeignClient.ShopInfoDTO> shopResult = null;
        try {
            shopResult = shopFeignClient.getShopById(request.getShopId());
        } catch (Exception e) {
            return Result.error("店铺服务暂时不可用，请稍后重试");
        }
        
        if (shopResult == null || shopResult.getData() == null) {
            return Result.error("店铺不存在，ID: " + request.getShopId());
        }
        
        // 2. 创建分类
        MenuCategory category = new MenuCategory();
        BeanUtils.copyProperties(request, category);
        // 设置默认值
        if (category.getIsVisible() == null) {
            category.setIsVisible(1);
        }
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        boolean success = menuCategoryService.save(category);
        return success ? Result.success(true) : Result.error("创建失败");
    }

    @Operation(summary = "更新分类信息")
    @PutMapping
    public Result<Boolean> updateCategory(@RequestBody @Valid MenuCategoryUpdateRequest request) {
        MenuCategory category = new MenuCategory();
        BeanUtils.copyProperties(request, category);
        boolean success = menuCategoryService.updateById(category);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCategory(
            @Parameter(description = "分类ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        boolean success = menuCategoryService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "更新分类可见性")
    @PutMapping("/{id}/visibility")
    public Result<Boolean> updateVisibility(
            @Parameter(description = "分类ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "是否可见：0-隐藏，1-显示", example = "1", required = true)
            @RequestParam("isVisible") Integer isVisible) {
        MenuCategory category = menuCategoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        category.setIsVisible(isVisible);
        boolean success = menuCategoryService.updateById(category);
        return success ? Result.success(true) : Result.error("更新失败");
    }
}
