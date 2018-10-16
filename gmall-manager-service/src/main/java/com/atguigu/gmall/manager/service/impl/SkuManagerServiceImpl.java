package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manager.BaseAttrInfo;
import com.atguigu.gmall.manager.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manager.mapper.sku.SkuAttrValueMapper;
import com.atguigu.gmall.manager.mapper.sku.SkuImageMapper;
import com.atguigu.gmall.manager.mapper.sku.SkuInfoMapper;
import com.atguigu.gmall.manager.mapper.sku.SkuSaleAttrValueMapper;
import com.atguigu.gmall.manager.mapper.spu.SpuSaleAttrMapper;
import com.atguigu.gmall.manager.sku.*;
import com.atguigu.gmall.manager.spu.SpuSaleAttr;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Slf4j
@Service
public class SkuManagerServiceImpl implements SkuManagerService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Override
    public List<BaseAttrInfo> getBaseAttrInfoByCatalog3Id(Integer catalog3Id) {
        return baseAttrInfoMapper.getBaseAttrInfoByCatalog3Id(catalog3Id);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrBySpuId(Integer spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrBySpuId(spuId);
    }

    /**
     * 页面传过来的sku数据进行大保存
     * @param skuInfo
     */
    @Transactional
    @Override
    public void bigSave(SkuInfo skuInfo) {
        log.info("页面传过来的sku数据是:{}",skuInfo);//
        //1.保存skuinfo的基本信息
        skuInfoMapper.insert(skuInfo);
        Integer skuId = skuInfo.getId();
        //2.保存skuinfo的图片信息
        List<SkuImage> skuImages = skuInfo.getSkuImages();
        for (SkuImage skuImage : skuImages) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insert(skuImage);
        }
        //3.保存skuinfo的平台属性
        List<SkuAttrValue> skuAttrValues = skuInfo.getSkuAttrValues();
        for (SkuAttrValue skuAttrValue : skuAttrValues) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insert(skuAttrValue);
        }
        //4保存skuinfo的销售属性
        List<SkuSaleAttrValue> skuSaleAttrValues = skuInfo.getSkuSaleAttrValues();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValues) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }
    }

    /**
     * 通过spuid获取sku信息
     * @param spuId
     * @return
     */
    @Override
    public List<SkuInfo> getSkuInfoBySpuId(Integer spuId) {
        return skuInfoMapper.selectList(new QueryWrapper<SkuInfo>().eq("spu_id", spuId));
    }

    /**
     * 通过商品id获取商品所有信息
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfoBySkuId(Integer skuId) {
        //1.查询skuinfo基本信息
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if(skuInfo == null){
            return null;
        }
        //2.查询skuinfo的图片信息
        List<SkuImage> skuImages = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
        skuInfo.setSkuImages(skuImages);
        //3.查询skuinfo销售属性
        List<SkuAllSaleAttrAndValueTo> skuAllSaleAttrAndValueTos= skuInfoMapper.getAllSaleAttrAndValueTo(skuInfo.getId(),skuInfo.getSpuId());
        skuInfo.setSkuAllSaleAttrAndValueTos(skuAllSaleAttrAndValueTos);
        return skuInfo;
    }
}
