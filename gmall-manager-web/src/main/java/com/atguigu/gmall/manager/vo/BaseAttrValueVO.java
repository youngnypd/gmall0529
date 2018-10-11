package com.atguigu.gmall.manager.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseAttrValueVO implements Serializable{
    private Integer id;
    private String valueName;
    private Integer attrId;

}
