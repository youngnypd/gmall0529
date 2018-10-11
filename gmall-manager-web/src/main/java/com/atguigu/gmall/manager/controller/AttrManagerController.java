package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manager.BaseAttrInfoService;
import com.atguigu.gmall.manager.BaseAttrValue;
import com.atguigu.gmall.manager.vo.BaseAttrInfoAndValueVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@RequestMapping("/attr")
@Controller
public class AttrManagerController {

    @Reference
    BaseAttrInfoService baseAttrInfoService;


    /**
     *
     * @param baseAttrInfoAndValueVO
     * @return
     * @RequestBody,将请求体中的数据按照要求封装成指定的对象
     */
    @ResponseBody
    @RequestMapping("/updates")
    public String saveOrUpdateOrDeleteAttrInfoAndValue(@RequestBody BaseAttrInfoAndValueVO baseAttrInfoAndValueVO){
        log.info("页面提交的数据{}",baseAttrInfoAndValueVO);
        return "Hala";
    }

    /**
     * 去平台属性列表页面
     * 所有的去页面的请求，都加上html后缀
     * @return
     */
    @RequestMapping("/listPage.html")
    public String toAttrListPage(){

        return "attr/attrListPage";
    }

    /**
     * 根据平台属性名id获取下面的所有属性值属性
     * @param id
     * @return
     */
    @RequestMapping("/value/{id}")
    @ResponseBody
    public List<BaseAttrValue> getBaseAttrValueByInfoId(@PathVariable("id") Integer id){
        return baseAttrInfoService.getBaseAttrValueByAttrId(id);
    }

}
