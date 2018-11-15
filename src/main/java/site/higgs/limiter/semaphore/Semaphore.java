package site.higgs.limiter.semaphore;

import site.higgs.limiter.Limiter;

import java.util.Map;

/**
 * Created by caocg on 2018/9/23.
 */
public abstract class Semaphore implements Limiter {


    public abstract boolean acquire(Object key, int permits);

    public abstract void release(Object key, int permits);

    public abstract String getName();

    @Override
    public String getLimiterName() {
        return getName();
    }

    @Override
    public boolean limite(Object key, Map<String, Object> args) {
        int permits;
        if (args == null || args.get("permits") == null) {
            permits = 1;
        } else {
            permits = (int) args.get("permits");
        }
        return acquire(key, permits);
    }

    @Override
    public void unlimite(Object key, Map<String, Object> args) {
        int permits;
        if (args == null || args.get("permits") == null) {
            permits = 1;
        } else {
            permits = (int) args.get("permits");
        }
        release(key, permits);
    }

}
