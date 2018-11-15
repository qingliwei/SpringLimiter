

# SpringLimiter

### 一个注解使你的SpringBoot项目获得分布式锁和限流器能力

----

### 添加依赖

`**该项目尚未上传到maven中央仓库,所以需要自行clone本项目本地编译**`

  pom.xml 添加依赖

```xml
<dependency>
	<groupId>site.higgs</groupId>
	<artifactId>limiter</artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
```

该模块依赖于`spring-context`、`spring-core`、`guava` 、`redisson`，如果存在冲突自行排出相关模块

例如

```xml
<dependency>
	<groupId>site.higgs</groupId>
	<artifactId>limiter</artifactId>
	<version>1.0-SNAPSHOT</version>
	<exclusions>
		<exclusion>
			<groupId>org.springframework</groupId>
			<artifactId>spring-context</artifactId>
		</exclusion>
		<exclusion>
			<groupId>org.springframework</groupId>
			<artifactId>spring-core</artifactId>
		</exclusion>
	</exclusions>
</dependency>
```

添加注解`@EnableLimiter`

```java
@SpringBootApplication
@EnableLimiter
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```



### 用例

-----

####  一、@HLock 

  假设有这样一个场景，用户可以使用兑换码进行兑换礼品，理所当然的是每个兑换码只能使用一次，那我们就需要在该接口中实现每个充值码能且只能兑换一次，一个通常得做法可能是开发人员需要手动得给这条数据添加一个悲观锁，例如 `select code from code_table where code =#{code} for update`,这样写是非常有必要的，可以防止该接口遭受恶意的攻击。

  但这样得写法有待商榷，关于锁的开销倒是其次，而开发人员必须时刻提防这些不期而至的恶意攻击，让本来一个简单的逻辑变复杂。现在我们可以声明式的获取这个锁，将“锁”和业务逻辑解离开来。

 例如：

```java
    /**
     * 限制键为 #redeemCode+#user.userId
     * 当多个请求同时到达时，只有一个会被正常处理，其他请求会被降级
     * 当正常的请求被处理完毕，锁会释放
     * 值得注意得是keys 本身不会包含方法名，最好前面加前缀同其他接口分开
     * @param redeemCode
     * @return
     */
    @RequestMapping(value = "/exchange", method = RequestMethod.GET)
    @HLock(keys = "#redeemCode+#user.userId", fallbackResolver = "busyFallback", lockManager = "redisLockManager", argInjecters = "injectUser")
    public ResponseMessage exchange(@RequestParam("redeemCode") String redeemCode) {
        try {
            // do something
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
        return ResponseMessage.ok(null);
    }
```

  通过`@HLock` 注解为这个接口添加了一个锁，`keys` 中指定了这个锁的键值，显而易见，对于同时到达的、相同`chargeCode`的请求，只会有一个正在被正确处理，而其他得请求将会被降级，而被降级后得请求将会返回什么呢？

答案就在`fallbackResolver`属性上，实际上 `busyFallback`是一个被Spring 管理得Bean,我们需要先实现`LimiterFallbackResolver`接口来定义接口被降级后得行为，例如本例中

```java
public class BusyFallbackResolver implements LimiterFallbackResolver<ResponseMessage> {
    @Override
    public ResponseMessage resolve(Method method, Class<?> aClass, Object[] objects, 		String s) {
        //对于被降级的请求直接返回服务繁忙
        return ResponseMessage.erroe("服务繁忙");
    }
}

```

`BusyFallbackResolver` 实现将会使降级后的接口直接返回“服务繁忙”，将该实现注入到Bean容器中即可使用

```java
@Bean
LimiterFallbackResolver<ResponseMessage> busyFallback() {
        return new BusyFallbackResolver();
}
```

现在，另一个疑问，我们的这个所谓的锁是如何实现的呢？实际上,我将锁抽象成一个接口`site.higgs.limiter.lock.Lock`，并且提供了两种实现：一种是使用的Jdk提供得Lock实现,如果所应用的项目没有多实例部署得需求，使用Jdk锁足以满足需求；另一种是使用的redis实现的分布式锁(redisson)，这在多实例项目中非常合适。当然，如果这两种锁不满足需求，开发人员可以自己实现相应接口来增加一种锁，相关的代码在`site.higgs.limiter.lock.support`下。

