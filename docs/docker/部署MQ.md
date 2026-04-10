# Docker 部署 RocketMQ 完整教程

---

## 一、前置准备

### 1.1 环境要求

```bash
# 检查 Docker 版本（要求 20.10+）
docker --version

# 检查 Docker Compose 版本（要求 2.0+）
docker compose version

# 检查可用内存（建议 2GB+）
free -h
```

### 1.2 目录规划

```bash
# 创建 RocketMQ 工作目录
mkdir -p /data/rocketmq/{namesrv/logs,namesrv/store,broker/logs,broker/store,broker/conf,dashboard}
cd /data/rocketmq
```

最终目录结构如下：

```
/data/rocketmq/
├── docker-compose.yml              # 编排文件
├── namesrv/
│   ├── logs/                       # NameServer 日志
│   └── store/                      # NameServer 数据
├── broker/
│   ├── logs/                       # Broker 日志
│   ├── store/                      # Broker 数据
│   └── conf/
│       └── broker.conf             # Broker 配置文件
└── dashboard/                      # Dashboard 数据（可选）
```

---

## 二、配置文件

### 2.1 Broker 配置文件

```bash
cat > /data/rocketmq/broker/conf/broker.conf << 'EOF'
# ============================================
# RocketMQ Broker 核心配置
# ============================================

# ---- 基本信息 ----
# broker 名称，集群中相同名称的 broker 归属同一组
brokerName = rocketmq-broker

# brokerId，master 设为 0，slave 设为大于 0 的值
brokerId = 0

# 角色标识：异步同步刷盘（ASYNC_MASTER / SYNC_MASTER / SLAVE）
brokerRole = ASYNC_MASTER

# 刷盘方式：异步刷盘（性能更高）/ 同步刷盘（数据更安全）
flushDiskType = ASYNC_FLUSH

# ---- 网络配置 ----
# broker 对外服务的监听端口
listenPort = 10911

# nameserver 地址（多个用分号分隔）
# docker-compose 中使用容器名，外部用宿主机IP
namesrvAddr = namesrv:9876

# broker 宿主机IP地址（必须配置！否则客户端连不上）
# ⚠️ 替换为你服务器的真实 IP，不能用 127.0.0.1
brokerIP1 = 192.168.1.100

# ---- 消息存储配置 ----
# 存储根目录（对应容器内路径）
storePathRootDir = /home/rocketmq/store

# commitlog 存储路径
storePathCommitLog = /home/rocketmq/store/commitlog

# 消费队列存储路径
storePathConsumeQueue = /home/rocketmq/store/consumequeue

# 消息索引存储路径
storePathIndex = /home/rocketmq/store/index

# checkpoint 文件路径
storeCheckpoint = /home/rocketmq/store/checkpoint

# abort 文件路径
abortFile = /home/rocketmq/store/abort

# ---- 性能与容量 ----
# commitlog 文件大小（默认1GB，建议保持默认）
mapedFileSizeCommitLog = 1073741824

# consumequeue 每个文件大小
mapedFileSizeConsumeQueue = 300000

# 是否开启根据 slave 刷盘偏移量检测消息丢失
checkCRC8WhenRecover = true

# ---- 消息限制 ----
# 单条消息最大字节数（4MB）
maxMessageSize = 4194304

# ---- 心跳检测 ----
# broker 与 nameserver 心跳间隔（秒）
heartbeatBrokerInterval = 30000

# broker 多久不发送心跳后被 nameserver 移除（毫秒）
notActiveBrokerMillis = 120000

# ---- 日志配置 ----
# 日志根目录（对应容器内路径）
rocketmqHome = /home/rocketmq
# 日志路径由 MQ 启动脚本自动拼接
EOF

# 替换为实际宿主机 IP
read -p "请输入宿主机真实 IP 地址（不能用127.0.0.1）: " HOST_IP
sed -i "s/brokerIP1 = 192.168.1.100/brokerIP1 = ${HOST_IP}/" /data/rocketmq/broker/conf/broker.conf

echo "✅ Broker 配置文件已生成"
```

### 2.2 Docker Compose 编排文件

