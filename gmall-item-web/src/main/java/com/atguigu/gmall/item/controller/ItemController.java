package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manager.sku.SkuInfo;
import com.atguigu.gmall.manager.sku.SkuManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ItemController {
    @Reference
    SkuManagerService skuManagerService;
    @RequestMapping("/{skuId}.html")
    public String getSkuInfoBySkuId(@PathVariable("skuId") Integer skuId, Model model){
        SkuInfo skuInfo = skuManagerService.getSkuInfoBySkuId(skuId);

        if (skuInfo == null) {
            return "error";
        }
        model.addAttribute("skuInfo", skuInfo);
        return "item";
    }
}
