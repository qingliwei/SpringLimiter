package site.higgs.limiter.interceptor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import site.higgs.limiter.Limiter;
import site.higgs.limiter.LimiterManager;


public abstract class  AbstractLimiterResolver<T extends LimiterOperation> implements LimiterResolver<T>, InitializingBean {


    private LimiterManager limiterManager;

    protected AbstractLimiterResolver() {
    }

    protected AbstractLimiterResolver(LimiterManager limiterManager) {
        this.limiterManager = limiterManager;
    }

    public LimiterManager getLimiterManager() {
        Assert.state(this.limiterManager != null, "No LockManager set");
        return limiterManager;
    }

    public void setLockManager(LimiterManager limiterManager) {
        this.limiterManager = limiterManager;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.limiterManager, "LimiterManage is required");
    }


    @Override
    public Limiter resolveLimiter(LimiterOperationInvocationContext<T> var1) {
        String limiterName = getLimiterName(var1);
        return getLimiterManager().getLimiter(limiterName);

    }

    protected abstract String getLimiterName(LimiterOperationInvocationContext<?> context);

}
