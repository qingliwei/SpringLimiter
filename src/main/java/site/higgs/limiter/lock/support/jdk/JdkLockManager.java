package site.higgs.limiter.lock.support.jdk;

import site.higgs.limiter.lock.Lock;
import site.higgs.limiter.lock.LockManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by caocg on 2018/9/23.
 */
public class JdkLockManager extends LockManager {

    private final ConcurrentMap<String, Lock> lockMap = new ConcurrentHashMap<String, Lock>(16);

    private boolean dynamic = true;

    private Config config;

    public JdkLockManager(Config config) {
        this.config = config;
    }

    public JdkLockManager() {
        config = new Config();
    }


    @Override
    public Lock getLock(String name) {
        Lock lock = this.lockMap.get(name);
        if (lock == null && this.dynamic) {
            synchronized (this.lockMap) {
                lock = this.lockMap.get(name);
                if (lock == null) {
                    lock = createJdkLock(name);
                    this.lockMap.put(name, lock);
                }
            }
        }
        return lock;
    }

    private Lock createJdkLock(String name) {
        return new JdkLock(name, this.config);
    }


    @Override
    public Collection<String> getLockNames() {
        return lockMap.keySet();
    }


}
