package org.example.shopservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.shopservice.entity.TableInfo;
import org.example.shopservice.mapper.TableInfoMapper;
import org.example.shopservice.service.TableInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 桌台信息服务实现类
 */
@Service
public class TableInfoServiceImpl extends ServiceImpl<TableInfoMapper, TableInfo> implements TableInfoService {

    @Override
    public List<TableInfo> listByShopId(Long shopId) {
        LambdaQueryWrapper<TableInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TableInfo::getShopId, shopId);
        wrapper.orderByAsc(TableInfo::getTableNumber);
        return list(wrapper);
    }

    @Override
    public List<TableInfo> listAvailableTables(Long shopId) {
        LambdaQueryWrapper<TableInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TableInfo::getShopId, shopId);
        wrapper.eq(TableInfo::getIsAvailable, 1);
        wrapper.eq(TableInfo::getTableStatus, 0); // 空闲状态
        wrapper.orderByAsc(TableInfo::getTableNumber);
        return list(wrapper);
    }

    @Override
    public TableInfo getByTableNumber(Long shopId, String tableNumber) {
        LambdaQueryWrapper<TableInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TableInfo::getShopId, shopId);
        wrapper.eq(TableInfo::getTableNumber, tableNumber);
        return getOne(wrapper);
    }

    @Override
    public boolean updateTableStatus(Long tableId, Integer status) {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setId(tableId);
        tableInfo.setTableStatus(status);
        return updateById(tableInfo);
    }
}
