package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manager.sku.SkuAttrValueMappingTo;
import com.atguigu.gmall.manager.sku.SkuInfo;
import com.atguigu.gmall.manager.sku.SkuManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ItemController {
    @Reference
    SkuManagerService skuManagerService;

    /**
     * 页面显示sku
     * @param skuId
     * @param model
     * @return
     */
    @RequestMapping("/{skuId}.html")
    public String getSkuInfoBySkuId(@PathVariable("skuId") Integer skuId, Model model){
        //只是为了显示sku商品信息
        SkuInfo skuInfo = skuManagerService.getSkuInfoBySkuId(skuId);

        if (skuInfo == null) {
            return "error";
        }
        model.addAttribute("skuInfo",skuInfo);
        Integer spuId = skuInfo.getSpuId();
        List<SkuAttrValueMappingTo> valueMappingTos = skuManagerService.getSkuAttrValueMapping(spuId);
        if (valueMappingTos == null) {
            return "error";
        }

        model.addAttribute("skuValueMapping",valueMappingTos);

        return "item";
        //根据不同的销售属性值的组合跳转到不同的页面


    }
}
