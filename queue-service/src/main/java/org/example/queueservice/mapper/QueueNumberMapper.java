package org.example.queueservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.queueservice.entity.QueueNumber;

@Mapper
public interface QueueNumberMapper extends BaseMapper<QueueNumber> {
}
