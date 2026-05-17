package org.example.shopservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shopservice.common.Result;
import org.example.shopservice.dto.TableCreateRequest;
import org.example.shopservice.dto.TableStatusUpdateRequest;
import org.example.shopservice.dto.TableUpdateRequest;
import org.example.shopservice.entity.TableInfo;
import org.example.shopservice.service.TableInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 桌台信息控制器
 */
@Tag(name = "桌台管理", description = "桌台信息的增删改查")
@RestController
@RequestMapping("/api/table")
@RequiredArgsConstructor
public class TableInfoController {

    private final TableInfoService tableInfoService;

    @Operation(summary = "获取店铺的所有桌台")
    @GetMapping("/shop/{shopId}")
    public Result<List<TableInfo>> listTables(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        List<TableInfo> tables = tableInfoService.listByShopId(shopId);
        return Result.success(tables);
    }

    @Operation(summary = "获取店铺可用桌台")
    @GetMapping("/shop/{shopId}/available")
    public Result<List<TableInfo>> listAvailableTables(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId) {
        List<TableInfo> tables = tableInfoService.listAvailableTables(shopId);
        return Result.success(tables);
    }

    @Operation(summary = "根据ID获取桌台详情")
    @GetMapping("/{id}")
    public Result<TableInfo> getTableById(
            @Parameter(description = "桌台ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        TableInfo table = tableInfoService.getById(id);
        if (table == null) {
            return Result.error("桌台不存在");
        }
        return Result.success(table);
    }

    @Operation(summary = "根据桌台编号查询")
    @GetMapping("/shop/{shopId}/number/{tableNumber}")
    public Result<TableInfo> getByTableNumber(
            @Parameter(description = "店铺ID", example = "1", required = true)
            @PathVariable("shopId") Long shopId,
            @Parameter(description = "桌台编号", example = "A01", required = true)
            @PathVariable("tableNumber") String tableNumber) {
        TableInfo table = tableInfoService.getByTableNumber(shopId, tableNumber);
        if (table == null) {
            return Result.error("桌台不存在");
        }
        return Result.success(table);
    }

    @Operation(summary = "创建桌台")
    @PostMapping
    public Result<Boolean> createTable(@RequestBody @Valid TableCreateRequest request) {
        TableInfo tableInfo = new TableInfo();
        BeanUtils.copyProperties(request, tableInfo);
        // 设置默认值
        if (tableInfo.getTableStatus() == null) {
            tableInfo.setTableStatus(0); // 默认空闲
        }
        if (tableInfo.getIsAvailable() == null) {
            tableInfo.setIsAvailable(1); // 默认可用
        }
        boolean success = tableInfoService.save(tableInfo);
        return success ? Result.success(true) : Result.error("创建失败");
    }

    @Operation(summary = "更新桌台信息")
    @PutMapping
    public Result<Boolean> updateTable(@RequestBody @Valid TableUpdateRequest request) {
        TableInfo tableInfo = new TableInfo();
        BeanUtils.copyProperties(request, tableInfo);
        boolean success = tableInfoService.updateById(tableInfo);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @Operation(summary = "删除桌台")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteTable(
            @Parameter(description = "桌台ID", example = "1", required = true)
            @PathVariable("id") Long id) {
        boolean success = tableInfoService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @Operation(summary = "更新桌台状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @Parameter(description = "桌台ID", example = "1", required = true)
            @PathVariable("id") Long id,
            @RequestBody @Valid TableStatusUpdateRequest request) {
        boolean success = tableInfoService.updateTableStatus(id, request.getTableStatus());
        return success ? Result.success(true) : Result.error("更新状态失败");
    }
}
