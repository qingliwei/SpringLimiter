package site.higgs.limiter.interceptor;

import site.higgs.limiter.LimiterManager;



public class SimpleLimiterResolver<T extends LimiterOperation> extends AbstractLimiterResolver {

    @Override
    protected String getLimiterName(LimiterOperationInvocationContext context) {
        return context.getLimiterOperation().getLimiterName();

    }

    public SimpleLimiterResolver() {
    }

    public SimpleLimiterResolver(LimiterManager limiterManager) {
        super(limiterManager);
    }

}
