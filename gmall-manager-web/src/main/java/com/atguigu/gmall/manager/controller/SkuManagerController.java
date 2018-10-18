package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manager.BaseAttrInfo;
import com.atguigu.gmall.manager.sku.SkuEsService;
import com.atguigu.gmall.manager.sku.SkuInfo;
import com.atguigu.gmall.manager.sku.SkuManagerService;
import com.atguigu.gmall.manager.spu.SpuImage;
import com.atguigu.gmall.manager.spu.SpuInfoService;
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

    @Reference
    SpuInfoService spuInfoService;

    @Reference
    SkuEsService skuEsService;

    /**
     * 根据商品的skuid进行上架功能
     * @param skuId
     * @return
     */
    @RequestMapping("/onSale")
    public String onSale(@RequestParam("skuId") Integer skuId){
        skuEsService.onSale(skuId);
        return "ok";
    }
    /**
     *返回sku页面
     * @param spuId
     * @return
     */
    @RequestMapping("/spu_info")
    public List<SkuInfo> getSkuInfoBySpuId(@RequestParam Integer spuId){
        return skuManagerService.getSkuInfoBySpuId(spuId);
    }

    @RequestMapping("/big_save")
    public String saveBigSkuInfo(@RequestBody SkuInfo skuInfo){
        skuManagerService.bigSave(skuInfo);
        return "ok";
    }

    /**
     * 根据商品id查询出所有图片信息
     * @param spuId
     * @return
     */
    @RequestMapping("/spuImages")
    public List<SpuImage> getSpuImageBySpuId(@RequestParam("id") Integer spuId){
        return spuInfoService.getSpuImageBySpuId(spuId);
    }

    /**
     * 根据三级分类id查询列出平台属性信息
     * @param catalog3Id
     * @return
     */
    @RequestMapping("/base_attr_info.json")
    public List<BaseAttrInfo> getBaseAttrInfoByCatalog3Id(@RequestParam("id") Integer catalog3Id){
        return skuManagerService.getBaseAttrInfoByCatalog3Id(catalog3Id);
    }
    /**puId对应的所有可供选择的sku
     * 查询s
     * @param spuId
     * @return
     */
    @RequestMapping("/spu_sale_attr.json")
    public List<SpuSaleAttr> getSpuSaleAttrBySpuId(@RequestParam("id") Integer spuId){
        return skuManagerService.getSpuSaleAttrBySpuId(spuId);
    }
}
