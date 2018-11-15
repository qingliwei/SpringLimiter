package site.higgs.limiter.semaphore.support.jdk;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 * Created by caocg on 2018/9/15.
 */
public class JdkSemaphore extends site.higgs.limiter.semaphore.Semaphore {

    protected final Log logger = LogFactory.getLog(getClass());

    private String name;


    private LoadingCache<Object, Semaphore> caches;


    private Timer timer;

    public JdkSemaphore(String name, Config config) {
        this(name, config.getSize(), config.getDuration(), config.getTimeUnit(), config.getTimerduration());
    }

    public JdkSemaphore(String name) {
        this(name, 2 << 20, 300, TimeUnit.SECONDS, 1800000);
    }

    public JdkSemaphore(String name, int size, long duration, TimeUnit timeUnit, long timerduration) {

        this.caches = CacheBuilder.newBuilder()
                .expireAfterAccess(duration, timeUnit)
                .maximumSize(size)
                .build(new CacheLoader<Object, Semaphore>() {
                    @Override
                    public Semaphore load(Object key) throws Exception {
                        KeyWrapper keyWrapper = (KeyWrapper) key;
                        return new Semaphore(keyWrapper.permits);
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
    public boolean acquire(Object key, int permits) {
        KeyWrapper keyWrapper = new KeyWrapper(key, permits);
        try {
            Semaphore semaphore = this.caches.get(keyWrapper);
            return semaphore.tryAcquire();
        } catch (ExecutionException e) {
            return false;
        }
    }

    @Override
    public void release(Object key, int permits) {
        KeyWrapper keyWrapper = new KeyWrapper(key, permits);
        try {
            Semaphore semaphore = this.caches.get(keyWrapper);
            semaphore.release();
        } catch (ExecutionException e) {

        }
    }

    protected static class KeyWrapper {

        private Object key;

        private int permits;

        public KeyWrapper(Object key, int permits) {
            this.key = key;
            this.permits = permits;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof KeyWrapper)) return false;

            KeyWrapper that = (KeyWrapper) o;

            if (permits != that.permits) return false;
            return key != null ? key.equals(that.key) : that.key == null;
        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + permits;
            return result;
        }
    }
}
