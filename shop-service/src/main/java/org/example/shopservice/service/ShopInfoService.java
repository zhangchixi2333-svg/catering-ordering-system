package org.example.shopservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.shopservice.entity.ShopInfo;

import java.util.List;

/**
 * 店铺信息服务接口
 */
public interface ShopInfoService extends IService<ShopInfo> {

    /**
     * 根据店铺编码查询店铺
     */
    ShopInfo getByShopCode(String shopCode);

    /**
     * 查询所有营业中的店铺
     */
    List<ShopInfo> listOpenShops();

    /**
     * 更新店铺状态
     */
    boolean updateShopStatus(Long shopId, Integer status);
}
