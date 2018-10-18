package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.es.SkuBaseAttrEsVo;
import com.atguigu.gmall.manager.BaseAttrInfo;
import com.atguigu.gmall.manager.constant.RedisCacheKeyConstant;
import com.atguigu.gmall.manager.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manager.mapper.sku.SkuAttrValueMapper;
import com.atguigu.gmall.manager.mapper.sku.SkuImageMapper;
import com.atguigu.gmall.manager.mapper.sku.SkuInfoMapper;
import com.atguigu.gmall.manager.mapper.sku.SkuSaleAttrValueMapper;
import com.atguigu.gmall.manager.mapper.spu.SpuSaleAttrMapper;
import com.atguigu.gmall.manager.sku.*;
import com.atguigu.gmall.manager.spu.SpuSaleAttr;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class SkuManagerServiceImpl implements SkuManagerService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    JedisPool jedisPool;//将数据存到redis中,不需要再从数据库中查找
    @Override
    public List<BaseAttrInfo> getBaseAttrInfoByCatalog3Id(Integer catalog3Id) {
        return baseAttrInfoMapper.getBaseAttrInfoByCatalog3Id(catalog3Id);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrBySpuId(Integer spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrBySpuId(spuId);
    }

    /**
     * 页面传过来的sku数据进行大保存
     * @param skuInfo
     */
    @Transactional
    @Override
    public void bigSave(SkuInfo skuInfo) {
        log.info("页面传过来的sku数据是:{}",skuInfo);//
        //1.保存skuinfo的基本信息
        skuInfoMapper.insert(skuInfo);
        Integer skuId = skuInfo.getId();
        //2.保存skuinfo的图片信息
        List<SkuImage> skuImages = skuInfo.getSkuImages();
        for (SkuImage skuImage : skuImages) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insert(skuImage);
        }
        //3.保存skuinfo的平台属性
        List<SkuAttrValue> skuAttrValues = skuInfo.getSkuAttrValues();
        for (SkuAttrValue skuAttrValue : skuAttrValues) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insert(skuAttrValue);
        }
        //4保存skuinfo的销售属性
        List<SkuSaleAttrValue> skuSaleAttrValues = skuInfo.getSkuSaleAttrValues();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValues) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }
    }

    /**
     * 通过spuid获取sku信息
     * @param spuId
     * @return
     */
    @Override
    public List<SkuInfo> getSkuInfoBySpuId(Integer spuId) {
        return skuInfoMapper.selectList(new QueryWrapper<SkuInfo>().eq("spu_id", spuId));
    }

    /**
     * 通过商品id获取商品所有信息
     * @param skuId
     * @return
     *
     * 逻辑基本是先从缓存中找,找不到再从数据中找skuinfo信息
     */
    @Override
    public SkuInfo getSkuInfoBySkuId(Integer skuId) {
        SkuInfo result = new SkuInfo();
        Jedis jedis = jedisPool.getResource();
        String key = RedisCacheKeyConstant.SKU_INFO_PREFIX + skuId +RedisCacheKeyConstant.SKU_INFO_SUFFIX;
        String s = jedis.get(key);
        if (s != null) {
            log.debug("缓存中找到了:{}",s);
            result = JSON.parseObject(s,SkuInfo.class);
            jedis.close();
        }
        if("null".equals(s)){
            //防止缓存穿透的
            //缓存中存了，只不过这是你数据库给我的
            //之前数据库查过，但是没有，所以给缓存中放了一个null串
            return null;
        }
        if(s==null){
            //当这个数据等于null的时候
            //缓存中没有必须从数据库先查出来，在放到缓存
            //我们需要加锁
            // 拿到锁再去查数据库；
            String token = UUID.randomUUID().toString();
            String lock = jedis.set(RedisCacheKeyConstant.LOCK_SKU_INFO, "ABC", "NX", "EX", RedisCacheKeyConstant.LOCK_TIMEOUT);

            if(lock == null){
                //没有拿到锁
                log.debug("没有获取到锁等待重试");
                try {
                    Thread.sleep(1000);//等待一秒重试
                } catch (InterruptedException e) {
                    log.error("线程出错了{}", e);
                }
                //自旋锁
                getSkuInfoBySkuId(skuId);
            }else if("OK".equals(lock)) {
                log.debug("获取到锁，查数据库了：");
                result = getSkuInfoFromDb(skuId);
                //sku:3:info --xxxxx
                //sku:4:info --yyyy
                //将对象转为json存到redis中
                String skuInfoJson = JSON.toJSONString(result);
                //json.null  "null"
                log.debug("从数据库查到的数据：{}",skuInfoJson);
                //存到缓存中,第二天以后就会有人新查数据
                if("null".equals(skuInfoJson)){
                    //空数据缓存时间短
                    jedis.setex(key,RedisCacheKeyConstant.SKU_INFO_NULL_TIMEOUT,skuInfoJson);
                }else{
                    //正常数据缓存时间长
                    jedis.setex(key,RedisCacheKeyConstant.SKU_INFO_TIMEOUT,skuInfoJson);
                }
            }
            jedis.close();
        }
        return result;
    }
    private SkuInfo getSkuInfoFromDb(Integer skuId){
        log.debug("从数据库获取信息!!!!!");
        //1.查询skuinfo基本信息
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if(skuInfo == null){
            return null;
        }
        //2.查询skuinfo的图片信息
        List<SkuImage> skuImages = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
        skuInfo.setSkuImages(skuImages);
        //3.查询skuinfo销售属性
        List<SkuAllSaleAttrAndValueTo> skuAllSaleAttrAndValueTos= skuInfoMapper.getAllSaleAttrAndValueTo(skuInfo.getId(),skuInfo.getSpuId());
        skuInfo.setSkuAllSaveAttrAndValueTos(skuAllSaleAttrAndValueTos);
        return skuInfo;
    }
    @Override
    public List<SkuAttrValueMappingTo> getSkuAttrValueMapping(Integer spuId) {
        return skuInfoMapper.getSkuAttrValueMapping(spuId);
    }

    /**
     * 获取sku所有平台属性id
     * @param skuId
     * @return
     */
    @Override
    public List<SkuBaseAttrEsVo> getSkuBaseAttrValueIdBySkuId(Integer skuId) {
        List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValue>().eq("sku_id", skuId));
        //将skuAttrValues中的value_id存到SkuBaseAttrEsVo中

        List<SkuBaseAttrEsVo> list = new ArrayList<SkuBaseAttrEsVo>();
        for (SkuAttrValue skuAttrValue : skuAttrValues) {
            Integer valueId = skuAttrValue.getValueId();
            SkuBaseAttrEsVo skuBaseAttrEsVo = new SkuBaseAttrEsVo();
            skuBaseAttrEsVo.setValueId(valueId);
            list.add(skuBaseAttrEsVo);
        }
        return list;
    }
}
