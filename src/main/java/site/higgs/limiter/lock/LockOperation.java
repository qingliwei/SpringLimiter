package site.higgs.limiter.lock;

import site.higgs.limiter.LimiterManager;
import site.higgs.limiter.interceptor.LimiterOperation;

/**
 * Created by caocg on 2018/9/21.
 */
public class LockOperation extends LimiterOperation<Lock> {

    public LockOperation(Builder b) {
        super(b);
    }


    @Override
    public Class<? extends LimiterManager<Lock>> getDefaultLimiterManagerClass() {

        return LockManager.class;
    }

    public static class Builder extends LimiterOperation.Builder {


        @Override
        public LockOperation build() {
            return new LockOperation(this);
        }
    }

}