```bash
cat > /data/rocketmq/docker-compose.yml << 'EOF'
version: '3.8'

# ============================================
# RocketMQ 完整部署：NameServer + Broker + Dashboard
# ============================================

services:
  # ==========================================
  # ① NameServer：路由注册中心（类似注册表）
  # ==========================================
  namesrv:
    image: apache/rocketmq:5.1.4
    container_name: rocketmq-namesrv
    hostname: namesrv
    restart: unless-stopped
    # NameServer 启动命令
    command: sh mqnamesrv
    ports:
      - "9876:9876"
    volumes:
      - ./namesrv/logs:/home/rocketmq/logs
      - ./namesrv/store:/home/rocketmq/store
    environment:
      JAVA_OPT_EXT: "-Xms512m -Xmx512m -Xmn256m"
    networks:
      - rocketmq-net
    healthcheck:
      test: ["CMD", "lsof", "-i", "9876"]
      interval: 10s
      timeout: 5s
      retries: 3

  # ==========================================
  # ② Broker：消息存储与转发（核心组件）
  # ==========================================
  broker:
    image: apache/rocketmq:5.1.4
    container_name: rocketmq-broker
    hostname: broker
    restart: unless-stopped
    # Broker 启动命令
    command: sh mqbroker -c /home/rocketmq/broker/conf/broker.conf
    ports:
      - "10909:10909"   # broker fast 拉取端口
      - "10911:10911"   # broker 主服务端口
      - "10912:10912"   # broker 高可用 HA 服务端口
    volumes:
      - ./broker/conf:/home/rocketmq/broker/conf
      - ./broker/logs:/home/rocketmq/logs
      - ./broker/store:/home/rocketmq/store
    environment:
      JAVA_OPT_EXT: "-Xms1g -Xmx1g -Xmn512m"
    depends_on:
      namesrv:
        condition: service_healthy
    networks:
      - rocketmq-net
    healthcheck:
      test: ["CMD", "lsof", "-i", "10911"]
      interval: 10s
      timeout: 5s
      retries: 3

  # ==========================================
  # ③ Dashboard：Web 管理控制台
  # ==========================================
  dashboard:
    image: apacherocketmq/rocketmq-dashboard:2.0.0
    container_name: rocketmq-dashboard
    hostname: dashboard
    restart: unless-stopped
    ports:
      - "9000:8080"
    environment:
      # RocketMQ NameServer 地址（容器间用服务名通信）
      JAVA_OPTS: "-Drocketmq.config.namesrvAddr=namesrv:9876"
    depends_on:
      namesrv:
        condition: service_healthy
    networks:
      - rocketmq-net

# ==========================================
# 共享网络
# ==========================================
networks:
  rocketmq-net:
    driver: bridge
EOF

echo "✅ Docker Compose 文件已生成"
```

---

## 三、启动服务

### 3.1 一键启动

```bash
cd /data/rocketmq

# 后台启动全部服务
docker compose up -d

# 查看启动进度
docker compose logs -f --tail=50
```

### 3.2 启动日志解读

```
# NameServer 启动成功日志
The Name Server boot success. serializeType=JSON

# Broker 启动成功日志
The broker[rocketmq-broker, 192.168.1.100:10911] boot success.
```

### 3.3 验证服务状态

```bash
# 查看容器运行状态
docker compose ps

# 预期输出：
# NAME                 STATUS              PORTS
# rocketmq-namesrv     Up (healthy)        0.0.0.0:9876->9876/tcp
# rocketmq-broker      Up (healthy)        0.0.0.0:10909-10912->10909-10912/tcp
# rocketmq-dashboard   Up                  0.0.0.0:9000->8080/tcp
```

---

## 四、验证部署

### 4.1 命令行验证

```bash
# ① 进入 Broker 容器
docker exec -it rocketmq-broker bash

# ② 创建测试 Topic
sh mqadmin updateTopic -n namesrv:9876 -t TEST_TOPIC -c DefaultCluster -r 4 -w 4

# ③ 查看所有 Topic 列表
sh mqadmin topicList -n namesrv:9876

# ④ 查看 Topic 详情
sh mqadmin topicStatus -n namesrv:9876 -t TEST_TOPIC

# ⑤ 测试发送消息（每秒1条，共100条）
sh mqadmin clusterList -n namesrv:9876

# ⑥ 退出容器
exit
```

### 4.2 Dashboard 控制台验证

