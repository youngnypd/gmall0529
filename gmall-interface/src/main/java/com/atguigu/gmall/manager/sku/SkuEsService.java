package com.atguigu.gmall.manager.sku;

import com.atguigu.gmall.es.SkuSearchParamESVo;
import com.atguigu.gmall.es.SkuSearchResultEsVo;

public interface SkuEsService {
    void onSale(Integer skuId);
    SkuSearchResultEsVo searchSkuFromES(SkuSearchParamESVo paramEsVo);
    /**
     * 更新Es中商品的热度值
     * @param skuId
     * @param hincrBy
     */
    void updateHotScore(Integer skuId, Long hincrBy);
}
