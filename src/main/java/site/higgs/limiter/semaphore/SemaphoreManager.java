package site.higgs.limiter.semaphore;

import site.higgs.limiter.LimiterManager;
import java.util.Collection;

/**
 * Created by caocg on 2018/9/23.
 */
public abstract class SemaphoreManager implements LimiterManager<Semaphore> {

    public abstract Semaphore getSemaphore(String name);

    public abstract Collection<String> getSemaphoreNames();

    @Override
    public Semaphore getLimiter(String name) {
        return getSemaphore(name);
    }

    @Override
    public Collection<String> getLimiterNames() {
        return getSemaphoreNames();
    }
}
