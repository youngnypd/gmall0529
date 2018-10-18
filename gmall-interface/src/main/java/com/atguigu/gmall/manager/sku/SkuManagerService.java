package com.atguigu.gmall.manager.sku;

import com.atguigu.gmall.es.SkuBaseAttrEsVo;
import com.atguigu.gmall.manager.BaseAttrInfo;
import com.atguigu.gmall.manager.spu.SpuSaleAttr;

import java.util.List;

public interface SkuManagerService {
    List<BaseAttrInfo> getBaseAttrInfoByCatalog3Id(Integer catalog3Id);

    List<SpuSaleAttr> getSpuSaleAttrBySpuId(Integer spuId);

    void bigSave(SkuInfo skuInfo);

    List<SkuInfo> getSkuInfoBySpuId(Integer spuId);

    SkuInfo getSkuInfoBySkuId(Integer skuId);

    /**
     *
     * @param spuId  根据销售属性的组合查询所有sku,也就是spu旗下的所有sku
     * @return
     */
    List<SkuAttrValueMappingTo> getSkuAttrValueMapping(Integer spuId);

    /**
     * 获取所有sku所有平台属性id
     * @param skuId
     * @return
     */
    List<SkuBaseAttrEsVo> getSkuBaseAttrValueIdBySkuId(Integer skuId);
}
