package site.higgs.limiter.ratelimiter.support.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import site.higgs.limiter.lock.Lock;
import site.higgs.limiter.ratelimiter.RateLimiter;
import site.higgs.limiter.ratelimiter.RateLimiterManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by caocg on 2018/9/25.
 */
public class RedisRateLimiterManager extends RateLimiterManager {

    private Config config;

    private boolean dynamic = true;

    private final ConcurrentMap<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<String, RateLimiter>(16);


    public RedisRateLimiterManager(Config redisConfig) {
        this.config = redisConfig;
    }

    private RedisRateLimiter createRedisRateLimiter(String name) {
        return new RedisRateLimiter(name, Redisson.create(this.config));
    }

    @Override
    public RateLimiter getRateLimiter(String name) {
        RateLimiter limiter = this.rateLimiterMap.get(name);
        if (limiter == null && this.dynamic) {
            synchronized (this.rateLimiterMap) {
                limiter = this.rateLimiterMap.get(name);
                if (limiter == null) {
                    limiter = createRedisRateLimiter(name);
                    this.rateLimiterMap.put(name, limiter);
                }
            }
        }
        return limiter;
    }

    @Override
    public Collection<String> getRateLimiterNames() {
        return rateLimiterMap.keySet();
    }
}
