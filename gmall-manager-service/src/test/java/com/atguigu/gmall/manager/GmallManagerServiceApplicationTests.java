package com.atguigu.gmall.manager;

import com.atguigu.gmall.manager.mapper.BaseCatalog1Mapper;
import com.atguigu.gmall.manager.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 1、导入mybatis-plus的starter
 * 2、编写javaBaen。编写mapper接口（继承BaseMapper）
 * 3、@MapperScan("com.atguigu.gmall.manager.mapper")
 *
 * 高级：
 * 	1）、逻辑删除
 * 		1、在application.properties说明逻辑删除的规则
 * 	    2、在javaBean里面加上逻辑删除字段并且用@TableLogic
 * 	    3、自定义一个mybatisplus的配置类，注入逻辑删除插件即可
 *
 */

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManagerServiceApplicationTests {

	@Autowired
	UserMapper userMapper;

	@Autowired
	BaseCatalog1Mapper mapper;


	@Autowired
	CatalogService catalogService;

	@Autowired
	StringRedisTemplate stringRedisTemplate;//k-v都是string

	@Autowired
	RedisTemplate redisTemplate;//k-v 都是object
	@Autowired
	JedisPool jedisPool;
	@Test
	public void testJedisPool(){
		Jedis jedis = jedisPool.getResource();
		jedis.set("jedis","jedisValue");
		String value = jedis.get("jedis");
		System.out.println("jedis的值是:"+value);
	}
	@Test
	public void testRedisTemplate(){
		ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
		operations.set("hello","world",20,TimeUnit.SECONDS);
		System.out.println("redis存储了吗?");

	}

	@Test
	public void testCatalogService(){
		List<BaseCatalog1> allBaseCatalog1 = catalogService.getAllBaseCatalog1();
		log.info("一级分类信息：{}",allBaseCatalog1);

		List<BaseCatalog2> baseCatalog2ByC1id = catalogService.getBaseCatalog2ByC1id(allBaseCatalog1.get(0).getId());
		log.info("{} 的二级分类信息：{}",allBaseCatalog1.get(0),baseCatalog2ByC1id);

		List<BaseCatalog3> baseCatalog3ByC2id = catalogService.getBaseCatalog3ByC2id(baseCatalog2ByC1id.get(0).getId());
		log.info("{} 的三级分类信息：{}",baseCatalog2ByC1id.get(0),baseCatalog3ByC2id);
	}


	@Test
	public void testMapper(){
		BaseCatalog1 baseCatalog1 = new BaseCatalog1();
		baseCatalog1.setName("呵呵");
		mapper.insert(baseCatalog1);


		log.info("成功....，id是{},name是{}",baseCatalog1.getId(),baseCatalog1.getName());
	}


	@Test
	public void testLogicDelete(){
		userMapper.deleteById(2L);
		System.out.println("删除完成...");
		//以后的所有查询默认都是查未删除的
		for (User user : userMapper.selectList(null)) {
			System.out.println(user);
		}
		;
	}

	@Test
	public void contextLoads() {

		for (User user : userMapper.selectList(null)) {
			System.out.println(user);
		}
		;


		System.out.println("========");


		//要让xml生效一定加上mybatis-plus.mapper-locations=classpath:mapper/*.xml
		User user = new User();
		user.setName("Jack");
		user.setAge(20);
		User userByHaha = userMapper.getUserByHaha(user);
		System.out.println(userByHaha);




	}

}
