package site.higgs.limiter.lock.support.jdk;

import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by caocg on 2018/9/15.
 */
public class JdkLock extends site.higgs.limiter.lock.Lock {

    protected final Log logger = LogFactory.getLog(getClass());

    private String name;


    private LoadingCache<Object, java.util.concurrent.locks.Lock> caches;


    private Timer timer;

    private Supplier<java.util.concurrent.locks.Lock> supplier = () -> new ReentrantLock(false);


    public JdkLock(String name) {
        this(name, 2 << 20, 300, TimeUnit.SECONDS, 1800000);
    }

    public JdkLock(String name, Config config) {
        this(name, config.getSize(), config.getDuration(), config.getTimeUnit(), config.getTimerduration());
    }

    /**
     * @param name
     * @param size          缓存锁的容量，当内存中存在的锁实例超过该阈值时将会根据LUR清除最近最少用到的锁实例
     * @param duration      在多久没获取该锁时自动解锁
     * @param timeUnit      单位
     * @param timerduration 看门狗 多久进行一次大扫除  单位毫秒
     */
    public JdkLock(String name, int size, long duration, TimeUnit timeUnit, long timerduration) {

        this.caches = CacheBuilder.newBuilder()
                .expireAfterAccess(duration, timeUnit)
                .maximumSize(size)
                .build(new CacheLoader<Object, java.util.concurrent.locks.Lock>() {
                    @Override
                    public java.util.concurrent.locks.Lock load(Object key) throws Exception {
                        return new ReentrantLock(false);
                    }
                });
        this.name = name;
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                caches.cleanUp();

            }
        }, timerduration, timerduration);

    }


    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean lock(Object key) {
        java.util.concurrent.locks.Lock lock = null;
        try {
            lock = this.caches.get(key);
        } catch (ExecutionException e) {
        }
        if (lock == null) {
            return false;
        }
        return lock.tryLock();
    }


    @Override
    public void unlock(Object key) {

        java.util.concurrent.locks.Lock lock = caches.getIfPresent(key);
        if (lock != null) {
            lock.unlock();
        }
    }

}
