package site.higgs.limiter.lock.support.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import site.higgs.limiter.lock.Lock;
import site.higgs.limiter.lock.LockManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by caocg on 2018/9/25.
 */
public class RedisLockManager extends LockManager {

    private Config config;

    private boolean dynamic = true;

    private final ConcurrentMap<String, Lock> lockMap = new ConcurrentHashMap<String, Lock>(16);


    public RedisLockManager(Config redisConfig) {
        this.config = redisConfig;
    }

    private RedisLock createRedisLock(String name) {
        return new RedisLock(name, Redisson.create(this.config));
    }


    @Override
    public Lock getLock(String name) {
        Lock lock = this.lockMap.get(name);
        if (lock == null && this.dynamic) {
            synchronized (this.lockMap) {
                lock = this.lockMap.get(name);
                if (lock == null) {
                    lock = createRedisLock(name);
                    this.lockMap.put(name, lock);
                }
            }
        }
        return lock;
    }

    @Override
    public Collection<String> getLockNames() {
        return this.lockMap.keySet();
    }
}
