package site.higgs.limiter.ratelimiter.support.guava;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import site.higgs.limiter.ratelimiter.RateLimiter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by caocg on 2018/9/23.
 */
public class GuavaRateLimiter extends RateLimiter {


    private String name;

    private LoadingCache<Object, com.google.common.util.concurrent.RateLimiter> caches;

    private Timer timer;


    public GuavaRateLimiter(String name, Config config) {
        this(name, config.getSize(), config.getDuration(), config.getTimeUnit(), config.getTimerduration());
    }

    public GuavaRateLimiter(String name) {
        this(name, 2 << 20, 300, TimeUnit.SECONDS, 1800000);
    }

    public GuavaRateLimiter(String name, int size, long duration, TimeUnit timeUnit, long timerduration) {

        this.caches = CacheBuilder.newBuilder()
                .expireAfterAccess(duration, timeUnit)
                .maximumSize(size).build(new CacheLoader<Object, com.google.common.util.concurrent.RateLimiter>() {
                    @Override
                    public com.google.common.util.concurrent.RateLimiter load(Object key) throws Exception {
                        KeyWrapper keyWrapper = (KeyWrapper) key;
                        return com.google.common.util.concurrent.RateLimiter.create(keyWrapper.pps);

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
    public boolean acquire(Object key, double pps) {
        KeyWrapper keyWrapper = new KeyWrapper(key, pps);
        try {
            com.google.common.util.concurrent.RateLimiter rateLimiter = this.caches.get(keyWrapper);
            return rateLimiter.tryAcquire();
        } catch (ExecutionException e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static class KeyWrapper {

        private Object key;

        private double pps;

        public KeyWrapper(Object key, double pps) {
            this.key = key;
            this.pps = pps;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof KeyWrapper)) return false;

            KeyWrapper that = (KeyWrapper) o;

            if (Double.compare(that.pps, pps) != 0) return false;
            return key != null ? key.equals(that.key) : that.key == null;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = key != null ? key.hashCode() : 0;
            temp = Double.doubleToLongBits(pps);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }
}
