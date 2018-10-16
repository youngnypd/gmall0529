package com.atguigu.gmall.manager.sku;

import com.atguigu.gmall.manager.BaseAttrInfo;
import com.atguigu.gmall.manager.spu.SpuSaleAttr;

import java.util.List;

public interface SkuManagerService {
    List<BaseAttrInfo> getBaseAttrInfoByCatalog3Id(Integer catalog3Id);

    List<SpuSaleAttr> getSpuSaleAttrBySpuId(Integer spuId);

    void bigSave(SkuInfo skuInfo);

    List<SkuInfo> getSkuInfoBySpuId(Integer spuId);

    SkuInfo getSkuInfoBySkuId(Integer skuId);
}
