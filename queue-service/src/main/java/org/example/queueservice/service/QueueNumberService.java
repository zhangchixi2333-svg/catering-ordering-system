package org.example.queueservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.queueservice.entity.QueueNumber;
import java.util.List;

public interface QueueNumberService extends IService<QueueNumber> {
    QueueNumber getByQueueNo(String queueNo);
    List<QueueNumber> getByShopId(Long shopId);
    List<QueueNumber> getByStatus(Integer queueStatus);
    List<QueueNumber> getWaitingQueue(Long shopId);
}
