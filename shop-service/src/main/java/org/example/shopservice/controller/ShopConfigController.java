package org.example.shopservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.shopservice.common.Result;
import org.example.shopservice.service.ShopConfigService;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺配置控制器
 */
@Tag(name = "店铺配置", description = "店铺配置的读写")
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ShopConfigController {

    private final ShopConfigService shopConfigService;

    @Operation(summary = "获取配置值")
    @GetMapping("/{shopId}/{configKey}")
    public Result<String> getConfig(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId,
            @Parameter(description = "配置键名", example = "queue_enabled", required = true)
            @PathVariable("configKey") String configKey) {
        String value = shopConfigService.getConfigValue(shopId, configKey);
        if (value == null) {
            return Result.error("配置不存在");
        }
        return Result.success(value);
    }

    @Operation(summary = "设置配置")
    @PostMapping
    public Result<Boolean> setConfig(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @RequestParam("shopId") Long shopId,
            @Parameter(description = "配置键名", example = "queue_enabled", required = true)
            @RequestParam("configKey") String configKey,
            @Parameter(description = "配置值", example = "true", required = true)
            @RequestParam("configValue") String configValue,
            @Parameter(description = "配置说明", example = "是否启用排队功能")
            @RequestParam(value = "configDesc", required = false) String configDesc) {
        boolean success = shopConfigService.setConfig(shopId, configKey, configValue, configDesc);
        return success ? Result.success(true) : Result.error("设置失败");
    }
}
