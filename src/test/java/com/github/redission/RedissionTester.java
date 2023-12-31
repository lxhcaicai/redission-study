package com.github.redission;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.api.listener.MessageListener;
import org.redisson.client.codec.Codec;
import org.redisson.codec.SerializationCodec;
import org.redisson.config.Config;
import org.slf4j.LoggerFactory;

import java.io.*;
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
        RMap<Object, Object> map = redissonClient.getMap("map");
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

    @Test
    public void rateLimiter() throws InterruptedException {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("123456");
        RedissonClient redissonClient = Redisson.create(config);

        //====================操作rateLimiter====================
        RRateLimiter rateLimiter = redissonClient.getRateLimiter("rateLimiter");
        //创建限流器，最大流速:每1秒钟产生20个令牌
        rateLimiter.trySetRate(RateType.OVERALL, 20, 1, RateIntervalUnit.SECONDS);
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                int i = 0;

                @Override
                public void run() {
                    while (true) {
                        rateLimiter.acquire(1);
                        System.out.println(Thread.currentThread() + "-" + System.currentTimeMillis() + "-" + i++);
                    }
                }
            }).start();
        }
        //等待执行完成,不设置等待可能出现还未执行完成客户端就关闭的情况
        Thread.sleep(5000);
        // 关闭客户端
        redissonClient.shutdown();
    }

    @Test
    public void lock() throws InterruptedException {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("123456");
        RedissonClient redissonClient = Redisson.create(config);

        //====================操作rateLimiter====================
        RLock lock = redissonClient.getLock("lock");
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                lock.lock();
                try {
                    System.out.println(Thread.currentThread() + "-" + System.currentTimeMillis() + "-" + "获取锁");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }).start();
        }
        //等待执行完成,不设置等待可能出现还未执行完成客户端就关闭的情况
        Thread.sleep(5000);
        // 关闭客户端
        redissonClient.shutdown();
    }

    @Test
    public void bloomFilter() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("123456");
        RedissonClient redissonClient = Redisson.create(config);

        //====================操作布隆过滤器====================
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("bloom-filter");
        // 初始化布隆过滤器，初始化预期插入的数据量为200，期望误差率为0.01
        bloomFilter.tryInit(200, 0.01);
        // 插入数据
        bloomFilter.add("lxhcaicai-1");
        bloomFilter.add("lxhcaicai-2");
        bloomFilter.add("lxhcaicai-3");

        //判断是否包含
        boolean victory = bloomFilter.contains("lxhcaicai-1");
        boolean forward = bloomFilter.contains("lxhcaicai-4");
        System.out.println(victory);
        System.out.println(forward);

        // 关闭客户端
        redissonClient.shutdown();
    }

    class Message{
        Long id;
        String name;
        Integer age;

        public Message(Long id, String name, Integer age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }
    }
    /**
     *  发布订阅操作
     *  Redisson操作RTopic执行发布订阅操作
     **/
    @Test
    public void topicPublisherAndSubscriber() throws InterruptedException {
        //====================创建Redisson客户端====================
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("123456");
        //config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        RedissonClient redissonClient = Redisson.create(config);
        RedissonClient redissonClient1 = Redisson.create(config);

        //====================操作topic执行发布操作====================
        RTopic topic1 = redissonClient.getTopic("topic",new SerializationCodec());
        topic1.publish(new Message(1L,"victory",18));

        //====================操作topic执行订阅操作====================
        Thread.sleep(5000);
        RTopic topic = redissonClient1.getTopic("topic", new SerializationCodec());
        topic.addListener(Message.class, new MessageListener<Message>() {
            @Override
            public void onMessage(CharSequence channel, Message msg) {
                System.out.println("onMessage:=========" + channel + "; Thread:========= " + Thread.currentThread().toString());
                System.out.println(" name : " + msg.getName() + " age : " + msg.getAge());
                LoggerFactory.getLogger(RedissionTester.class).info("Redisson接收到消息",msg);
               // System.out.println("Redisson接收到消息:" + msg);
            }
        });

        //====================关闭客户端====================
        redissonClient.shutdown();
        redissonClient1.shutdown();
    }
}

