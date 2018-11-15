package site.higgs.limiter.ratelimiter.support.guava;

import site.higgs.limiter.ratelimiter.RateLimiter;
import site.higgs.limiter.ratelimiter.RateLimiterManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by caocg on 2018/9/23.
 */
public class GuavaRateLimiterManager extends RateLimiterManager {

    private final ConcurrentMap<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<String, RateLimiter>(16);

    private boolean dynamic = true;

    private Config config;

    public GuavaRateLimiterManager() {
        this.config = new Config();
    }

    public GuavaRateLimiterManager(Config config) {
        this.config = config;
    }

    @Override
    public RateLimiter getRateLimiter(String name) {
        RateLimiter rateLimiter = this.rateLimiterMap.get(name);
        if (rateLimiter == null && this.dynamic) {
            synchronized (this.rateLimiterMap) {
                rateLimiter = this.rateLimiterMap.get(name);
                if (rateLimiter == null) {
                    rateLimiter = createGuavaRateLimiter(name);
                    this.rateLimiterMap.put(name, rateLimiter);
                }
            }
        }
        return rateLimiter;
    }

    public RateLimiter createGuavaRateLimiter(String name) {
        return new GuavaRateLimiter(name, this.config);
    }

    @Override
    public Collection<String> getRateLimiterNames() {
        return this.rateLimiterMap.keySet();
    }
}
