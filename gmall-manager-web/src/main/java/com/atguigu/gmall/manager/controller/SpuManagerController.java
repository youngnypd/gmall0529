package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manager.spu.SpuInfo;
import com.atguigu.gmall.manager.spu.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/spu")
public class SpuManagerController {

    @Reference
    SpuInfoService spuInfoService;

    @ResponseBody
    @RequestMapping("/info.json")
    public List<SpuInfo> getSpuInfoByC3Id(@RequestParam("catalog3Id") Integer catalog3Id){

        return spuInfoService.getSpuInfoByC3Id(catalog3Id);
    }
    @RequestMapping("/listPage.html")
    public String gotoSpuList(){
        return "spu/spuListPage";
    }
}
