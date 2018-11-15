package site.higgs.limiter.ratelimiter.support.redis;

import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import site.higgs.limiter.ratelimiter.RateLimiter;

/**
 * Created by caocg on 2018/9/25.
 */
public class RedisRateLimiter extends RateLimiter {


    private RedissonClient redisson;

    private String name;

    public RedisRateLimiter(String name, RedissonClient redisson) {
        this.redisson = redisson;
        this.name = name;
    }


    @Override
    public boolean acquire(Object key, double pps) {
        RRateLimiter rRateLimiter = redisson.getRateLimiter(key.toString());
        rRateLimiter.trySetRate(RateType.OVERALL, new Double(pps).longValue(), 1, RateIntervalUnit.SECONDS);
        return rRateLimiter.tryAcquire();
    }

    @Override
    public String getName() {
        return this.name;
    }
}
