package org.example.queueservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.queueservice.entity.QueueNumber;
import org.example.queueservice.mapper.QueueNumberMapper;
import org.example.queueservice.service.QueueNumberService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class QueueNumberServiceImpl extends ServiceImpl<QueueNumberMapper, QueueNumber> implements QueueNumberService {
    
    @Override
    public QueueNumber getByQueueNo(String queueNo) {
        LambdaQueryWrapper<QueueNumber> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QueueNumber::getQueueNo, queueNo);
        return getOne(wrapper);
    }
    
    @Override
    public List<QueueNumber> getByShopId(Long shopId) {
        LambdaQueryWrapper<QueueNumber> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QueueNumber::getShopId, shopId).orderByAsc(QueueNumber::getCreatedAt);
        return list(wrapper);
    }
    
    @Override
    public List<QueueNumber> getByStatus(Integer queueStatus) {
        LambdaQueryWrapper<QueueNumber> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QueueNumber::getQueueStatus, queueStatus).orderByAsc(QueueNumber::getCreatedAt);
        return list(wrapper);
    }
    
    @Override
    public List<QueueNumber> getWaitingQueue(Long shopId) {
        LambdaQueryWrapper<QueueNumber> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QueueNumber::getShopId, shopId)
               .eq(QueueNumber::getQueueStatus, 0)
               .orderByAsc(QueueNumber::getCreatedAt);
        return list(wrapper);
    }
}
