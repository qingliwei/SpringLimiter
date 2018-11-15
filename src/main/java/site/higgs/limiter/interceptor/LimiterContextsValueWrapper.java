package site.higgs.limiter.interceptor;


public class LimiterContextsValueWrapper {

    boolean value;

    private Object failLimiterResolverResult;

    public LimiterContextsValueWrapper(boolean value, Object failLimiterResolverResult) {
        this.value = value;
        this.failLimiterResolverResult = failLimiterResolverResult;
    }

    public boolean value() {
        return value;
    }

    public Object getFailLimiterResolverResult() {
        return failLimiterResolverResult;
    }
}
