package com.yuexiang.voucher.support;

import org.springframework.stereotype.Component;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.Enumeration;

/**
 * 雪花算法 ID 生成器
 * <p>
 * ID 结构：
 * 1. 时间戳差值
 * 2. 数据中心 ID
 * 3. 机器 ID
 * 4. 毫秒内自增序列
 * <p>
 * 总共 64 bit，其中最高位为符号位，固定为 0。
 */
@Component
public class SnowflakeIdGenerator {

    /**
     * 自定义起始时间戳
     * 用于缩短生成 ID 的长度
     */
    private static final long EPOCH = 1700000000000L;

    /**
     * 机器 ID 占用位数
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 数据中心 ID 占用位数
     */
    private static final long DATACENTER_ID_BITS = 5L;

    /**
     * 序列号占用位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 机器 ID 最大值：31
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 数据中心 ID 最大值：31
     */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /**
     * 序列号最大值：4095
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /**
     * 机器 ID 左移位数
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据中心 ID 左移位数
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间戳左移位数
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    /**
     * 随机数生成器
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 数据中心 ID
     */
    private final long datacenterId;

    /**
     * 机器 ID
     */
    private final long workerId;

    /**
     * 毫秒内自增序列
     */
    private long sequence = 0L;

    /**
     * 上次生成 ID 的时间戳
     */
    private long lastTimestamp = -1L;

    /**
     * 默认构造方法
     * <p>
     * 自动根据本机网络信息、进程信息生成数据中心 ID 和机器 ID
     */
    public SnowflakeIdGenerator() {
        this.datacenterId = getDatacenterId();
        this.workerId = getMaxWorkerId(datacenterId);
    }

    /**
     * 指定 workerId 和 datacenterId 的构造方法
     *
     * @param workerId 机器 ID
     * @param datacenterId 数据中心 ID
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    "workerId can't be greater than " + MAX_WORKER_ID + " or less than 0"
            );
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(
                    "datacenterId can't be greater than " + MAX_DATACENTER_ID + " or less than 0"
            );
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 生成下一个唯一 ID
     * <p>
     * 为保证线程安全，这里使用 synchronized
     *
     * @return 全局唯一 long 型 ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 时钟回拨保护
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    "Clock moved backwards. Refusing to generate id for "
                            + (lastTimestamp - timestamp) + " milliseconds"
            );
        }

        // 同一毫秒内，序列号递增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;

            // 当前毫秒内序列号溢出，等待下一毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 拼接最终 ID
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 阻塞等待直到下一毫秒
     *
     * @param lastTimestamp 上一次生成 ID 的时间戳
     * @return 新的时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前系统时间戳
     *
     * @return 毫秒时间戳
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 根据数据中心 ID、网络接口信息、进程 ID 计算机器 ID
     *
     * @param datacenterId 数据中心 ID
     * @return workerId
     */
    protected long getMaxWorkerId(long datacenterId) {
        StringBuilder builder = new StringBuilder();
        builder.append(datacenterId);

        // 拼接网络接口名
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces.hasMoreElements()) {
                String name = interfaces.nextElement().getName();
                builder.append(name);
            }
        } catch (Exception e) {
            builder.append(SECURE_RANDOM.nextInt());
        }

        // 拼接进程 ID
        try {
            builder.append(ProcessHandle.current().pid());
        } catch (Exception e) {
            builder.append(SECURE_RANDOM.nextInt());
        }

        return (builder.toString().hashCode() & 0xffff) % (SnowflakeIdGenerator.MAX_WORKER_ID + 1);
    }

    /**
     * 根据网络接口信息计算数据中心 ID
     *
     * @return datacenterId
     */
    protected long getDatacenterId() {
        long id = 0L;
        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            int count = 0;
            while (networks.hasMoreElements() && count < 2) {
                NetworkInterface networkInterface = networks.nextElement();
                id = (id << 8) | (networkInterface.getName().hashCode() & 0xff);
                count++;
            }
        } catch (Exception e) {
            id = SECURE_RANDOM.nextInt();
        }
        return id % (SnowflakeIdGenerator.MAX_DATACENTER_ID + 1);
    }
}