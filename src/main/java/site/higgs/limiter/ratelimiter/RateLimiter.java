package site.higgs.limiter.ratelimiter;

import site.higgs.limiter.Limiter;

import java.util.Map;

/**
 * Created by caocg on 2018/9/23.
 */
public abstract class RateLimiter implements Limiter {


    public abstract boolean acquire(Object key, double pps);

    public abstract String getName();


    @Override
    public String getLimiterName() {
        return getName();
    }

    @Override
    public boolean limite(Object key, Map<String, Object> args) {
        double pps;
        if (args == null || args.get("pps") == null) {
            pps = 1;
        } else {
            pps = (double) args.get("pps");
        }
        return acquire(key, pps);
    }

    @Override
    public void unlimite(Object key, Map<String, Object> args) {
        // do nothing
    }
}
