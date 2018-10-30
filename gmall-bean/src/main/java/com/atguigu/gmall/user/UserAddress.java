package com.atguigu.gmall.user;

import com.atguigu.gmall.SuperBean;
import lombok.Data;

@Data
public class UserAddress extends SuperBean {
    //id  user_address        user_id  consignee  phone_num  is_default
    private String userAddress;
    private Integer userId;
    private String consignee;
    private String phoneNum;
    private String isDefault;
}