而如何配置使用哪一种锁呢？

首先我们需要注入LockManager,

```java
 @Bean
public LockManager redisLockManager() {
       Config config = new Config();
       config.useSingleServer().setAddress("redis://127.0.0.1:6379")
                .setDatabase(1);
       //config 来源于redisson 
       config.setLockWatchdogTimeout(1000 * 60 * 30);
       RedisLockManager redisLockManager = new RedisLockManager(config);
       return redisLockManager;
}
```

并在使用`@HLock`注解时选择该LockManager

```java
 @HLock(keys = "#changeCode", fallbackResolver = "busyFallback",lockManager = "redisLockManager")
```

同理 使用Jdk锁

```java
@Bean 
public LockManager jdkLockManager() {
        return  new JdkLockManager();
 }
```





---

  现在，我们去讨论另外一个问题，如果现在的需求不再是每个兑换码只能被兑换一次，而是每个兑换码每个用户只能被兑换一次呢，与上面不同的是现在`keys`要与用户编号`userId`产生某种关系，而似乎方法参数并没有任何和用户相关得参数。

 实际上可以通过配置`@HLock` 的`argInjecters` 注入用户对象。为此，我们先实现一个参数注入器

```java
public class InjectUser implements ArgumentInjecter {
    @Override
    public Map<String, Object> inject(Object... objects) {
        /**
         * 大多数项目中 当前登录用户都是存放在线程级变量中
         */
        User user = new User();
        user.setUserId("123");
        user.setUserName("higgs");
        Map<String, Object> retVal = new HashMap<>();
        retVal.put("user", user);
        return retVal;
    }
}
```

同样的注入到Spring容器中使用

```java
@Bean
public ArgumentInjecter injectUser() {
        return new InjectUser();
}
```

最后修改`@HLock`来使用

```java
@HLock(keys = "#changeCode+#user.userId", fallbackResolver = "busyFallback",lockManager = "redisLockManager",argInjecters = "injectUser")
```

至于keys表达式的形式参考`Spel`的相关资料，此处不再赘述。

---

#### 二、`@HSemaphore` 

     `@HSemaphore` 注解用来为接口声明一个信号量，可以达到限制并发数得效果

```java
@HSemaphore(keys = "'exchange2'+#user.userId", fallbackResolver = "busyFallback", semaphoreManager = "redisSemaphoreManager",permits = 5, argInjecters = "injectUser")
```

配置manager代码

```java
  @Bean
 public SemaphoreManager redisSemaphoreManager() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(3);
        config.setLockWatchdogTimeout(1000 * 60 * 30);
        RedisSemaphoreManager semaphoreManager = new RedisSemaphoreManager(config);
        return semaphoreManager;
 }
```



#### 三 、`@HRateLimiter`

  `@HRateLimiter` 注解用来为接口声明一个速率限制器，限制接口的访问频率

  配置 RateLimiterManager

```java
 @Bean
public RateLimiterManager redisRateLimiterManager() {
        Config config = new Config();
        // 不要和 lock 使用一个db 会有冲突 ，这里选择db2
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(2);
        config.setLockWatchdogTimeout(1000 * 60 * 30);
        RedisRateLimiterManager redisRateLimiterManager = new       RedisRateLimiterManager(config);
        return redisRateLimiterManager;
}
```



### 其他配置

----

 实际上，你必须配置一个全局生效的配置，来保证组件的高可用和缺省参数

```java
@Bean
    GlobalConfig globalConfig() {
        LimiterGlobalConfig limiterGlobalConfig = new LimiterGlobalConfig();
        // 当组件内遇到异常时是否进行降级，比如使用分布式锁时，
        // redis 宕机后的降级策略，返回true未不降级，false为降级
        limiterGlobalConfig.setErrorHandler(new ErrorHandler() {
            @Override
            public boolean handleError(RuntimeException runtimeException) {
                throw runtimeException;
            }
        });
        // 当没有配置降级接口时使用全局配置
        limiterGlobalConfig.setLimiterFallbackResolver(new LimiterFallbackResolver() {
            @Override
            public Object resolve(Method method, Class clazz, Object[] args, String key) {
                throw new RuntimeException("");
            }
        });
        return limiterGlobalConfig;
    }
```