```
浏览器访问：http://你的IP:9000

页面功能：
├── Cluster          → 查看集群状态、Broker信息
├── Topic            → 管理 Topic（创建、删除、查询）
├── Consumer         → 查看消费组状态、消费进度
├── Message          → 搜索消息（按 Topic、MessageId、Key）
├── Producer         → 查看生产者连接
└── Admin Ex Command → 高级管理命令
```

### 4.3 Java 客户端验证

```xml
<!-- pom.xml 依赖 -->
<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-client</artifactId>
    <version>5.1.4</version>
</dependency>
```

```java
// 测试类：验证MQ连通性
public class RocketMQTest {

    // ⚠️ 替换为你的服务器 IP（不是127.0.0.1）
    private static final String NAME_SERVER = "192.168.1.100:9876";

    public static void main(String[] args) throws Exception {
        // ---- 生产者测试 ----
        DefaultMQProducer producer = new DefaultMQProducer("TEST_PRODUCER_GROUP");
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();

        Message message = new Message("TEST_TOPIC", "Hello RocketMQ!".getBytes());
        SendResult result = producer.send(message);
        System.out.println("发送成功: " + result);

        producer.shutdown();

        // ---- 消费者测试 ----
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("TEST_CONSUMER_GROUP");
        consumer.setNamesrvAddr(NAME_SERVER);
        consumer.subscribe("TEST_TOPIC", "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                System.out.println("收到消息: " + new String(msg.getBody()));
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
        System.out.println("消费者启动成功，等待消息...");
    }
}
```

---

## 五、常用运维命令

### 5.1 服务管理

```bash
# 启动服务
docker compose up -d

# 停止服务（保留数据）
docker compose stop

# 停止并移除容器（保留数据卷）
docker compose down

# 停止并移除容器 + 数据卷（⚠️ 会删除数据）
docker compose down -v

# 重启指定服务
docker compose restart broker

# 查看实时日志
docker compose logs -f broker

# 查看最近100行日志
docker compose logs --tail=100 broker

# 查看资源占用
docker stats rocketmq-namesrv rocketmq-broker
```

### 5.2 管理命令（在 Broker 容器内执行）

```bash
# 进入 Broker 容器
docker exec -it rocketmq-broker bash

# ---- Topic 管理 ----
# 创建 Topic（4读4写队列）
sh mqadmin updateTopic -n namesrv:9876 -t MY_TOPIC -c DefaultCluster -r 4 -w 4

# 查看所有 Topic
sh mqadmin topicList -n namesrv:9876

# 查看 Topic 统计信息
sh mqadmin topicStatus -n namesrv:9876 -t MY_TOPIC

# 删除 Topic
sh mqadmin deleteTopic -n namesrv:9876 -t MY_TOPIC -c DefaultCluster

# ---- Consumer 管理 ----
# 查看消费组列表
sh mqadmin consumerProgress -n namesrv:9876

# 查看指定消费组详情
sh mqadmin consumerProgress -n namesrv:9876 -g CG_MY_GROUP

# 重置消费偏移量到指定时间（从2024-01-01 00:00:00开始消费）
sh mqadmin resetOffsetByTime -n namesrv:9876 -g CG_MY_GROUP -t MY_TOPIC -s '2024-01-01#00:00:00'

# 重置消费偏移量到最早（重新消费所有消息）
sh mqadmin resetOffsetByTime -n namesrv:9876 -g CG_MY_GROUP -t MY_TOPIC -s earliest

# 重置消费偏移量到最新（跳过所有消息）
sh mqadmin resetOffsetByTime -n namesrv:9876 -g CG_MY_GROUP -t MY_TOPIC -s latest

# ---- 消息查询 ----
# 根据 MessageId 查询消息
sh mqadmin queryMsgById -i "C0A8016400002A9F0000000000000001" -n namesrv:9876

# 根据 Topic + Key 查询消息
sh mqadmin queryMsgByKey -t MY_TOPIC -k "order-12345" -n namesrv:9876

# ---- 集群信息 ----
# 查看集群状态
sh mqadmin clusterList -n namesrv:9876

# 查看 Broker 状态
sh mqadmin brokerStatus -n namesrv:9876 -b 192.168.1.100:10911
```

---

## 六、生产环境优化

### 6.1 JVM 参数调优

