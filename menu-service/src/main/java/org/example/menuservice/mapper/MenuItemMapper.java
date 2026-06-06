package org.example.menuservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.menuservice.entity.MenuItem;

/**
 * 菜品信息 Mapper
 */
@Mapper
public interface MenuItemMapper extends BaseMapper<MenuItem> {
}
