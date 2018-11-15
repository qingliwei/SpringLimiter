package site.higgs.limiter.semaphore.support.redis;

import org.redisson.Redisson;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import site.higgs.limiter.semaphore.Semaphore;
/**
 * Created by caocg on 2018/9/25.
 */
public class RedisSemaphore extends Semaphore {

    private String name;

    private RedissonClient redisson;


    public RedisSemaphore(String name, Config config) {
        this.name = name;
        this.redisson = Redisson.create(config);
    }


    @Override
    public boolean acquire(Object key, int permits) {
        RSemaphore rSemaphore = redisson.getSemaphore(key.toString());
        rSemaphore.trySetPermits(permits);
        return rSemaphore.tryAcquire();
    }

    @Override
    public void release(Object key, int permits) {
        RSemaphore rSemaphore = redisson.getSemaphore(key.toString());
        rSemaphore.release();
    }

    @Override
    public String getName() {
        return null;
    }
}
