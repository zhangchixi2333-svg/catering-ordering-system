package org.example.menuservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.menuservice.entity.MenuCategory;
import org.example.menuservice.mapper.MenuCategoryMapper;
import org.example.menuservice.service.MenuCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜单分类服务实现类
 */
@Service
public class MenuCategoryServiceImpl extends ServiceImpl<MenuCategoryMapper, MenuCategory> implements MenuCategoryService {
    
    @Override
    public List<MenuCategory> getByShopId(Long shopId) {
        LambdaQueryWrapper<MenuCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuCategory::getShopId, shopId)
               .orderByAsc(MenuCategory::getSortOrder);
        return list(wrapper);
    }
    
    @Override
    public List<MenuCategory> getVisibleCategories(Long shopId) {
        LambdaQueryWrapper<MenuCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuCategory::getShopId, shopId)
               .eq(MenuCategory::getIsVisible, 1)
               .orderByAsc(MenuCategory::getSortOrder);
        return list(wrapper);
    }
}
