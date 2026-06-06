package org.example.menuservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.menuservice.entity.ItemTopping;

/**
 * 菜品配料 Mapper
 */
@Mapper
public interface ItemToppingMapper extends BaseMapper<ItemTopping> {
}
