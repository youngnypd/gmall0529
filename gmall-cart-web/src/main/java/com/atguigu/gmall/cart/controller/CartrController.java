package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotation.LoginRequired;
import com.atguigu.gmall.cart.CartItem;
import com.atguigu.gmall.cart.CartService;
import com.atguigu.gmall.cart.CartVo;
import com.atguigu.gmall.constant.CookieConstant;
import com.atguigu.gmall.user.UserInfo;
import com.atguigu.gmall.utils.CookieUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
public class CartrController {
    @Reference
    CartService cartService;


    /**
     * 购物车勾选
     * @param skuId
     * @param checkFlag
     * @param request
     * @return
     */
    @LoginRequired(needLogin = false)
    @ResponseBody
    @RequestMapping("/checkItem")
    public String checkItem(Integer skuId,Boolean checkFlag,HttpServletRequest request){
        Map<String,Object> userInfo = (Map<String, Object>)
                request.getAttribute(CookieConstant.LOGIN_USER_INFO_KEY);
        String tempCartKey = CookieUtils.getCookieValue(request, CookieConstant.COOKIE_CART_KEY);

        boolean loginFlag = userInfo==null?false:true;
        int userId = 0;
        try {
            userId = Integer.parseInt(userInfo.get("id").toString());
        }catch (Exception e){}

        cartService.checkItem(skuId,checkFlag,tempCartKey,userId,loginFlag);
        return "ok";
    }

    /**
     * 用户的信息放在了请求域中
     * @param request
     * @return
     */
    @LoginRequired(needLogin = false)//不需要登陆,只要用户信息
    @RequestMapping("/cartList")
    public String getCartInfoPage(HttpServletRequest request, HttpServletResponse response){
        Map<String,Object> userInfo = (Map<String, Object>) request.getAttribute(CookieConstant.LOGIN_USER_INFO_KEY);
        //判断是否需要合并购物车？
        //temp:cart:9c766092f
        String tempCart = CookieUtils.getCookieValue(request, CookieConstant.COOKIE_CART_KEY);
        if(!StringUtils.isEmpty(tempCart) && userInfo!=null){
            //说明有临时购物车。合并购物车
            cartService.mergeCart(tempCart,Integer.parseInt(userInfo.get("id").toString()));
            //相当于是将之前的临时购物车键覆盖掉
            Cookie cookie = new Cookie(CookieConstant.COOKIE_CART_KEY, "372863287");
            cookie.setMaxAge(0);
            //立即删除之前临时购物车数据
            response.addCookie(cookie);
        }

        boolean login = false;
        String cartKey = "";
        if(userInfo!=null){
            //1、登陆了
            login = true;
            cartKey = userInfo.get("id").toString()+"";//将购物车的键变成

        }else{
            //2、没登录
            login = false;
            cartKey = CookieUtils.getCookieValue(request, CookieConstant.COOKIE_CART_KEY);
        }

        //查询数据
        List<CartItem> cartItems =  cartService.getCartInfoList(cartKey,login);

        //购物车,购物车里面再添加购物项
        CartVo cartVo = new CartVo();
        cartVo.setCartItems(cartItems);
        cartVo.setTotalPrice(cartVo.getTotalPrice());


        request.setAttribute("cartVo",cartVo);
        //来到列表页
        return "cartList";
    }

    /**
     * 添加购物车
     * @param
     * @param num
     * @return
     */
    @LoginRequired(needLogin = false)
    @RequestMapping("/addToCart")
    public String addToCart(Integer skuId, Integer num, HttpServletRequest request,
                            HttpServletResponse response) throws InterruptedException {
        //如果登录了,直接放入到对应usr的购物车中
        //判断是否登陆，登陆了用user:cart:12:info在redis中
        Map<String,Object> loginUser = (Map<String, Object>) request.getAttribute(CookieConstant.LOGIN_USER_INFO_KEY);
        String cartKey = null;
        if(loginUser == null){
            cartKey = CookieUtils.getCookieValue(request, CookieConstant.COOKIE_CART_KEY);
            //未登陆情况下的处理
            if(StringUtils.isEmpty(cartKey)){
                //返回的是给你造的购物车在redis'中存数据用的key。
                cartKey = cartService.addToCartUnLogin(skuId,null,num);
                Cookie cookie = new Cookie(CookieConstant.COOKIE_CART_KEY, cartKey);
                cookie.setMaxAge(CookieConstant.COOKIE_CART_KEY_MAX_AGE);
                response.addCookie(cookie);
            }else {
                cartKey = cartService.addToCartUnLogin(skuId, cartKey, num);

            }
        }else{
            Integer userId = Integer.parseInt(loginUser.get("id").toString());
            //合并购物车；
            cartKey = CookieUtils.getCookieValue(request, CookieConstant.COOKIE_CART_KEY);
            if(StringUtils.isEmpty(cartKey)){
                //cookie没有临时购物车
                cartKey = cartService.addToCartLogin(skuId, userId, num);
            }else {
                //有临时购物车，先合并在加购物车
                cartService.mergeCart(cartKey,userId);
                cartKey = cartService.addToCartLogin(skuId,userId,num);
                //删掉cart-key这个cookie
                CookieUtils.removeCookie(response,CookieConstant.COOKIE_CART_KEY);
            }
        }
        //没登录，临时搞一个购物车的id了;response.addCookie("cart-key":"dsajldjaskldj")
        //这个id在redis中存数据
        //把购物车刚才的数据查出来 购物车的id，查那个
        CartItem cartItem = cartService.getCartItemInfo(cartKey,skuId);
        request.setAttribute("skuInfo",cartItem);
        return "success";
    }
}

