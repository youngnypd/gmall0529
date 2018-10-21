package com.atguigu.gmall.es;

import lombok.Data;

import java.io.Serializable;

/**
 * 页面发过来的筛选请求可能的参数
 */
@Data
public class SkuSearchParamESVo implements Serializable {
    private String keyWord;//关键字
    private Integer catalog3Id;
    private Integer[] valueId;//平台属性值id,会有多个
    private Integer pageNo = 1;
    private Integer pageSize = 12;
    private String sortField = "hotScore";//按什么排序,默认按照点击量
    private String sortOrder = "desc";//升序还是降序,默认降序
}
