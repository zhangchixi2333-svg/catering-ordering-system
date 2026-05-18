package org.example.menuservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.menuservice.common.Result;
import org.example.menuservice.dto.MenuItemCreateRequest;
import org.example.menuservice.dto.MenuItemUpdateRequest;
import org.example.menuservice.entity.MenuItem;
import org.example.menuservice.feign.ShopFeignClient;
import org.example.menuservice.service.MenuItemService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品信息控制器
 */
@Tag(name = "菜品管理", description = "菜品的增删改查")
@RestController
@RequestMapping("/api/menu/item")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;
    private final ShopFeignClient shopFeignClient;

    @Operation(summary = "获取所有菜品列表")
    @GetMapping("/list")
    public Result<List<MenuItem>> listItems() {
        List<MenuItem> items = menuItemService.list();
        return Result.success(items);
    }

    @Operation(summary = "根据分类ID获取菜品列表")
    @GetMapping("/category/{categoryId}")
    public Result<List<MenuItem>> getItemsByCategory(
            @Parameter(description = "分类ID", example = "1", required = true)
            @PathVariable("categoryId") Long categoryId) {
        List<MenuItem> items = menuItemService.getByCategoryId(categoryId);
        return Result.success(items);
    }

    @Operation(summary = "获取可售的菜品列表")
    @GetMapping("/available/{shopId}")
    public Result<List<MenuItem>> getAvailableItems(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        List<MenuItem> items = menuItemService.getAvailableItems(shopId);
        return Result.success(items);
    }

    @Operation(summary = "获取推荐的菜品列表")
    @GetMapping("/recommended/{shopId}")
    public Result<List<MenuItem>> getRecommendedItems(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        List<MenuItem> items = menuItemService.getRecommendedItems(shopId);
        return Result.success(items);
    }

    @Operation(summary = "根据ID获取菜品详情")
    @GetMapping("/{id}")
    public Result<MenuItem> getItemById(
            @Parameter(description = "菜品ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        MenuItem item = menuItemService.getById(id);
        if (item == null) {
            return Result.error("菜品不存在");
        }
        return Result.success(item);
    }

    @Operation(summary = "根据编码获取菜品")
    @GetMapping("/code/{itemCode}")
    public Result<MenuItem> getItemByCode(
            @Parameter(description = "菜品编码", example = "ITEM001", required = true)
            @PathVariable("itemCode") String itemCode) {
        MenuItem item = menuItemService.getByItemCode(itemCode);
        if (item == null) {
            return Result.error("菜品不存在");
        }
        return Result.success(item);
    }

    @Operation(
        summary = "创建菜品（验证店铺）",
        description = "<font color='red'>【优化】</font><br/>" +
                "创建新的菜品，系统会自动验证店铺信息<br/><br/>" +
                "<font color='green'>业务规则：</font><br/>" +
                "1. 调用 shop-service 验证店铺是否存在 - 不存在则返回错误<br/>" +
                "2. 设置默认值：isAvailable=1, stock=-1(无限), salesCount=0<br/>" +
                "3. 菜品必须关联到有效的店铺和分类"
    )
    @PostMapping
    public Result<Boolean> createItem(@RequestBody @Valid MenuItemCreateRequest request) {
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
        
        // 2. 创建菜品
        MenuItem item = new MenuItem();
        BeanUtils.copyProperties(request, item);
        // 设置默认值
        if (item.getIsAvailable() == null) {
            item.setIsAvailable(1);
        }
        if (item.getStock() == null) {
            item.setStock(-1);
        }
        if (item.getSalesCount() == null) {
            item.setSalesCount(0);
        }
        boolean success = menuItemService.save(item);
        return success ? Result.success(true) : Result.error("创建失败");
    }

    @Operation(summary = "更新菜品信息")
    @PutMapping
    public Result<Boolean> updateItem(@RequestBody @Valid MenuItemUpdateRequest request) {
        MenuItem item = new MenuItem();
        BeanUtils.copyProperties(request, item);
        boolean success = menuItemService.updateById(item);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @Operation(summary = "删除菜品")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteItem(
            @Parameter(description = "菜品ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        boolean success = menuItemService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "更新菜品上下架状态")
    @PutMapping("/{id}/availability")
    public Result<Boolean> updateAvailability(
            @Parameter(description = "菜品ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "是否可售：0-下架，1-上架", example = "1", required = true)
            @RequestParam("isAvailable") Integer isAvailable) {
        MenuItem item = menuItemService.getById(id);
        if (item == null) {
            return Result.error("菜品不存在");
        }
        item.setIsAvailable(isAvailable);
        boolean success = menuItemService.updateById(item);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @Operation(summary = "更新菜品库存")
    @PutMapping("/{id}/stock")
    public Result<Boolean> updateStock(
            @Parameter(description = "菜品ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "库存数量", example = "100", required = true)
            @RequestParam("stock") Integer stock) {
        MenuItem item = menuItemService.getById(id);
        if (item == null) {
            return Result.error("菜品不存在");
        }
        item.setStock(stock);
        boolean success = menuItemService.updateById(item);
        return success ? Result.success(true) : Result.error("更新失败");
    }
}
