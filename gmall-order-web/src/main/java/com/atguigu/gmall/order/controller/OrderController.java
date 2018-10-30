package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotation.LoginRequired;
import com.atguigu.gmall.order.OrderInfo;
import com.atguigu.gmall.order.OrderInfoTo;
import com.atguigu.gmall.order.OrderService;
import com.atguigu.gmall.order.OrderSubmitVo;
import com.atguigu.gmall.user.UserAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
@Slf4j
@Controller
public class OrderController {
    @Reference
    OrderService orderService;

    /**
     * 只收集送货地址,用户给的备注
     * @param orderSubmitVo
     * @param request
     * @return
     */
    @LoginRequired//如果没有登录就去登录页面
    @RequestMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, HttpServletRequest request, Model model){
        Map<String, Object> userInfo =
                (Map<String, Object>) request.getAttribute("userInfo");
        log.info("页面收到的数据是: {}", orderSubmitVo);
        //验证token
        boolean flag = orderService.verifyToken(orderSubmitVo.getTradeToken());
        if (!flag) {
            request.setAttribute("errorMsg", "订单数据失效,请重新下单");
            return "tradeFail";
        }
        //验证库存
        Integer userId = Integer.parseInt(userInfo.get("id").toString());
        List<String> lackOfStock = null;
        try {
            lackOfStock = orderService.verifyStock(userId);
        } catch (IOException e) {
            log.debug("库存验证失败,请联系管理员{}",e);
        }
        if (lackOfStock != null && lackOfStock.size() > 0) {
            request.setAttribute("errorMsg", "您所选择的商品库存不足" + lackOfStock);
            return  "tradeFail";
        }
        //验证都ok,那么直接下单
        OrderInfoTo orderInfoTo = new OrderInfoTo();//页面需要的数据
        orderInfoTo.setOrderComment(orderSubmitVo.getOrderComment());
        Integer userAddressId = orderSubmitVo.getUserAddressId();
        UserAddress userAddress = orderService.getUserAddressById(userAddressId);
        orderInfoTo.setDeliveryAddress(userAddress.getUserAddress());
        orderInfoTo.setConsignee(userAddress.getConsignee());
        orderInfoTo.setConsigneeTel(userAddress.getPhoneNum());
        //检查完毕,下单
        OrderInfo orderInfo = null;
        try{
            orderInfo = orderService.createOrder(userId, orderInfoTo);
            //订单创建好了之后,给页面返回订单对象
            model.addAttribute("orderInfo", orderInfo);
        } catch (Exception e) {
            request.setAttribute("errorMsg", "网络异常" + e.getMessage());
            return "tradeFail";
        }
        return "paymentPage";
    }

    public String listOrder(){
        return "list";
    }
}
