package org.example.paymentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.paymentservice.entity.PaymentOrder;

@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {
}
