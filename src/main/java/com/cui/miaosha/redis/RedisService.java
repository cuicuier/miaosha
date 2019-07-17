package com.cui.miaosha.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RedisService {
    @Autowired
    JedisPool jedisPool;

    public <T> T get(KeyPrefix prefix,String key, Class<T> clazz) {

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey=prefix.getPrefix()+key;
            String str = jedis.get(realKey);
            T t = StringToBean(str,clazz);
            return t;
        }finally {
            returnToPool(jedis);
         }
    }

    public <T> boolean set(KeyPrefix prefix,String key, T value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            String realKey=prefix.getPrefix()+key;
            int seconds = prefix.expireSeconds();
            if (seconds <= 0) {//永不过期
                jedis.set(realKey, str);
            } else {
                jedis.setex(realKey, seconds, str); //设置redis中key的年龄
            }
            return true;
        }finally {
            returnToPool(jedis);
        }
    }

    public static  <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return ""+value;
        } else if (clazz==String.class) {
            return (String) value;
        } else if (clazz == long.class || clazz == Long.class) {
            return ""+value;
        } else {
            return JSON.toJSONString(value);
        }

    }

    public static  <T> T StringToBean(String str,Class<T> clazz) {
        if (str == null || str.length()<=0 || clazz==null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz==String.class) {
            return (T) str;
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else {
            return JSON.toJavaObject(JSON.parseObject(str),clazz);
        }
    }

    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    /**
     * 判断一个key是否存在
     */
    public <T> boolean exists(KeyPrefix prefix, String key) {
        Jedis jedis=null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.exists(realKey);

        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * value加1
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long incr(KeyPrefix prefix, String key) {
        Jedis jedis=null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.incr(realKey);

        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * value减1
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long decr(KeyPrefix prefix, String key) {
        Jedis jedis=null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.decr(realKey);

        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 删除
     * @param prefix
     * @param key
     * @return
     */
    public boolean delete(KeyPrefix prefix, String key) {
        Jedis jedis=null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            long res = jedis.del(realKey);
            return res > 0;

        }finally {
            returnToPool(jedis);
        }
    }



}
