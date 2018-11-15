package site.higgs.limiter.lock;

import site.higgs.limiter.Limiter;

import java.util.Map;

/**
 * Created by caocg on 2018/9/21.
 */
public abstract class Lock implements Limiter {

    public abstract boolean lock(Object key);

    public abstract void unlock(Object key);

    public abstract String getName();

    @Override
    public String getLimiterName() {
        return getName();
    }

    @Override
    public boolean limite(Object key, Map<String, Object> args) {
        return lock(key);
    }

    @Override
    public void unlimite(Object key, Map<String, Object> args) {
        unlock(key);
    }
}
