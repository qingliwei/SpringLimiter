package site.higgs.limiter.interceptor;

import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

abstract class LimiterOperationSourcePointcut extends StaticMethodMatcherPointcut implements Serializable {

    public LimiterOperationSourcePointcut() {
    }

    @Override
    public boolean matches(Method method, Class<?> aClass) {
        LimiterOperationSource limiterOperationSource = this.getLimiterOperationSource();
        boolean matched = limiterOperationSource != null && !CollectionUtils.isEmpty(limiterOperationSource.getLimiterOperations(method, aClass));
        if (matched == true) {
            return matched;
        }
        return matched;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof LimiterOperationSourcePointcut)) {
            return false;
        } else {
            LimiterOperationSourcePointcut otherPc = (LimiterOperationSourcePointcut) other;
            return ObjectUtils.nullSafeEquals(this.getLimiterOperationSource(), otherPc.getLimiterOperationSource());
        }
    }

    public int hashCode() {
        return LimiterOperationSourcePointcut.class.hashCode();
    }

    public String toString() {
        return this.getClass().getName() + ": " + this.getLimiterOperationSource();
    }

    protected abstract LimiterOperationSource getLimiterOperationSource();
}
