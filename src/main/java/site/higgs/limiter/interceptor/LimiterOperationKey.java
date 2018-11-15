package site.higgs.limiter.interceptor;

import org.springframework.context.expression.AnnotatedElementKey;

import java.lang.reflect.Method;


public class LimiterOperationKey implements Comparable<LimiterOperationKey> {

    private final LimiterOperation limiterOperation;
    private final AnnotatedElementKey methodCacheKey;

    public LimiterOperationKey(LimiterOperation limiterOperation, Method method, Class<?> targetClass) {
        this.limiterOperation = limiterOperation;
        this.methodCacheKey = new AnnotatedElementKey(method, targetClass);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof LimiterOperationKey)) {
            return false;
        } else {
            LimiterOperationKey otherKey = (LimiterOperationKey) other;
            return this.limiterOperation.equals(otherKey.limiterOperation) && this.methodCacheKey.equals(otherKey.methodCacheKey);
        }
    }

    public int hashCode() {
        return this.limiterOperation.hashCode() * 31 + this.methodCacheKey.hashCode();
    }

    public String toString() {
        return this.limiterOperation + " on " + this.methodCacheKey;
    }

    @Override
    public int compareTo(LimiterOperationKey o) {
        int result = this.limiterOperation.getName().compareTo(o.limiterOperation.getName());
        if (result == 0) {
            result = this.methodCacheKey.compareTo(o.methodCacheKey);
        }

        return result;
    }
}
