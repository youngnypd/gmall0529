package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manager.BaseAttrInfo;
import com.atguigu.gmall.manager.BaseAttrInfoService;
import com.atguigu.gmall.manager.BaseAttrValue;
import com.atguigu.gmall.manager.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manager.mapper.BaseAttrValueMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
public class BaseAttrInfoServiceImpl implements BaseAttrInfoService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> getBaseAttrInfoByCatalog3Id(Integer catalog3Id) {
        return baseAttrInfoMapper.selectList(new QueryWrapper<BaseAttrInfo>().eq("catalog3_id",catalog3Id));
    }

    @Override
    public List<BaseAttrValue> getBaseAttrValueByAttrId(Integer baseAttrInfoId) {
        return baseAttrValueMapper.selectList(new QueryWrapper<BaseAttrValue>().eq("attr_id",baseAttrInfoId));
    }

    @Transactional
    @Override
    public void saveOrUpdateOrDeleteAttrInfoAndValue(BaseAttrInfo baseAttrInfo) {
        log.info("传到service的对象是{}",baseAttrInfo);
        //判断是修改,删除还是新增
        if(baseAttrInfo.getId() != null){
            //1,修改基本属性名
            baseAttrInfoMapper.updateById(baseAttrInfo);
            //2,删除没有提交过来的数据,基本属性值
            List<BaseAttrValue> attrValues = baseAttrInfo.getAttrValues();
            List<Integer> ids = new ArrayList<>();
            for (BaseAttrValue attrValue : attrValues) {
               Integer id = attrValue.getId();
                if(id != null){
                    ids.add(id);
                }
            }
            //真正的删除功能,基本属性值如果没有提交过来id,那么就删除
            baseAttrValueMapper.delete(new QueryWrapper<BaseAttrValue>()
                    .notIn("id", ids).eq("attr_id", baseAttrInfo.getId()));
            for (BaseAttrValue attrValue : attrValues) {
                //2.2）、提交过来的数据，如果有id就是修改
                if(attrValue.getId()!=null){
                    baseAttrValueMapper.updateById(attrValue);
                }else {
                    //2.3）、提交过来的数据，如果没有id就是新增
                    attrValue.setAttrId(baseAttrInfo.getId());
                    baseAttrValueMapper.insert(attrValue);
                }

            }
        }else {
            //1.添加基本属性名\
            baseAttrInfoMapper.insert(baseAttrInfo);
            //2.添加属性值
            //获取到插入的属性名的id
            Integer attrId = baseAttrInfo.getId();
            List<BaseAttrValue> attrValues = baseAttrInfo.getAttrValues();
            for (BaseAttrValue attrValue : attrValues) {
                //将属性名的id赋值给属性值
                attrValue.setAttrId(attrId);
                baseAttrValueMapper.insert(attrValue);
            }

        }
    }
}
