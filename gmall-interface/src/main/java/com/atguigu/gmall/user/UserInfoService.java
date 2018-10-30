package com.atguigu.gmall.user;

import java.util.List;

public interface UserInfoService {

    /**
     * 登陆
     * @param userInfo  按照带来的账号密码查询用户的详情
     * @return 返回用户在数据库的详细信息
     */
    public UserInfo login(UserInfo userInfo);

    /**
     * 查询下订单的用户
     * @param userId
     * @return
     */
    List<UserAddress> getUserAdressesByUserId(Integer userId);

    /**
     * 查询用户的收货人,收货地址
     * @param userId
     * @return
     */
}
