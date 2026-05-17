package org.example.shopservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.shopservice.entity.TableInfo;

import java.util.List;

/**
 * 桌台信息服务接口
 */
public interface TableInfoService extends IService<TableInfo> {

    /**
     * 根据店铺ID查询桌台列表
     */
    List<TableInfo> listByShopId(Long shopId);

    /**
     * 查询店铺可用桌台
     */
    List<TableInfo> listAvailableTables(Long shopId);

    /**
     * 根据桌台编号查询
     */
    TableInfo getByTableNumber(Long shopId, String tableNumber);

    /**
     * 更新桌台状态
     */
    boolean updateTableStatus(Long tableId, Integer status);
}