使用Redis组件时，你可以手动配置地址和db，甚至可以使用集群，还有防止死锁机制(看门狗)

```java
    @Bean
    public LockManager redisLockManager() {

        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379")
                .setDatabase(1);
        config.setLockWatchdogTimeout(1000 * 60 * 30);
        RedisLockManager redisLockManager = new RedisLockManager(config);
        return redisLockManager;
    }
```

使用Jdk锁时，可以配置的参数

```java
  @Bean
public LockManager jdkLockManager() {
        site.higgs.limiter.lock.support.jdk.Config config = new site.higgs.limiter.lock.support.jdk.Config();
        config.setSize(2 << 10);// //缓存锁的容量，当内存中存在的锁实例超过该阈值时将会根据LUR清除最近最少用到的锁实例
        config.setDuration(30);   //在多久没获取该锁时自动解锁
        config.setTimeUnit(TimeUnit.SECONDS);
        config.setTimerduration(86400000);// //看门狗 多久进行一次大扫除  单位毫秒 主要用来清除最近未使用到的锁 减少内存消耗
        return new JdkLockManager();
}
```





### 项目架构

---

    组件的类图如下，`Limiter`作为一个顶级接口，提供了扩展其他组件的能力

![](C:\Users\24191\Desktop\limiter.jpg)

![](C:\Users\24191\Desktop\manager.jpg)

例子代码

```java
package site.higgs.limiterdemo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.higgs.limiter.lock.HLock;
import site.higgs.limiter.ratelimiter.HRateLimiter;
import site.higgs.limiter.semaphore.HSemaphore;

@RestController
public class Controller {





    /**
     * 限制键为 #redeemCode+#user.userId
     * 当多个请求同时到达时，只有一个会被正常处理，其他请求会被降级
     * 当正常的请求被处理完毕，锁会释放
     * 值得注意得是keys 本身不会包含方法名，最好前面加前缀同其他接口分开
     * @param redeemCode
     * @return
     */
    @RequestMapping(value = "/exchange", method = RequestMethod.GET)
    @HLock(keys = "#redeemCode+#user.userId", fallbackResolver = "busyFallback", lockManager = "redisLockManager", argInjecters = "injectUser")
    public ResponseMessage exchange(@RequestParam("redeemCode") String redeemCode) {
        try {
            // do something
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
        return ResponseMessage.ok(null);
    }

    /**
     * 限制该接口的访问频率为 10次/秒
     * redis实现的限流器精度和网络环境和机器配置有关，自行测试效果
     * @param redeemCode
     * @return
     */
    @RequestMapping(value = "/exchange1", method = RequestMethod.GET)
    @HRateLimiter(keys = "'exchange1'+#redeemCode", fallbackResolver = "busyFallback", rateLimiterManager = "redisRateLimiterManager",pps = 10, argInjecters = "injectUser")
    public ResponseMessage exchange1(@RequestParam("redeemCode") String redeemCode) {
        try {
            // do something
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }
        return ResponseMessage.ok(null);
    }


    /**
     * 限制该接口并发数为10
     * redis实现的限流器精度和网络环境和机器配置有关，自行测试效果
     * @param redeemCode
     * @return
     */
    @RequestMapping(value = "/exchange2", method = RequestMethod.GET)
    @HSemaphore(keys = "'exchange2'+#redeemCode", fallbackResolver = "busyFallback", semaphoreManager = "redisSemaphoreManager",permits = 5, argInjecters = "injectUser")
    public ResponseMessage exchange2(@RequestParam("redeemCode") String redeemCode) {
        try {
            // do something
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
        return ResponseMessage.ok(null);
    }




}

```

Application.java

