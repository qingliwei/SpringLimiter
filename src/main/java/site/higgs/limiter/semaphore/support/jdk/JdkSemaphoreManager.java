package site.higgs.limiter.semaphore.support.jdk;


import site.higgs.limiter.semaphore.Semaphore;
import site.higgs.limiter.semaphore.SemaphoreManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by caocg on 2018/9/23.
 */
public class JdkSemaphoreManager extends SemaphoreManager {

    private final ConcurrentMap<String, Semaphore> semaphoreMap = new ConcurrentHashMap<String, Semaphore>(16);

    private boolean dynamic = true;

    private Config config;

    public JdkSemaphoreManager(Config config) {
        this.config = config;
    }

    public JdkSemaphoreManager() {
        this.config = new Config();
    }

    @Override
    public Semaphore getSemaphore(String name) {
        Semaphore semaphore = this.semaphoreMap.get(name);
        if (semaphore == null && this.dynamic) {
            synchronized (this.semaphoreMap) {
                semaphore = this.semaphoreMap.get(name);
                if (semaphore == null) {
                    semaphore = createJdkSemaphore(name);
                    this.semaphoreMap.put(name, semaphore);
                }
            }
        }
        return semaphore;
    }

    private Semaphore createJdkSemaphore(String name) {
        return new JdkSemaphore(name, this.config);
    }

    @Override
    public Collection<String> getSemaphoreNames() {
        return this.semaphoreMap.keySet();
    }
}
