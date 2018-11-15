package site.higgs.limiter.semaphore.support.redis;

import org.redisson.config.Config;
import site.higgs.limiter.semaphore.Semaphore;
import site.higgs.limiter.semaphore.SemaphoreManager;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by caocg on 2018/9/25.
 */
public class RedisSemaphoreManager extends SemaphoreManager {

    private Config config;

    private boolean dynamic = true;

    private final ConcurrentMap<String, Semaphore> semaphoreConcurrentMap = new ConcurrentHashMap<String, Semaphore>(16);


    public RedisSemaphoreManager(Config config) {
        this.config = config;
    }


    private RedisSemaphore createRedisSemaphore(String name) {
        return new RedisSemaphore(name, config);
    }

    @Override
    public Semaphore getSemaphore(String name) {
        Semaphore semaphore = this.semaphoreConcurrentMap.get(name);
        if (semaphore == null && this.dynamic) {
            synchronized (this.semaphoreConcurrentMap) {
                semaphore = this.semaphoreConcurrentMap.get(name);
                if (semaphore == null) {
                    semaphore = createRedisSemaphore(name);
                    this.semaphoreConcurrentMap.put(name, semaphore);
                }
            }
        }
        return semaphore;
    }

    @Override
    public Collection<String> getSemaphoreNames() {
        return semaphoreConcurrentMap.keySet();
    }
}
