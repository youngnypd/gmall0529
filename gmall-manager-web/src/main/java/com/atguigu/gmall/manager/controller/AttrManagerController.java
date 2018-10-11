package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manager.BaseAttrInfo;
import com.atguigu.gmall.manager.BaseAttrInfoService;
import com.atguigu.gmall.manager.BaseAttrValue;
import com.atguigu.gmall.manager.vo.BaseAttrInfoAndValueVO;
import com.atguigu.gmall.manager.vo.BaseAttrValueVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
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
        //1,修改还是添加,修改的id不为空,而添加为空
        //1、修改还是添加
        if(baseAttrInfoAndValueVO.getId()!=null) {
            //修改,检查新提交的属性名不能是空串.....
            //1、修改基本属性名
            //2、修改这个属性对应的所有的值
            BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
            //将vo中的所有属性复制到bean中
            BeanUtils.copyProperties(baseAttrInfoAndValueVO, baseAttrInfo);

            List<BaseAttrValue> values = new ArrayList<>();
            //遍历页面上的数据vo
            for (BaseAttrValueVO baseAttrValueVo : baseAttrInfoAndValueVO.getAttrValues()) {
                //将这个vo里面的数据封装到BaseAttrValue这个对象
                BaseAttrValue baseAttrValue = new BaseAttrValue();
                BeanUtils.copyProperties(baseAttrValueVo, baseAttrValue);
                values.add(baseAttrValue);
            }
            ;
            //将复制好的list设置在attrInfo中
            baseAttrInfo.setAttrValues(values);
            baseAttrInfoService.saveOrUpdateOrDeleteAttrInfoAndValue(baseAttrInfo);
            log.info("复制属性完成：{}", baseAttrInfo);
        }else {
            //添加属性名
            BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
            //将vo中的所有属性复制到bean中
            BeanUtils.copyProperties(baseAttrInfoAndValueVO, baseAttrInfo);

            List<BaseAttrValue> values = new ArrayList<>();
            //遍历页面上的数据vo
            for (BaseAttrValueVO baseAttrValueVo : baseAttrInfoAndValueVO.getAttrValues()) {
                //将这个vo里面的数据封装到BaseAttrValue这个对象
                BaseAttrValue baseAttrValue = new BaseAttrValue();
                BeanUtils.copyProperties(baseAttrValueVo, baseAttrValue);
                values.add(baseAttrValue);
            }
            ;
            //将复制好的list设置在attrInfo中
            baseAttrInfo.setAttrValues(values);
            baseAttrInfo.setCatalog3Id(baseAttrInfoAndValueVO.getCatalog3Id());
            //log.info("复制完成后的对象{},它的c3id是{}",baseAttrInfo,baseAttrInfo.getCatalog3Id());
            baseAttrInfoService.saveOrUpdateOrDeleteAttrInfoAndValue(baseAttrInfo);
        }
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
