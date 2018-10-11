package com.atguigu.gmall.manager.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
public class BaseAttrInfoAndValueVO implements Serializable{
    private Integer id;
    private String attrName;
    private List<BaseAttrValueVO> attrValues;
    private Integer catalog3Id;
}
