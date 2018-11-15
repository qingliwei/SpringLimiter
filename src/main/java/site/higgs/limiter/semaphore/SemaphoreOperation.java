package site.higgs.limiter.semaphore;

import site.higgs.limiter.LimiterManager;
import site.higgs.limiter.interceptor.LimiterOperation;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by caocg on 2018/9/23.
 */
public class SemaphoreOperation extends LimiterOperation<Semaphore> {

    protected SemaphoreOperation(SemaphoreOperation.Builder b) {
        super(b);

    }

    @Override
    public Class<? extends LimiterManager<Semaphore>> getDefaultLimiterManagerClass() {
        return SemaphoreManager.class;
    }

    public static class Builder extends LimiterOperation.Builder {

        private int permits;

        public void setPermits(int permits) {
            this.permits = permits;
        }

        @Override
        public SemaphoreOperation build() {
            Map<String, Object> customeArgs = new HashMap<>();
            customeArgs.put("permits", this.permits);
            this.setCustomArgument(customeArgs);
            return new SemaphoreOperation(this);
        }
    }
}
