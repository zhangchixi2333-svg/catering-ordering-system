package org.example.shopservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.shopservice.entity.TableInfo;

/**
 * 桌台信息Mapper接口
 */
@Mapper
public interface TableInfoMapper extends BaseMapper<TableInfo> {
}
