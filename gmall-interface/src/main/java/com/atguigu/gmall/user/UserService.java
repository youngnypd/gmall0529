package com.atguigu.gmall.user;

import java.util.List;

public interface UserService {

    /**
     * 获取用户
     * @param id
     * @return
     */
    public User getUser(String id);

    /**
     * 购买电影
     * @param uid  用户id
     * @param mid  电影id
     */
    public void buyMovie(String uid,String mid);

    /**
     * 订单页面需要的收货人以及收货地址等信息
     * @param userId
     * @return
     */
    List<UserAddress> getUserAdressesByUserId(Integer userId);
}
