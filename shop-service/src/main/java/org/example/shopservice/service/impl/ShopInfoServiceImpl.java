package org.example.shopservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.shopservice.entity.ShopInfo;
import org.example.shopservice.mapper.ShopInfoMapper;
import org.example.shopservice.service.ShopInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 店铺信息服务实现类
 */
@Service
public class ShopInfoServiceImpl extends ServiceImpl<ShopInfoMapper, ShopInfo> implements ShopInfoService {

    @Override
    public ShopInfo getByShopCode(String shopCode) {
        LambdaQueryWrapper<ShopInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopInfo::getShopCode, shopCode);
        return getOne(wrapper);
    }

    @Override
    public List<ShopInfo> listOpenShops() {
        LambdaQueryWrapper<ShopInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopInfo::getShopStatus, 1); // 营业中
        return list(wrapper);
    }

    @Override
    public boolean updateShopStatus(Long shopId, Integer status) {
        ShopInfo shopInfo = new ShopInfo();
        shopInfo.setId(shopId);
        shopInfo.setShopStatus(status);
        return updateById(shopInfo);
    }
}
