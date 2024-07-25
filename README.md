# mScheduler
提供集群环境中负载均衡的定时任务，支持基于Spring管理的quartz、@Scheduled的定时任务，详见配置说明。

# 配置说明
 ``` yml
  mScheduler:
    #策略sticky、random
    #sticky：先占先得一直占用，直到应用停止或重启 【默认】
    #random：随机，对于一天多次执行的任务可能有效果，否则的话可能就是一直都是那个时钟靠前的服务器一直抢占到执行权
    strategy: sticky
    #占用时长 【默认1800s】，超过这个时间释放占用，对sticky策略的无效
    lock-time-secs: 1800
    #数据存储方式：db、redis【默认优先使用redis】
    store: db
    #数据存储方式为db是使用，当有多个数据源时可能需要指定；
    #不指定的话优先匹配动态数据源没有的话再使用任意一个
    #dbSourceBeanName: masterDataSource
 ```

# 用法
### maven引入依赖
  ```xml
  <dependency>
       <groupId>name.menghw</groupId>
       <artifactId>mScheduler</artifactId>
       <version>1.0</version>
  </dependency>
  ```
### 启动类上导入
  ```java
  @Import(MSchedulerConfiguration.class)
  @SpringBootApplication
  public class MyApplication {
      public static void main(String [] args) {
         //...
      }
  }
  ```
