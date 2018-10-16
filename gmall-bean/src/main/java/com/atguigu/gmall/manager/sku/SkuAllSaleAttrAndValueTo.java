package com.atguigu.gmall.manager.sku;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SkuAllSaleAttrAndValueTo implements Serializable {
    private Integer id;
    private Integer spuId;
    private Integer saleAttrId;
    private String saleAttrName;
    // sale_attr_value_id  sale_attr_value_name   sku_id  is_check
    private List<SkuAllSaveAttrValueContentTo> valueContent;
}
