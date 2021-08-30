<p align='center'>
<a href="https://github.com/wpwbb510582246/rpc-framework/blob/main/LICENSE"><img alt="GitHub" src="https://img.shields.io/github/license/wpwbb510582246/rpc-framework?label=License"></a>
<img src="https://img.shields.io/badge/build-passing-brightgreen.svg">
<img src="https://img.shields.io/badge/platform-%20iOS | Android | Mac | Web%20-ff69b4.svg">
<img src="https://img.shields.io/badge/language-Java-orange.svg">
<img src="https://img.shields.io/badge/made%20with-=1-blue.svg">
<a href="https://github.com/wpwbb510582246/rpc-framework/pulls"><img src="https://img.shields.io/badge/PR-Welcome-brightgreen.svg"></a>
<img src="https://img.shields.io/github/stars/wpwbb510582246/rpc-framework?style=social">
<img src="https://img.shields.io/github/forks/wpwbb510582246/rpc-framework?style=social">
<a href="https://github.com/wpwbb510582246/rpc-framework"><img src="https://visitor-badge.laobi.icu/badge?page_id=wpwbb510582246.ComputerCookbook-SchoolRecruitment"></a>
<a href="https://github.com/wpwbb510582246/rpc-framework/releases"><img src="https://img.shields.io/github/v/release/wpwbb510582246/rpc-framework"></a>
<a href="https://github.com/wpwbb510582246/rpc-framework"><img src="https://img.shields.io/github/repo-size/wpwbb510582246/rpc-framework"></a>
</p>

<p align='center'>
<a href="https://www.grayson.top"><img src="https://img.shields.io/badge/Blog-Grayson-80d4f9.svg?style=flat"></a>
<a href="https://unsplash.com/@graysonwp"><img src="https://img.shields.io/badge/Unsplash-Grayson-success.svg"></a>
 <a href="https://www.zhihu.com/people/wei-peng-36-39"><img src="https://img.shields.io/badge/%E7%9F%A5%E4%B9%8E-@Grayson-fd6f32.svg?style=flat&colorA=0083ea"></a>
</p>

## 1 项目介绍

一个简单的 RPC 框架，主要包括服务注册与发现、网络传输、动态代理、负载均衡等几个核心模块，项目使用 Netty 进行网络传输，设计了一套客户端与服务端的通信协议，采用 Zookeeper 作为注册中心，基于 Spring 注解进行服务的注册与消费，支持 Kyro 和 Protostuff 两种序列化方式，采用 Gzip 对数据进行解压缩，采用 SPI 机制对程序进行解耦，初步实现了 RPC 框架的功能，具有良好的扩展性。

## 2 项目设计

### 2.1 框架设计

