package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotation.LoginRequired;
import com.atguigu.gmall.manager.sku.SkuAttrValueMappingTo;
import com.atguigu.gmall.manager.sku.SkuInfo;
import com.atguigu.gmall.manager.sku.SkuManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
@Slf4j
@Controller
public class ItemController {
    @Reference
    SkuManagerService skuManagerService;


    @LoginRequired
    @RequestMapping("/haha")
    public String hahaha(HttpServletRequest request){
        //常用的key都要抽取为常量
        Map<String,Object> userInfo = (Map<String, Object>) request.getAttribute("userInfo");

        //要用户名
        //1、如果这个token不是没意义的随机数（只用来做redis中key标识的）
        //2、假设这个token是 一串有意义的  dsjakljdsalkjdals.djsaljdaskljdlasjdkslajdsadlkas
        //   这串数据以及包含了你最常用的信息，你要用这些信息不用查了，你的领牌里面就有
        //不可伪造;还携带了常用信息
        //JWT(JSON Web Token)（规范）稍微加密。能加也要能解
        //UserInfo = redis.get(token)
        log.info("我们可以解码到用户的信息是：{}",userInfo);
        return "haha";
    }
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
