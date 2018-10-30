package com.atguigu.gmall.passport.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.passport.mapper.UserInfoMapper;
import com.atguigu.gmall.passport.mapper.user.UserAddressMapper;
import com.atguigu.gmall.user.UserAddress;
import com.atguigu.gmall.user.UserInfo;
import com.atguigu.gmall.user.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserAddressMapper userAddressMapper;


    @Override
    public UserInfo login(UserInfo userInfo) {
        String passwd = userInfo.getPasswd();
        //密码更新为md5去数据库查询
        String md5Hex = DigestUtils.md5Hex(passwd);
        userInfo.setPasswd(md5Hex);

        UserInfo selectOne = userInfoMapper.selectOne(new QueryWrapper<UserInfo>()
                .eq("login_name", userInfo.getLoginName())
                .eq("passwd", userInfo.getPasswd()));

        return selectOne;
    }

    @Override
    public List<UserAddress> getUserAdressesByUserId(Integer userId) {
        List<UserAddress> userAddresses = userAddressMapper.selectList(new QueryWrapper<UserAddress>().eq("user_id", userId));
        return userAddresses;
    }

    /**
     * 下单页面,查询用户的收货地址,收货人
     * @param userId
     * @return
     */
}
