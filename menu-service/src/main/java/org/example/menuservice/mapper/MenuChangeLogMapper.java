package org.example.menuservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.menuservice.entity.MenuChangeLog;

/**
 * 菜单变更日志 Mapper
 */
@Mapper
public interface MenuChangeLogMapper extends BaseMapper<MenuChangeLog> {
}
