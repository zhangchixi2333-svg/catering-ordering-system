package org.example.shopservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.shopservice.entity.ShopConfig;
import org.example.shopservice.mapper.ShopConfigMapper;
import org.example.shopservice.service.ShopConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 店铺配置服务实现类
 */
@Service
public class ShopConfigServiceImpl extends ServiceImpl<ShopConfigMapper, ShopConfig> implements ShopConfigService {

    @Override
    public String getConfigValue(Long shopId, String configKey) {
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopConfig::getShopId, shopId);
        wrapper.eq(ShopConfig::getConfigKey, configKey);
        ShopConfig config = getOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public boolean setConfig(Long shopId, String configKey, String configValue, String configDesc) {
        LambdaQueryWrapper<ShopConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopConfig::getShopId, shopId);
        wrapper.eq(ShopConfig::getConfigKey, configKey);
        ShopConfig existingConfig = getOne(wrapper);

        if (existingConfig != null) {
            // 更新现有配置
            existingConfig.setConfigValue(configValue);
            if (configDesc != null) {
                existingConfig.setConfigDesc(configDesc);
            }
            // 手动设置更新时间，确保一定生效
            existingConfig.setUpdatedAt(LocalDateTime.now());
            return updateById(existingConfig);
        } else {
            // 创建新配置
            ShopConfig newConfig = new ShopConfig();
            newConfig.setShopId(shopId);
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(configValue);
            newConfig.setConfigDesc(configDesc);
            // 手动设置创建和更新时间
            LocalDateTime now = LocalDateTime.now();
            newConfig.setCreatedAt(now);
            newConfig.setUpdatedAt(now);
            return save(newConfig);
        }
    }
}
