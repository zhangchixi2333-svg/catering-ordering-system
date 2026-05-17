package org.example.menuservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.menuservice.entity.MenuCategory;

/**
 * 菜单分类 Mapper
 */
@Mapper
public interface MenuCategoryMapper extends BaseMapper<MenuCategory> {
}