```yaml
# docker-compose.yml 中调整（按实际内存调整）
services:
  namesrv:
    environment:
      JAVA_OPT_EXT: "-Xms1g -Xmx1g -Xmn512m -XX:+UseG1GC"
      
  broker:
    environment:
      JAVA_OPT_EXT: "-Xms4g -Xmx4g -Xmn2g -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
```

**参数说明：**

| 参数 | 说明 | 建议值（4核8G机器） |
|------|------|---------------------|
| `-Xms` | 初始堆内存 | Broker: 4g |
| `-Xmx` | 最大堆内存 | Broker: 4g |
| `-Xmn` | 新生代大小 | 总内存的 1/2 |
| `-XX:+UseG1GC` | 使用 G1 垃圾回收器 | 推荐开启 |

### 6.2 Broker 性能调优

```bash
cat > /data/rocketmq/broker/conf/broker.conf << 'EOF'
# ============================================
# 生产环境 Broker 调优配置
# ============================================

# ---- 基本信息 ----
brokerName = rocketmq-broker
brokerId = 0
brokerRole = ASYNC_MASTER
flushDiskType = ASYNC_FLUSH
namesrvAddr = namesrv:9876

# ⚠️ 必须设置为宿主机真实IP
brokerIP1 = 192.168.1.100

# ---- 网络与IO ----
listenPort = 10911

# 发送线程池大小
sendMessageThreadPoolNums = 16

# 拉取消息线程池大小
pullMessageThreadPoolNums = 32

# admin命令线程池
adminBrokerThreadPoolNums = 16

# 客户端管理线程池
clientManageThreadPoolNums = 32

# ---- 消息存储 ----
storePathRootDir = /home/rocketmq/store
storePathCommitLog = /home/rocketmq/store/commitlog
mapedFileSizeCommitLog = 1073741824

# commitlog 刷盘间隔（毫秒），异步模式下生效
commitIntervalCommitLog = 500

# consumequeue 刷盘间隔（毫秒）
flushConsumeQueueLeastPages = 4
flushCommitLogLeastPages = 4

# ---- 消费消息 ----
# 消费拉取长轮询超时（毫秒）
longPollingEnable = true
shortPollingTimeMills = 1000

# 消息索引相关
maxHashSlotNum = 5000000
maxIndexNum = 5000000 * 4

# ---- 限流保护 ----
# 启用消息过期删除
deleteWhen = "04"
fileReservedTime = 72

# 单条消息最大大小（4MB）
maxMessageSize = 4194304

# ---- HA 高可用（集群模式启用）----
# haListenPort = 10912
# haMasterAddress =
# haSendHeartbeatInterval = 1000
# haHousekeepingInterval = 30000

EOF
```

### 6.3 Docker 资源限制

```yaml
# docker-compose.yml 中添加资源限制
services:
  namesrv:
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 1G
        reservations:
          memory: 512M

  broker:
    deploy:
      resources:
        limits:
          cpus: '4'
          memory: 8G
        reservations:
          cpus: '2'
          memory: 4G
```

### 6.4 持久化与备份

```bash
# 创建定时备份脚本
cat > /data/rocketmq/backup.sh << 'SCRIPT'
#!/bin/bash
# RocketMQ 数据备份脚本

BACKUP_DIR="/data/rocketmq-backup/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

# 停止 Broker（确保数据一致）
docker compose stop broker

# 备份 store 目录
tar czf "$BACKUP_DIR/broker-store.tar.gz" -C /data/rocketmq/broker store/

# 启动 Broker
docker compose start broker

# 清理7天前的备份
find /data/rocketmq-backup -type d -mtime +7 -exec rm -rf {} + 2>/dev/null

echo "备份完成: $BACKUP_DIR"
SCRIPT

chmod +x /data/rocketmq/backup.sh

# 添加 crontab 定时备份（每天凌晨4点）
# crontab -e
# 0 4 * * * /data/rocketmq/backup.sh >> /var/log/rocketmq-backup.log 2>&1
```

---

## 七、故障排查

### 7.1 常见问题速查表

