package com.atguigu.gmall.listweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.es.SkuSearchParamESVo;
import com.atguigu.gmall.es.SkuSearchResultEsVo;
import com.atguigu.gmall.manager.sku.SkuEsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 将页面可能提交的属性封装到SkuSearchParamESVo对象中
 */
@Controller
public class ListController {

    @Reference
    SkuEsService skuEsService;
    @RequestMapping("/list.html")
    public String  listPage(SkuSearchParamESVo paramEsVo, Model model){
        //按照keyword -xxxxx

        //搜索完成以后返回这个对象，这个对象里面有所有的数据；
        SkuSearchResultEsVo searchResult = skuEsService.searchSkuFromES(paramEsVo);
        model.addAttribute("searchResult",searchResult);
        return "list";
    }

}
