package org.example.menuservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.menuservice.entity.MenuCategory;

import java.util.List;

/**
 * 菜单分类服务接口
 */
public interface MenuCategoryService extends IService<MenuCategory> {
    
    /**
     * 根据店铺ID获取分类列表
     */
    List<MenuCategory> getByShopId(Long shopId);
    
    /**
     * 获取可见的分类列表
     */
    List<MenuCategory> getVisibleCategories(Long shopId);
}