```
┌────────────────────────────────────┬───────────────────────────────────────────┐
│           问题                      │                  解决方案                 │
├────────────────────────────────────┼───────────────────────────────────────────┤
│ 客户端连接不上 Broker              │ 检查 broker.conf 中 brokerIP1 是否为     │
│                                    │ 宿主机真实IP（不能用127.0.0.1）           │
├────────────────────────────────────┼───────────────────────────────────────────┤
│ Broker 启动后立即退出              │ 查看日志: docker compose logs broker      │
│                                    │ 常见原因：内存不足、配置语法错误          │
├────────────────────────────────────┼───────────────────────────────────────────┤
│ Broker 显示 "boot success" 但      │ brokerIP1 配置错误                        │
│ 客户端仍连不上                      │ 重新配置 brokerIP1 后重启                │
├────────────────────────────────────┼───────────────────────────────────────────┤
│ 端口被占用导致启动失败             │ 查看占用: lsof -i:10911                   │
│                                    │ 修改 listenPort 或释放占用端口            │
├────────────────────────────────────┼───────────────────────────────────────────┤
│ Dashboard 连不上 NameServer        │ 确认 Dashboard 环境变量中                  │
│                                    │ rocketmq.config.namesrvAddr 为容器名     │
├────────────────────────────────────┼───────────────────────────────────────────┤
│ 消费堆积严重                        │ 增加消费者实例 / 增加队列数               │
│                                    │ 检查消费者处理逻辑是否有阻塞              │
├────────────────────────────────────┼───────────────────────────────────────────┤
│ 磁盘空间不足导致消息写入失败       │ 清理历史消息 / 扩容磁盘                   │
│                                    │ 调小 fileReservedTime（消息保留时间）      │
├────────────────────────────────────┼───────────────────────────────────────────┤
│ OOM (内存溢出)                      │ 增大 -Xmx 值 / 增加服务器内存            │
└────────────────────────────────────┴───────────────────────────────────────────┘
```

### 7.2 日志查看

```bash
# 查看 NameServer 日志
docker compose logs namesrv | grep -i error

# 查看 Broker 日志
docker compose logs broker | grep -i error

# 查看 Broker 实时启动日志
docker compose logs -f broker

# 进入容器查看详细日志文件
docker exec -it rocketmq-broker bash
cat /home/rocketmq/logs/broker.log
cat /home/rocketmq/logs/rocketmqlogs/broker.log
```

### 7.3 网络连通性检查

```bash
# ① 检查端口是否开放
# 在客户端机器执行
telnet 192.168.1.100 9876   # NameServer
telnet 192.168.1.100 10911  # Broker

# ② 检查防火墙
# CentOS/RHEL
firewall-cmd --list-ports
firewall-cmd --add-port=9876/tcp --permanent
firewall-cmd --add-port=10911/tcp --permanent
firewall-cmd --reload

# Ubuntu/Debian
ufw status
ufw allow 9876/tcp
ufw allow 10911/tcp

# ③ 检查云服务器安全组
# 阿里云 / 腾讯云 / AWS 控制台 → 安全组 → 放行以下端口：
# 9876, 10909, 10911, 10912, 9000
```

### 7.4 内存不足处理

```bash
# 查看当前内存
free -h

# 如果内存紧张，减少 JVM 分配
# 编辑 docker-compose.yml，修改环境变量：
# JAVA_OPT_EXT: "-Xms256m -Xmx256m -Xmn128m"

# 重启服务
docker compose down
docker compose up -d
```

---

## 八、完整操作脚本

保存以下脚本为一键部署脚本：

