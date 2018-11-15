package site.higgs.limiter.ratelimiter;

import site.higgs.limiter.LimiterManager;

import java.util.Collection;

/**
 * Created by caocg on 2018/9/23.
 */
public abstract class RateLimiterManager implements LimiterManager<RateLimiter> {

    public abstract RateLimiter getRateLimiter(String name);

    public abstract Collection<String> getRateLimiterNames();

    @Override
    public RateLimiter getLimiter(String name) {
        return getRateLimiter(name);
    }

    @Override
    public Collection<String> getLimiterNames() {
        return getRateLimiterNames();
    }
}
