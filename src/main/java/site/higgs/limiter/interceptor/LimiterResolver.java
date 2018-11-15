package site.higgs.limiter.interceptor;


import site.higgs.limiter.Limiter;


public interface LimiterResolver<T extends LimiterOperation> {

    Limiter resolveLimiter(LimiterOperationInvocationContext<T> limiterOperationInvocationContext);

}
