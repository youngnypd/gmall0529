package com.atguigu.gmall.cart;

import java.util.List;

/**
 * 购物车功能
 */
public interface CartService {
    /**
     * 没登陆的情况下添加购物车
     * @param skuId
     * @param cartKey   购物车id
     * @param num
     * @return
     */
    String addToCartUnLogin(Integer skuId, String cartKey, Integer num);

    /**
     * 登陆的情况下添加购物车
     * @param skuId
     * @param userId
     * @param num
     */
    String addToCartLogin(Integer skuId, Integer userId, Integer num) throws InterruptedException;

    /**
     * 合并临时购物车和登陆之后的购物车
     * @param cartKey
     * @param userId
     */
    void mergeCart(String cartKey, Integer userId);

    /**
     * 获取购物项
     * @param cartKey
     * @param skuId
     * @return
     */
    CartItem getCartItemInfo(String cartKey, Integer skuId);

    /**
     * 获取购物车
     * @param cartKey
     * @return
     */
    CartVo getYourCartByCartKey(String cartKey);

    /**
     * 获取购物车中所有的购物项
     * @param cartKey
     * @param login
     * @return
     */
    List<CartItem> getCartInfoList(String cartKey, boolean login);

    /**
     * 勾选购物项
     * @param skuId
     * @param checkFlag
     * @param tempCartKey
     * @param userId
     * @param loginFlag
     */
    void checkItem(Integer skuId, Boolean checkFlag, String tempCartKey, int userId, boolean loginFlag);
}
