package com.github.redission;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RedissionTester {
    @Test
    public void RedissonBucket() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("123456");
        RedissonClient redissonClient = Redisson.create(config);

        //====================操作对象桶来存储对象(同步)====================

        RBucket<Object> bucket = redissonClient.getBucket("name");
        //设置值为victory，过期时间为3小时
        bucket.set("victory", 3, TimeUnit.HOURS);
        Object value = bucket.get();
        System.out.println(value);

        // 通过key取value值
        Object name = redissonClient.getBucket("name").get();
        System.out.println(name);

        // 关闭客户端
        redissonClient.shutdown();
    }

    @Test
    public void RedissonStream() throws IOException {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("123456");
        RedissonClient redissonClient = Redisson.create(config);

        //====================操作流来存储对象====================
        RBinaryStream stream = redissonClient.getBinaryStream("stream");
        stream.set("name is".getBytes());
        OutputStream outputStream = stream.getOutputStream();

        outputStream.write("victory".getBytes());
        InputStream inputStream = stream.getInputStream();

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int length;
        while ((length = inputStream.read(bytes)) != -1) {
            result.write(bytes, 0, length);
        }
        System.out.println(result.toString());
        // 关闭客户端
        redissonClient.shutdown();

    }

    @Test
    public void list() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("123456");
        RedissonClient redissonClient = Redisson.create(config);

        //====================操作list====================
        RList<String> list = redissonClient.getList("list");
        list.add("victory1");
        list.add("victory2");
        System.out.println(list);

        // 取值
        List<Object> list1 = redissonClient.getList("list").get();
        System.out.println(list1);
        //移除索引0位置元素
        list.remove(0);
        System.out.println(list);
        // 通过key取value值
        List<Object> list2 = redissonClient.getList("list").get();
        System.out.println(list2);
        // 关闭客户端
        redissonClient.shutdown();
    }

    @Test
    public void set() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("123456");
        RedissonClient redissonClient = Redisson.create(config);

        //====================操作Set====================
        RSet<String> set = redissonClient.getSet("set");
        set.add("victory1");
        set.add("victory2");
        System.out.println(set);

        //通过key取value值
        RSet<Object> set1 = redissonClient.getSet("set");
        System.out.println(set1);
        // 关闭客户端
        redissonClient.shutdown();
    }

    @Test
    public void map() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("123456");
        RedissonClient redissonClient = Redisson.create(config);

        //====================操作map====================
        RMap<Object,Object> map = redissonClient.getMap("map");
        map.put("name1", "victory1");
        map.put("name2", "victory2");
        map.forEach((key, value) -> {
            System.out.println("key = " + key + ", value = " + value);
        });
        //通过key取value值
        Object o = redissonClient.getMap("map").get("name1");
        System.out.println(o);
        // 关闭客户端
        redissonClient.shutdown();
    }

    @Test
    public void queue() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("123456");
        RedissonClient redissonClient = Redisson.create(config);

        //====================操作queue====================
        RQueue<String> queue = redissonClient.getQueue("queue");
        // 存储
        queue.add("victory1");
        queue.add("victory2");
        // 取值
        String item = queue.poll();
        System.out.println(item);

        RQueue<Object> queue1 = redissonClient.getQueue("queue");
        System.out.println(queue1);
        // 关闭客户端
        redissonClient.shutdown();
    }
}
