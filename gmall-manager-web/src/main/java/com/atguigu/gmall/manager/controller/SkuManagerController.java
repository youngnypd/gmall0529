package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manager.BaseAttrInfo;
import com.atguigu.gmall.manager.sku.SkuManagerService;
import com.atguigu.gmall.manager.spu.SpuSaleAttr;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/sku")
@RestController
public class SkuManagerController {

    @Reference
    SkuManagerService skuManagerService;
    @RequestMapping("/base_attr_info.json")
    public List<BaseAttrInfo> getBaseAttrInfoByCatalog3Id(@RequestParam("id") Integer catalog3Id){
        return skuManagerService.getBaseAttrInfoByCatalog3Id(catalog3Id);
    }
    /**
     * 查询spuId对应的所有可供选择的sku
     * @param spuId
     * @return
     */
    @RequestMapping("/spu_sale_attr.json")
    public List<SpuSaleAttr> getSpuSaleAttrBySpuId(@RequestParam("id") Integer spuId){
        return skuManagerService.getSpuSaleAttrBySpuId(spuId);
    }
}
