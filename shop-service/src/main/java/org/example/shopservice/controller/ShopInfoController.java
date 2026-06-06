package org.example.shopservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shopservice.common.Result;
import org.example.shopservice.dto.ShopCreateRequest;
import org.example.shopservice.dto.ShopStatusUpdateRequest;
import org.example.shopservice.dto.ShopUpdateRequest;
import org.example.shopservice.entity.ShopInfo;
import org.example.shopservice.service.ShopInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 店铺信息控制器
 */
@Tag(name = "店铺管理", description = "店铺信息的增删改查")
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopInfoController {

    private final ShopInfoService shopInfoService;

    @Operation(summary = "获取所有店铺列表")
    @GetMapping("/list")
    public Result<List<ShopInfo>> listShops() {
        List<ShopInfo> shops = shopInfoService.list();
        return Result.success(shops);
    }

    @Operation(summary = "获取营业中的店铺")
    @GetMapping("/open")
    public Result<List<ShopInfo>> listOpenShops() {
        List<ShopInfo> shops = shopInfoService.listOpenShops();
        return Result.success(shops);
    }

    @Operation(summary = "根据ID获取店铺详情")
    @GetMapping("/{id}")
    public Result<ShopInfo> getShopById(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        ShopInfo shop = shopInfoService.getById(id);
        if (shop == null) {
            return Result.error("店铺不存在");
        }
        return Result.success(shop);
    }

    @Operation(summary = "根据编码获取店铺")
    @GetMapping("/code/{shopCode}")
    public Result<ShopInfo> getShopByCode(
            @Parameter(description = "店铺编码", example = "SHOP001", required = true)
            @PathVariable("shopCode") String shopCode) {
        ShopInfo shop = shopInfoService.getByShopCode(shopCode);
        if (shop == null) {
            return Result.error("店铺不存在");
        }
        return Result.success(shop);
    }

    @Operation(summary = "创建店铺")
    @PostMapping
    public Result<Boolean> createShop(@RequestBody @Valid ShopCreateRequest request) {
        ShopInfo shopInfo = new ShopInfo();
        BeanUtils.copyProperties(request, shopInfo);
        // 设置默认值
        if (shopInfo.getShopStatus() == null) {
            shopInfo.setShopStatus(1); // 默认营业中
        }
        boolean success = shopInfoService.save(shopInfo);
        return success ? Result.success(true) : Result.error("创建失败");
    }

    @Operation(summary = "更新店铺信息")
    @PutMapping
    public Result<Boolean> updateShop(@RequestBody @Valid ShopUpdateRequest request) {
        ShopInfo shopInfo = new ShopInfo();
        BeanUtils.copyProperties(request, shopInfo);
        boolean success = shopInfoService.updateById(shopInfo);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @Operation(summary = "删除店铺")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteShop(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        boolean success = shopInfoService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "更新店铺状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @RequestBody @Valid ShopStatusUpdateRequest request) {
        boolean success = shopInfoService.updateShopStatus(id, request.getShopStatus());
        return success ? Result.success(true) : Result.error("更新状态失败");
    }
}
