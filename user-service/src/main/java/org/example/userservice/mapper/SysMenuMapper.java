package org.example.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.userservice.entity.SysMenu;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    
    /**
     * 根据用户ID查询菜单列表
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.visible = 1 " +
            "ORDER BY m.sort_order")
    List<SysMenu> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色编码查询菜单列表
     * @param roleCode 角色编码
     * @return 菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "JOIN sys_role r ON rm.role_id = r.id " +
            "WHERE r.role_code = #{roleCode} AND m.status = 1 " +
            "ORDER BY m.sort_order")
    List<SysMenu> findByRoleCode(@Param("roleCode") String roleCode);
}
