package com.github.redission;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
}
