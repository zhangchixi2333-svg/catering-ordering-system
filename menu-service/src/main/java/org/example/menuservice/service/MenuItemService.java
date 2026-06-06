package org.example.menuservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.menuservice.entity.MenuItem;

import java.util.List;

/**
 * 菜品信息服务接口
 */
public interface MenuItemService extends IService<MenuItem> {
    
    /**
     * 根据分类ID获取菜品列表
     */
    List<MenuItem> getByCategoryId(Long categoryId);
    
    /**
     * 获取可售的菜品列表
     */
    List<MenuItem> getAvailableItems(Long shopId);
    
    /**
     * 获取推荐的菜品列表
     */
    List<MenuItem> getRecommendedItems(Long shopId);
    
    /**
     * 根据菜品编码获取菜品
     */
    MenuItem getByItemCode(String itemCode);
}
