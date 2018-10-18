package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.es.SkuBaseAttrEsVo;
import com.atguigu.gmall.es.SkuInfoEsVo;
import com.atguigu.gmall.list.constant.EsConstant;
import com.atguigu.gmall.manager.sku.SkuEsService;
import com.atguigu.gmall.manager.sku.SkuInfo;
import com.atguigu.gmall.manager.sku.SkuManagerService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class SkuEsServiceImpl implements SkuEsService {
    @Reference
    SkuManagerService skuManagerService;
    @Autowired
    JestClient jestClient;
    @Async//异步方法
    @Override
    public void onSale(Integer skuId) {
        SkuInfo info = skuManagerService.getSkuInfoBySkuId(skuId);
        log.info("获取到的商品详细信息是: {}", info);
        //将查询到的skuinfo对象拷贝到SkuInfoEsVo中,再存到es中
        SkuInfoEsVo skuInfoEsVo = new SkuInfoEsVo();
        BeanUtils.copyProperties(info, skuInfoEsVo);
        //还需要将平台属性的id查询出来再设置进skuInfoEsVo中
         List<SkuBaseAttrEsVo> vo = skuManagerService.getSkuBaseAttrValueIdBySkuId(skuId);
         skuInfoEsVo.setBaseAttrEsVos(vo);

         //保存到es中
        Index index = new Index.Builder(skuInfoEsVo).index(EsConstant.GMALL_INDEX)
                .type(EsConstant.GMALL_SKU_TYPE).id(skuInfoEsVo.getId().toString()).build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {

            log.error("保存es中失败了,{}",e);
        }

    }
}
