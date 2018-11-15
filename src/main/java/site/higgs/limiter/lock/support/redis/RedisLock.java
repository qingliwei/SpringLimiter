package site.higgs.limiter.lock.support.redis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import site.higgs.limiter.lock.Lock;

/**
 * Created by caocg on 2018/9/24.
 */
public class RedisLock extends Lock {
    protected static final Log logger = LogFactory.getLog(RedisLock.class);

    private RedissonClient redisson;

    private String name;

    public RedisLock(String name, RedissonClient redisson) {

        this.name = name;
        this.redisson = redisson;
    }


    @Override
    public boolean lock(Object key) {

        RLock rLock = redisson.getLock(key.toString());
        return rLock.tryLock();
    }

    @Override
    public void unlock(Object key) {
        RLock rLock = redisson.getLock(key.toString());
        rLock.unlock();
    }

    @Override
    public String getName() {
        return null;
    }
}
