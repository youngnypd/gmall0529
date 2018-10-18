package com.atguigu.gmall.manager.mapper.sku;

import com.atguigu.gmall.manager.sku.SkuAllSaleAttrAndValueTo;
import com.atguigu.gmall.manager.sku.SkuAttrValueMappingTo;
import com.atguigu.gmall.manager.sku.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SkuInfoMapper extends BaseMapper<SkuInfo> {
    List<SkuAllSaleAttrAndValueTo> getAllSaleAttrAndValueTo(@Param("id") Integer id, @Param("spuId")Integer spuId);

    List<SkuAttrValueMappingTo> getSkuAttrValueMapping(Integer spuId);
}
