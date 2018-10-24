package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.CartItem;
import com.atguigu.gmall.cart.CartService;
import com.atguigu.gmall.cart.CartVo;
import com.atguigu.gmall.cart.SkuItem;
import com.atguigu.gmall.cart.constant.CartConstant;
import com.atguigu.gmall.manager.sku.SkuInfo;
import com.atguigu.gmall.manager.sku.SkuManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    JedisPool jedisPool;
    @Reference
    SkuManagerService skuManagerService;
    /**
     * 用户没有登录,添加购物车
     * @param skuId
     * @param cartKey   购物车id
     * @param num
     * @return
     */
    @Override
    public String addToCartUnLogin(Integer skuId, String cartKey, Integer num) {
        Jedis jedis = jedisPool.getResource();

        //cart-key : djskaljdaskljdasdkja
        if(!StringUtils.isEmpty(cartKey)){
            //之前创建过购物车
            Boolean exists = jedis.exists(cartKey);
            if(exists == false){
                //传来的购物车这个键不存在；也要新建购物车；假设传来的是非法的 cart-key:ddddddd
                String newCartKey = null;
                try {
                    newCartKey = createCartVo(skuId, num,false,null);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return newCartKey;
            }else{
                String skuInfoJson = jedis.hget(cartKey, skuId + "");
                if(!StringUtils.isEmpty(skuInfoJson)){
                    //1、购物车中有此商品，叠加数量
                    CartItem cartItem = JSON.parseObject(skuInfoJson, CartItem.class);
                    cartItem.setNum(cartItem.getNum()+num);
                    //价格重新算一下
                    cartItem.setTotalPrice(cartItem.getTotalPrice());
                    String toJSONString = JSON.toJSONString(cartItem);
                    jedis.hset(cartKey,skuId + "",toJSONString);

                }else {
                    //2、购物车中无此商品，新增商品
                    try {
                        CartItem cartItem = new CartItem();
                        SkuInfo skuInfo = skuManagerService.getSkuInfoBySkuId(skuId);
                        SkuItem skuItem = new SkuItem();
                        BeanUtils.copyProperties(skuInfo,skuItem);
                        cartItem.setNum(num);
                        cartItem.setSkuItem(skuItem);
                        cartItem.setTotalPrice(cartItem.getTotalPrice());

                        //添加商品
                        String jsonString = JSON.toJSONString(cartItem);
                        jedis.hset(cartKey,skuItem.getId()+"",jsonString);



                        //更新顺序字段
                        //拿出之前的顺序，把顺序也更新一下
                        String fieldOrder = jedis.hget(cartKey, "fieldOrder");
                        List list = JSON.parseObject(fieldOrder, List.class);
                        //把新的商品放进list
                        list.add(skuId);
                        String string = JSON.toJSONString(list);
                        jedis.hset(cartKey,"fieldOrder",string);
                    } catch (Exception e) {
                    }
                }
            }

        }else {
            //无购物车就新建
            try {
                return createCartVo(skuId,num,false,null);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        jedis.close();
        //返回之前的cart-key
        return cartKey;
    }

    /**
     * 用户登录了,添加购物车
     * @param skuId
     * @param userId
     * @param num
     */
    @Override
    public String addToCartLogin(Integer skuId, Integer userId, Integer num) throws InterruptedException {
        Jedis jedis = jedisPool.getResource();
        //先判断这个用户是否有购物车
        Boolean flag = jedis.exists(CartConstant.USER_CART_PREFIX + userId);
        if (!flag) {
            //用户没有购物车,直接创建购物车

                String cartKey = createCartVo(skuId, num, true, userId);
                jedis.close();
                return cartKey;

        } else {
            //0,有购物车
            String cartKey = CartConstant.USER_CART_PREFIX + userId;
            //1,购物车中是否有这个商品
            String hget = jedis.hget(cartKey, skuId.toString());
            if (hget != null) {
                //有这个商品,更新数量,更新价格
                CartItem cartItem = JSON.parseObject(hget, CartItem.class);
                cartItem.setNum(cartItem.getNum() + num);
                cartItem.setTotalPrice(cartItem.getTotalPrice());
                //再继续存到redis
                String s = JSON.toJSONString(cartItem);
                jedis.hset(cartKey, skuId.toString(), s);
                return cartKey;
            } else {
                //没有这个商品,添加到购物车
                CartItem cartItem = new CartItem();
                SkuItem skuItem = new SkuItem();
                SkuInfo skuInfo = skuManagerService.getSkuInfoBySkuId(skuId);
                BeanUtils.copyProperties(skuInfo, skuItem);
                //设置到购物项中
                cartItem.setSkuItem(skuItem);
                cartItem.setNum(num);
                cartItem.setTotalPrice(cartItem.getTotalPrice());
                String toJSONString = JSON.toJSONString(cartItem);
                jedis.hset(cartKey, skuId.toString(), toJSONString);

                //拿出之前的顺序，把顺序也更新一下
                String fieldOrder = jedis.hget(cartKey, "fieldOrder");
                List list = JSON.parseObject(fieldOrder, List.class);
                //把新的商品放进list
                list.add(skuId);
                String string = JSON.toJSONString(list);
                jedis.hset(cartKey,"fieldOrder",string);

            }
            jedis.close();
            return cartKey;
        }
    }

    @Override
    public void mergeCart(String cartKey, Integer userId) {
        //查出临时购物车的信息,肯定是登陆之后
        List<CartItem> cartInfoList = getCartInfoList(cartKey, false);
        if (cartInfoList != null && cartInfoList.size() > 0) {
            for (CartItem cartItem : cartInfoList) {
                try {
                    addToCartLogin(cartItem.getSkuItem().getId(), userId, cartItem.getNum());
                } catch (InterruptedException e) {
                    log.error("服务器出问题了, 请联系管理员:{}", e);
                }
            }
        }
        //合并完成,删除临时购物车
        Jedis jedis = jedisPool.getResource();
        jedis.del(cartKey);
    }

    @Override
    public CartItem getCartItemInfo(String cartKey, Integer skuId) {
        Jedis jedis = jedisPool.getResource();
        String hget = jedis.hget(cartKey, skuId.toString());
        CartItem cartItem = JSON.parseObject(hget, CartItem.class);
        jedis.close();
        return cartItem;
    }

    @Override
    public CartVo getYourCartByCartKey(String cartKey) {
        return null;
    }

    @Override
    public List<CartItem> getCartInfoList(String cartKey, boolean login) {

        if (login) {
            //说明登陆了,
            cartKey = CartConstant.USER_CART_PREFIX + cartKey;
        }
        Jedis jedis = jedisPool.getResource();
        //键是购物车的键,值是购物车里面的东西
        List<CartItem> cartItemList =  new ArrayList<CartItem>();
        String fieldOrder = jedis.hget(cartKey, "fieldOrder");
        List list = JSON.parseObject(fieldOrder, List.class);
        for (Object o : list) {
            int idSort = Integer.parseInt(o.toString());
            String hget = jedis.hget(cartKey, idSort + "");
            CartItem cartItem = JSON.parseObject(hget, CartItem.class);
            cartItemList.add(cartItem);
        }
        return cartItemList;
    }

    /**
     * 勾选购物项
     * @param skuId
     * @param checkFlag
     * @param tempCartKey
     * @param userId
     * @param loginFlag
     */
    @Override
    public void checkItem(Integer skuId, Boolean checkFlag, String tempCartKey, int userId, boolean loginFlag) {
        //购物车勾选
        String caryKey = loginFlag?CartConstant.USER_CART_PREFIX+ userId:tempCartKey;
        CartItem cartItem = getCartItemInfo(caryKey, skuId);
        //设置勾选状态
        cartItem.setCheck(checkFlag);

        //修改购物车数据
        String string = JSON.toJSONString(cartItem);
        Jedis jedis = jedisPool.getResource();
        jedis.hset(caryKey,skuId+"",string);
        jedis.close();
    }

    /**
     * 新建购物车的方法,只需要返回购物车的id即可
     * @param skuId
     * @param num
     * @param login
     * @param userId
     * @return
     */
    private String createCartVo(Integer skuId, Integer num,boolean login,Integer userId) throws InterruptedException {
        String newCartKey = null;
        Jedis jedis = jedisPool.getResource();
        if (login) {
            //已经登录
            //newCartKey = CartConstant.
            newCartKey = CartConstant.USER_CART_PREFIX + userId;
        } else {
           newCartKey = CartConstant.TEMP_CART_PREFIX + UUID.randomUUID().toString().substring(0, 10).replaceAll("-","");
        }
        //保存购物车数据；
        //1、查出商品的详细信息
        SkuInfo skuInfo = skuManagerService.getSkuInfoBySkuId(skuId);
        //redis用什么样的方式存储比较合适?
        //key -value
        //newCartKey - [{},{},{}]
        //修改2商品的数量 [{},{2},{}]
        //hash
        // newCartKey  field value
        // temp:cart:001  1 {id:1,skuName:ddd,num:xxx};
        // temp:cart:001  3 {id:3,skuName:ddd,num:xxx};
        //问题？购物车数据是有序的？
        // temp:cart:001  skuIdOrder [1,2,3]
        //
        CartItem cartItem = new CartItem();
        SkuItem skuItem = new SkuItem();
        //2、拷贝商品的详细信息进来，准备保存到redis
        BeanUtils.copyProperties(skuInfo,skuItem);
        cartItem.setSkuItem(skuItem);
        cartItem.setNum(num);
        cartItem.setTotalPrice(cartItem.getTotalPrice());

        String jsonString = JSON.toJSONString(cartItem);


        List<Integer> ids = new ArrayList<>();
        ids.add(cartItem.getSkuItem().getId());
        Long hset = jedis.hset(newCartKey, skuItem.getId()+"", jsonString);
        String fieldOrder = JSON.toJSONString(ids);
        jedis.hset(newCartKey,"fieldOrder",fieldOrder);


        jedis.close();
        return  newCartKey;
    }
}
