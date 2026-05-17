package org.example.shopservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.shopservice.entity.ShopConfig;

/**
 * 店铺配置服务接口
 */
public interface ShopConfigService extends IService<ShopConfig> {

    /**
     * 获取店铺配置值
     */
    String getConfigValue(Long shopId, String configKey);

    /**
     * 设置店铺配置
     */
    boolean setConfig(Long shopId, String configKey, String configValue, String configDesc);
}