```java
package site.higgs.limiterdemo;

import org.redisson.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import site.higgs.limiter.annotation.EnableLimiter;
import site.higgs.limiter.config.GlobalConfig;
import site.higgs.limiter.config.LimiterGlobalConfig;
import site.higgs.limiter.interceptor.ArgumentInjecter;
import site.higgs.limiter.interceptor.ErrorHandler;
import site.higgs.limiter.interceptor.LimiterFallbackResolver;
import site.higgs.limiter.lock.LockManager;
import site.higgs.limiter.lock.support.jdk.JdkLockManager;
import site.higgs.limiter.lock.support.redis.RedisLockManager;
import site.higgs.limiter.ratelimiter.RateLimiterManager;
import site.higgs.limiter.ratelimiter.support.guava.GuavaRateLimiterManager;
import site.higgs.limiter.ratelimiter.support.redis.RedisRateLimiterManager;
import site.higgs.limiter.semaphore.SemaphoreManager;
import site.higgs.limiter.semaphore.support.jdk.JdkSemaphoreManager;
import site.higgs.limiter.semaphore.support.redis.RedisSemaphoreManager;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableLimiter
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    /**
     * 定义一个降级接口，被拦截降级的请求将会返回 服务繁忙
     * 可以直接设置busyFallback 使用该组件
     *
     * @return
     */
    @Bean
    LimiterFallbackResolver<ResponseMessage> busyFallback() {
        return new BusyFallbackResolver();
    }

    /**
     * 定义一个参数注入器
     *
     * @return
     */
    @Bean
    public ArgumentInjecter injectUser() {
        return new UserInfoInjecter();
    }


    /**
     * 定义一个全局生效的配置
     *
     * @return
     */
    @Bean
    GlobalConfig globalConfig() {
        LimiterGlobalConfig limiterGlobalConfig = new LimiterGlobalConfig();
        // 当组件内遇到异常时是否进行降级，比如使用分布式锁时，
        // redis 宕机后的降级策略，返回true未不降级，false为降级
        limiterGlobalConfig.setErrorHandler(new ErrorHandler() {
            @Override
            public boolean handleError(RuntimeException runtimeException) {
                throw runtimeException;
            }
        });
        // 当没有配置降级接口时使用全局配置
        limiterGlobalConfig.setLimiterFallbackResolver(new LimiterFallbackResolver() {
            @Override
            public Object resolve(Method method, Class clazz, Object[] args, String key) {
                throw new RuntimeException("");
            }
        });
        return limiterGlobalConfig;
    }

    // 配置一个LockManager, 可以设置lockManager = "redisLockManager" 使用该LockManager
    @Bean
    public LockManager redisLockManager() {

        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379")
                .setDatabase(1);
        config.setLockWatchdogTimeout(1000 * 60 * 30);
        RedisLockManager redisLockManager = new RedisLockManager(config);
        return redisLockManager;
    }

    @Bean
    public LockManager jdkLockManager() {
        site.higgs.limiter.lock.support.jdk.Config config = new site.higgs.limiter.lock.support.jdk.Config();
        config.setSize(2 << 10);// //缓存锁的容量，当内存中存在的锁实例超过该阈值时将会根据LUR清除最近最少用到的锁实例
        config.setDuration(30);   //在多久没获取该锁时自动解锁
        config.setTimeUnit(TimeUnit.SECONDS);
        config.setTimerduration(86400000);// //看门狗 多久进行一次大扫除  单位毫秒 主要用来清除最近未使用到的锁 减少内存消耗
        return new JdkLockManager();
    }

    @Bean
    public RateLimiterManager redisRateLimiterManager() {
        Config config = new Config();
        // 不要和 lock 使用一个db 会有冲突 ，这里选择db2
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(2);
        config.setLockWatchdogTimeout(1000 * 60 * 30);
        RedisRateLimiterManager redisRateLimiterManager = new RedisRateLimiterManager(config);
        return redisRateLimiterManager;
    }

    @Bean
    public RateLimiterManager guavaRateLimiterManager() {
        return new GuavaRateLimiterManager();
    }

    @Bean
    public SemaphoreManager redisSemaphoreManager() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(3);
        config.setLockWatchdogTimeout(1000 * 60 * 30);
        RedisSemaphoreManager semaphoreManager = new RedisSemaphoreManager(config);
        return semaphoreManager;
    }


    @Bean
    public SemaphoreManager jdkSemaphoreManager() {
        return new JdkSemaphoreManager();
    }


}

```