```bash
cat > /data/rocketmq/deploy.sh << 'SCRIPT'
#!/bin/bash
set -e

echo "============================================"
echo "  RocketMQ Docker 一键部署脚本"
echo "============================================"

# 1. 获取宿主机IP
if [ -z "$HOST_IP" ]; then
    read -p "请输入宿主机真实IP地址（客户端连接用）: " HOST_IP
fi

# 2. 创建目录
echo "[1/5] 创建目录..."
mkdir -p /data/rocketmq/{namesrv/logs,namesrv/store,broker/logs,broker/store,broker/conf,dashboard}

# 3. 生成 Broker 配置
echo "[2/5] 生成 Broker 配置..."
cat > /data/rocketmq/broker/conf/broker.conf << EOF
brokerName = rocketmq-broker
brokerId = 0
brokerRole = ASYNC_MASTER
flushDiskType = ASYNC_FLUSH
namesrvAddr = namesrv:9876
brokerIP1 = ${HOST_IP}
listenPort = 10911
storePathRootDir = /home/rocketmq/store
storePathCommitLog = /home/rocketmq/store/commitlog
mapedFileSizeCommitLog = 1073741824
maxMessageSize = 4194304
deleteWhen = "04"
fileReservedTime = 72
EOF

# 4. 生成 docker-compose.yml
echo "[3/5] 生成编排文件..."
cat > /data/rocketmq/docker-compose.yml << 'EOF'
version: '3.8'

services:
  namesrv:
    image: apache/rocketmq:5.1.4
    container_name: rocketmq-namesrv
    restart: unless-stopped
    command: sh mqnamesrv
    ports:
      - "9876:9876"
    volumes:
      - ./namesrv/logs:/home/rocketmq/logs
      - ./namesrv/store:/home/rocketmq/store
    environment:
      JAVA_OPT_EXT: "-Xms512m -Xmx512m -Xmn256m"
    healthcheck:
      test: ["CMD", "lsof", "-i", "9876"]
      interval: 10s
      timeout: 5s
      retries: 3

  broker:
    image: apache/rocketmq:5.1.4
    container_name: rocketmq-broker
    restart: unless-stopped
    command: sh mqbroker -c /home/rocketmq/broker/conf/broker.conf
    ports:
      - "10909:10909"
      - "10911:10911"
      - "10912:10912"
    volumes:
      - ./broker/conf:/home/rocketmq/broker/conf
      - ./broker/logs:/home/rocketmq/logs
      - ./broker/store:/home/rocketmq/store
    environment:
      JAVA_OPT_EXT: "-Xms1g -Xmx1g -Xmn512m"
    depends_on:
      namesrv:
        condition: service_healthy

  dashboard:
    image: apacherocketmq/rocketmq-dashboard:2.0.0
    container_name: rocketmq-dashboard
    restart: unless-stopped
    ports:
      - "9000:8080"
    environment:
      JAVA_OPTS: "-Drocketmq.config.namesrvAddr=namesrv:9876"
    depends_on:
      namesrv:
        condition: service_healthy
EOF

# 5. 启动服务
echo "[4/5] 启动服务..."
cd /data/rocketmq
docker compose up -d

# 6. 等待并验证
echo "[5/5] 等待服务启动..."
sleep 10

echo ""
echo "============================================"
echo "  部署完成！服务状态："
echo "============================================"
docker compose ps

echo ""
echo "  访问地址："
echo "  NameServer:  ${HOST_IP}:9876"
echo "  Broker:      ${HOST_IP}:10911"
echo "  Dashboard:   http://${HOST_IP}:9000"
echo "============================================"
SCRIPT

chmod +x /data/rocketmq/deploy.sh

echo "✅ 部署脚本已生成"
echo ""
echo "执行部署：cd /data/rocketmq && ./deploy.sh"
```

---

## 九、部署流程总结

```
┌──────────────────────────────────────────────────────────────────────────┐
│                     RocketMQ Docker 部署流程                              │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   Step 1: 准备环境                                                        │
│   ├── 确认 Docker 版本 20.10+                                            │
│   ├── 确认 Docker Compose 版本 2.0+                                     │
│   └── 确认可用内存 2GB+                                                  │
│                                                                          │
│   Step 2: 创建目录                                                        │
│   ├── /data/rocketmq/namesrv/                                           │
│   ├── /data/rocketmq/broker/conf/                                       │
│   └── /data/rocketmq/dashboard/                                         │
│                                                                          │
│   Step 3: 生成配置文件                                                    │
│   ├── broker.conf（核心！必须配置 brokerIP1）                             │
│   └── docker-compose.yml                                                │
│                                                                          │
│   Step 4: 启动服务                                                        │
│   └── docker compose up -d                                              │
│                                                                          │
│   Step 5: 验证部署                                                        │
│   ├── docker compose ps（查看状态）                                       │
│   ├── 访问 Dashboard（http://IP:9000）                                   │
│   └── Java 客户端测试连接                                                 │
│                                                                          │
│   ⚠️ 关键注意点：                                                         │
│   • brokerIP1 必须为宿主机真实 IP                                        │
│   • 云服务器需在安全组放行端口                                            │
│   • 生产环境需配置持久化目录                                              │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```