![image.png](https://notebook.grayson.top/media/202108/2021-08-18_1005570.4841046817647092.png)

#### 2.1.1 注册中心

1. 注册中心**负责服务地址的注册于查找**，**相当于目录服务**，**服务端启动的时候将服务名称及其对应的地址**（IP + Port）**注册到注册中心**，**服务消费端根据服务名称找到对应的服务地址**，**有了服务地址之后**，**服务消费端就可以通过网络请求服务端了**。
2. 比较推荐用 Zookeeper 作为注册中心（当然也可以使用 Nacos，甚至是 Redis）：
  
   1. Zookeeper 为我们提供了**高可用**、**高性能**、**稳定的分布式数据一致性解决方案**，**通常被用于实现诸如数据发布/订阅**、**负载均衡**、**命名服务**、**分布式协调/通知**、**集群管理**、**Master 选举**、**分布式锁和分布式队列等功能**。
   2. 而且，Zookeeper**将数据保存在内存中**，**性能是非常棒的**，**在读多于写的应用程序中尤其地高性能**，因为**写会导致所有的服务器间同步状态**（读多于写是协调服务的典型场景）。

#### 2.1.2 网络传输

1. 既然我们要**调用远程的方法**，就要**发送网络请求来传递目标类和方法的信息以及方法的参数等数据到服务提供端**。
2. 网络传输具体实现我们可以使用 Socket（Java 中**最原始**、**最基础的网络通信方式**，但是 Socket 是**阻塞 IO**、**性能低并且功能单一**），也可以使用**同步非阻塞的 I/O 模型[NIO](https://notebook.grayson.top/project-26/doc-335/#3-2-%E9%9D%9E%E9%98%BB%E5%A1%9E-IO)**（用来进行网络编程比较麻烦），也可以使用**基于 NIO 的网络模型框架[Netty](https://notebook.grayson.top/project-46/doc-839)**，他将是我们最好的选择。

#### 2.1.3 序列化与反序列化

1. **序列化是指将数据结构**（如 C++ 中的 struct 就是数据结构类型）**或对象**（如 Java 中实例化后的类）**转换成二进制字节流的过程**，**反序列化是指把二进制字节流转换成数据结构或者对象的过程**。
2. **要将数据进行网络传输就要涉及到序列化**，因为**网络传输的方式就是 IO**，而**我们的 IO 支持数据格式就是字节数组**，但是我们单方面**只把对象转成字节数组还不行**，因为**没有规则的字节数组我们没办法把对象的本来面目还原回来**，所以**我们必须在把对象转成字节数组的时候就制定一种规则**（序列化），那么**我们从 IO 流里面读出数据的时候再以这种规则把对象还原回来**（反序列化）。
3. 序列化的主要目的是**通过网络传输对象**或者说是**将对象存储到文件系统**、**数据库**、**内存中**，因此，实际开发中主要有以下场景会用到序列化和反序列化：
  
   1. 对象在**进行网络传输**（比如远程方法调用 RPC）**之前需要先被序列化**，**接收到序列化的对象之后需要再进行反序列化**。
   2. 将对象**存储到文件中的时候需要进行序列化**，**将对象从文件中读取出来需要进行反序列化**。
   3. 将对象**存储到缓存数据库**（如 Redis）**时需要用到序列化**，**将对象从缓存数据库中读取出来需要反序列化**。
4. 常用的序列化协议有**Kyro**、**Protobuf**、**ProtoStuff**、**Hession**（JDK 自带的序列化方式一般不用，因为序列化效率低并且部分版本有安全漏洞，JSON、XML 这种属于文本类序列化方式，虽然可读性较好，但是性能较差，一般也不会选择）：
  
   1. **JDK 自带的序列化方式**：
     
      1. JDK 自带的序列化，**只需实现 `java.io.Serializable` 接口即可**：
        
         ```java
         @AllArgsConstructor
         @NoArgsConstructor
         @Getter
         @Builder
         @ToString
         public class RpcRequest implements Serializable {
             private static final long serialVersionUID = 1905122041950251207L;
             private String requestId;
             private String interfaceName;
             private String methodName;
             private Object[] parameters;
             private Class<?>[] paramTypes;
             private RpcMessageTypeEnum rpcMessageTypeEnum;
         }
         ```
      2. **序列化号 `serialVersionUID` 属于版本控制的作用**，**序列化的时候也会被写入二进制序列中**，当**反序列化时会检查 `serialVersionUID` 是否和当前类的 `serialVersionUID` 一致**，**如果不一致**，则**会抛出 `InvalidClassException` 异常**，**强烈推荐每个序列化类都手动指定其 `serialVersionUID`**，**如果不手动指定**，那么**编译器会动态生成默认的序列化号**。
      3. 我们很少或者说几乎不会直接使用这个序列化方式，主要原因为：
        
         1. **不支持跨语言调用**：
            1. **如果调用的是其他语言开发的服务的时候就不支持了**。
         2. **性能差**：
            1. 相比于其他序列化框架**性能更低**，主要原因是**序列化之后的字节数组体积较大**，**导致传输成本加大**。
   2. **Kyro**：
     
      1. Kyro 是一个**高性能的序列化/反序列化工具**，由于其**变长存储**特性并**使用了字节码生成机制**，**拥有较高的运行速度和较小的字节码体积**。
      2. 另外，Kyro 是一种**非常成熟的序列化实现**了，已经在 Twitter、Groupon、Yahoo 以及多个著名开源项目（如 Hive、Storm）中广泛的使用。
      3. 具体的序列化和反序列化的代码如下：
        
         ```java
         /**
          * Kryo 序列化类，Kryo 序列化效率很高，但是只兼容 Java 语言
          *
          * @author shuang.kou
          * @createTime 2020 年 05 月 13 日 19:29:00
          */
         @Slf4j
         public class KryoSerializer implements Serializer {
         
             /**
              * 由于 Kryo 不是线程安全的。每个线程都应该有自己的 Kryo，Input 和 Output 实例。
              * 所以，使用 ThreadLocal 存放 Kryo 对象
              */
             private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
                 Kryo kryo = new Kryo();
                 kryo.register(RpcResponse.class);
                 kryo.register(RpcRequest.class);
                 kryo.setReferences(true); //默认值为 true,是否关闭注册行为,关闭之后可能存在序列化问题，一般推荐设置为 true
                 kryo.setRegistrationRequired(false); //默认值为 false,是否关闭循环引用，可以提高性能，但是一般不推荐设置为 true
                 return kryo;
             });
         
             @Override
             public byte[] serialize(Object obj) {
                 try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                      Output output = new Output(byteArrayOutputStream)) {
                     Kryo kryo = kryoThreadLocal.get();
                     // Object->byte:将对象序列化为 byte 数组
                     kryo.writeObject(output, obj);
                     kryoThreadLocal.remove();
                     return output.toBytes();
                 } catch (Exception e) {
                     throw new SerializeException("序列化失败");
                 }
             }
         
             @Override
             public <T> T deserialize(byte[] bytes, Class<T> clazz) {
                 try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                      Input input = new Input(byteArrayInputStream)) {
                     Kryo kryo = kryoThreadLocal.get();
                     // byte->Object:从 byte 数组中反序列化出对对象
                     Object o = kryo.readObject(input, clazz);
                     kryoThreadLocal.remove();
                     return clazz.cast(o);
                 } catch (Exception e) {
                     throw new SerializeException("反序列化失败");
                 }
             }
         
         }
         ```
   3. **Protobuf**：
     
      1. Protobuf 出自于 Google，**性能还比较优秀**，也**支持多种语言**，同时还是**跨平台**的。
      2. Protobuf**包含序列化格式的定义**、**各种语言的库以及一个 IDL 编译器**，正常情况下**我们需要定义 `proto` 文件**，然后**使用 IDL 编译器编译成我们需要的语言**。
      3. Protobuf**在使用中比较繁琐**，**需要我们自己定义 IDL 文件和生成对应的序列化代码**，这样**虽然不灵活**，但是，另一方面导致 Protobuf**没有序列化漏洞的风险**。
      4. 一个简单的 `proto` 文件如下：
        
         ```java
         // protobuf 的版本
         syntax = "proto3"; 
         // SearchRequest 会被编译成不同的编程语言的相应对象，比如 Java 中的 class、Go 中的 struct
         message Person {
           //string 类型字段
           string name = 1;
           // int 类型字段
           int32 age = 2;
         }
         ```
   4. **ProtoStuff**：
     
      1. ProtoStuff**基于 Google Protobuf**，但是**提供了更多的功能和更简易的用法**，虽然更加易用，但是并不代表 ProtoStuff 性能更差。
   5. **Hession**：
     
      1. Hession 是一个**轻量级**的、**自定义描述**的二进制 RPC 协议，是一个**比较老的序列化的实现**，同样也是**跨语言**的。
      2. Dubbo RPC**默认启用的序列化方式是 Hession2**，但是，Dubbo 对 Hession2 进行了修改，不过大体结构还是差不多的。

#### 2.1.4 动态代理

6. 前面提到，RPC 的主要目的就是**让我们调用远程方法像调用本地方法一样简单**，**我们不需要关心远程方法调用的细节**，比如网络传输，动态代理主要就是用来**屏蔽方法调用的底层细节**，**当我们调用远程方法的时候**，**实际会通过代理对象来传输网络请求**。
7. 关于动态代理的详细阐述可以参考[3.2 动态代理](https://notebook.grayson.top/project-42/doc-770/#3-2-%E5%8A%A8%E6%80%81%E4%BB%A3%E7%90%86)。

#### 2.1.5 负载均衡

1. 当我们的系统中的某个服务的访问量特别大，并且将这个服务部署在了多台服务器上，**当客户端发起请求的时候**，我们**可以通过负载均衡算法将请求转发到其中一台服务器上**，这样**可以避免单个服务器响应同一请求而造成服务器宕机**、**崩溃等问题**。

#### 2.1.6 传输协议

1. 我们还需要设计一个私有的 RPC 协议，这个协议**是客户端和服务端交流的基础**。
2. 通过设计协议，我们**定义需要传输哪些类型的数据**，并且还会**规定每一种类型的数据应该占多少字节**，这样**我们在接收到二进制数据之后**，**就可以正确的解析出我们需要的数据**。

### 2.2 详细设计

#### 2.2.1 注册中心

> 注册中心的作用可参考[1.1 注册中心](#1-1-注册中心)。

1. 我们定义了两个接口 `ServiceDiscovery` 和 `ServiceRegistry`，这两个接口**分别定义了服务发现和服务注册行为**。
2. 接下来我们**使用 Zookeeper 作为注册中心的实现方式**，并**实现了这两个接口**。
3. **当我们的服务被注册进 Zookeeper 的时候**，我们将完整的服务名称**（`className + group + version`）**作为根节点**，**子节点是对应的服务地址**（`ip + 端口号 `）。
  
   > 1. `className`：**服务接口名也就是类名**，例如 `top.grayson.provider.impl.ZkServiceProviderImpl`。
   > 2. `version`：**服务版本**，**主要是为后续不兼容升级提供可能**。
   > 3. `group`：**主要用于处理一个接口有多个类实现的情况**。
4. **一个根节点可能会对应多个服务地址**（相同服务被部署多份的情况），如果我们**要获得某个服务对应的地址**的话，就**直接根据完整的服务名称来获取其下的所有子节点**，然后**通过具体的负载均衡策略取出一个**就可以了：![](https://notebook.grayson.top/media/202108/2021-08-28_1630190.7235401684883743.png)

#### 2.2.2 传输协议

```txt
*   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的 Id）
```

1. **魔法数**：
   1. 通常来说是**4 字节**。
   2. 主要是为了**筛选来到服务端的数据包**，有了这个魔法数之后，**服务端首先取出前面 4 个字节进行比对**，**能够在第一时间识别出这个数据包并非是遵循自己协议的**，**也就是无效数据包**，**为了安全考虑可以直接关闭连接以节省资源**。
2. **版本**：
   1. 通常来说是**1 字节**。
   2. 主要用来**标识服务的版本**，**为后续不兼容升级提供可能**，比如服务接口增加方法，或服务模型增加字段，**需通过变更版本号升级**。
3. **消息长度**：
   1. **运行时计算出来**。
4. **消息类型**：
   1. 通常来说是**1 字节**。
   2. 主要用来**标识消息是心跳消息还是正常发送的消息**。
5. **压缩类型**：
   1. 通常来说是**1 字节**。
   2. 目前**采用的是 GZIP 压缩**。
6. **序列化类型**：
   1. 通常来说是**1 字节**。
   2. 目前**支持 Kyro 和 Protostuff 两种序列化方式**。
7. **请求 ID**：
   1. 通常来说是**4 字节**。

#### 2.2.3 编解码器

##### 2.2.3.1 编码器

1. 编码器主要**负责处理出站消息**，**将消息格式转换字节数组然后写入到字节数据的容器 ByteBuf 中**，`body`**部分需要经过序列化对象**、**压缩字节等步骤**。

##### 2.2.3.2 解码器

1. 解码器主要**负责处理入站消息**，**将 ByteBuf 消息格式的对象转换为我们需要的业务对象**，`body`**部分需要经过解压缩**、**反序列化等步骤**。

#### 2.2.4 动态代理

1. **通过[动态代理](https://notebook.grayson.top/project-42/doc-770/#3-2-%E5%8A%A8%E6%80%81%E4%BB%A3%E7%90%86)来屏蔽复杂的网络传输细节**，**当我们去调用一个远程的方法时**，**实际上是通过代理对象调用的**，**网络传输细节都封装在了 `RpcClientProxy.invoke()` 中**。

#### 2.2.5 通过注解注册/消费服务

1. 我们这里借用了 `Spring` 容器的相关功能，定义了两个注解：
   1. `RpcService`：**注册服务**。
   2. `RpcReference`：**消费服务**。
2. 我们需要**实现 `BeanPostProcessor` 接口**，并**重写 `postProcessBeforeInitialization()` 和 `postProcessAfterInitialization()` 方法**，Spring Bean 在实例化前会调用 `postProcessAfterInitialization()` 方法，在 Spring Bean 实例化之后会调用 `postProcessAfterInitialization()` 方法。
3. **被我们使用 `RpcService` 和 `RpcReference` 注解的类都算是 Spring Bean**：
   1. 我们可以**在 `postProcessAfterInitialization()` 方法中去判断类上是否有 `RpcService` 注解**，**如果有的话**，**就取出 `group` 和 `version` 的值**，**然后**，**再调用 `ServiceProvider` 的 `publishService()` 方法发布服务即可**。
   2. 我们可以**在 `postProcessAfterInitialization()` 方法中遍历类的属性上是否有 `RpcReference` 注解**，**如果有的话**，**我们就通过[反射](https://notebook.grayson.top/project-34/doc-820)将这个属性赋值即可**。

#### 2.2.6 负载均衡

1. 负载均衡算法我们采用的有两种，分比为：
   1. **随机负载均衡**。
   2. **[一致性哈希](https://notebook.grayson.top/project-37/doc-762)负载均衡**。

## ❗️ 勘误

- 如果在文章中发现了问题，欢迎提交 PR 或者 issue，欢迎大神们多多指点🙏🙏🙏

## ♥️ Thanks

感谢Star！

[![Stargazers over time](https://starchart.cc/wpwbb510582246/rpc-framework.svg)](https://starchart.cc/wpwbb510582246/DeepinKnowledge)

## ©️ 转载

<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="知识共享许可协议" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br />本<span xmlns:dct="http://purl.org/dc/terms/" href="http://purl.org/dc/dcmitype/Text" rel="dct:type">作品</span>由 <a xmlns:cc="http://creativecommons.org/ns#" href="https://github.com/wpwbb510582246" property="cc:attributionName" rel="cc:attributionURL">Grayson</a> 创作，采用<a rel="license" href="http://creativecommons.org/licenses/by/4.0">知识共享署名 4.0 国际许可协议</a>进行许可。
