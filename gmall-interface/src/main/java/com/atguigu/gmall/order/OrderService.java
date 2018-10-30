package com.atguigu.gmall.order;

import com.atguigu.gmall.user.UserAddress;

import java.io.IOException;
import java.util.List;

public interface OrderService {
    String createTradeToken();

    boolean verifyToken(String tradeToken);

    List<String> verifyStock(Integer userId) throws IOException;

    /**
     * 创建订单
     * @param userId
     * @return
     */
    OrderInfo createOrder(Integer userId, OrderInfoTo orderInfoTo);

    /**
     * 获取用户的地址
     * @param userAddressId
     * @return
     */
    UserAddress getUserAddressById(Integer userAddressId);
}
