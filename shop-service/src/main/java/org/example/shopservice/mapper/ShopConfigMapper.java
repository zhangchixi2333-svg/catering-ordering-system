package org.example.shopservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.shopservice.entity.ShopConfig;

/**
 * 店铺配置Mapper接口
 */
@Mapper
public interface ShopConfigMapper extends BaseMapper<ShopConfig> {
}
