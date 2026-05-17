package org.example.menuservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.menuservice.entity.MenuItem;
import org.example.menuservice.mapper.MenuItemMapper;
import org.example.menuservice.service.MenuItemService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜品信息服务实现类
 */
@Service
public class MenuItemServiceImpl extends ServiceImpl<MenuItemMapper, MenuItem> implements MenuItemService {
    
    @Override
    public List<MenuItem> getByCategoryId(Long categoryId) {
        LambdaQueryWrapper<MenuItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuItem::getCategoryId, categoryId)
               .orderByAsc(MenuItem::getSortOrder);
        return list(wrapper);
    }
    
    @Override
    public List<MenuItem> getAvailableItems(Long shopId) {
        LambdaQueryWrapper<MenuItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuItem::getShopId, shopId)
               .eq(MenuItem::getIsAvailable, 1)
               .orderByDesc(MenuItem::getSalesCount);
        return list(wrapper);
    }
    
    @Override
    public List<MenuItem> getRecommendedItems(Long shopId) {
        LambdaQueryWrapper<MenuItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuItem::getShopId, shopId)
               .eq(MenuItem::getIsRecommended, 1)
               .eq(MenuItem::getIsAvailable, 1)
               .orderByDesc(MenuItem::getRating);
        return list(wrapper);
    }
    
    @Override
    public MenuItem getByItemCode(String itemCode) {
        LambdaQueryWrapper<MenuItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuItem::getItemCode, itemCode);
        return getOne(wrapper);
    }
}
