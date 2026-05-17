package org.example.menuservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.menuservice.entity.ItemReview;

/**
 * 菜品评价 Mapper
 */
@Mapper
public interface ItemReviewMapper extends BaseMapper<ItemReview> {
}
