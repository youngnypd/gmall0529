package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotation.LoginRequired;
import com.atguigu.gmall.cart.CartItem;
import com.atguigu.gmall.cart.CartService;
import com.atguigu.gmall.cart.CartVo;
import com.atguigu.gmall.order.OrderService;
import com.atguigu.gmall.order.TradePageVo;
import com.atguigu.gmall.user.UserAddress;
import com.atguigu.gmall.user.UserInfo;
import com.atguigu.gmall.user.UserInfoService;
import com.atguigu.gmall.user.UserService;
import com.atguigu.gmall.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
@Slf4j
@Controller
public class TradeController {
    @Reference
    CartService cartService;
    @Reference
    UserInfoService userInfoService;
    @Reference
    OrderService orderService;

    /**
     * 订单页面所需要的所有信息,以及防止重复提交
     * @param request
     * @return
     */
    @LoginRequired
    @RequestMapping("/trade")
    public String index(HttpServletRequest request){
        //1,验证商品,如果没有商品选中,返回到购物车页面
        //1.1, 获取用户信息
        Map<String, Object> userInfo = (Map<String, Object>)request.getAttribute("userInfo");
        Integer userId = Integer.parseInt(userInfo.get("id").toString());
        //1.2  获取这个用户所选中的商品信息
        String cartKey = CookieUtils.getCookieValue(request,"cart_key");

        if (cartKey != null) {
            cartService.mergeCart(cartKey, userId);
            log.error("打印出来的临时购物车是: {}",cartKey);
        }

        List<CartItem> cartItemList = cartService.getCheckedCartItemList(userId);
        //2,查询下单页面需要的信息,收货人地址等等
        List<UserAddress> userAddresses = userInfoService.getUserAdressesByUserId(userId);
        //3,防重复提交
        //3、防重复提交的；生成一个令牌，服务器一份，页面一份
        String token = orderService.createTradeToken();//创建一个交易令牌，服务端也保存了

        TradePageVo tradePageVo = new TradePageVo();
        CartVo cartVo = new CartVo();
        cartVo.setCartItems(cartItemList);
        BigDecimal totalPrice = cartVo.getTotalPrice();
        tradePageVo.setUserAddresses(userAddresses);
        tradePageVo.setCartItems(cartItemList);
        tradePageVo.setTotalPrice(totalPrice);
        request.setAttribute("token", token);
        request.setAttribute("tradeInfo", tradePageVo);
        log.error("传给结算页面的数据是: {}", tradePageVo.getCartItems().get(0));
        return "trade";
    }
}
