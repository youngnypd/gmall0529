package com.atguigu.gmall.manager.spu;

import java.util.List;

public interface SpuInfoService {
    List<SpuInfo> getSpuInfoByC3Id(Integer catalog3Id);

    List<BaseSaleAttr> getBaseSaleAttr();

    //spuInfo的大保存
    void saveBigSpuInfo(SpuInfo spuInfo);

    List<SpuImage> getSpuImageBySpuId(Integer spuId);
}
