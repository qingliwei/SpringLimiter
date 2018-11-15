package site.higgs.limiter.interceptor;

import site.higgs.limiter.Limiter;

import java.lang.reflect.Method;

public class LimiterExpressionRootObject {

    private final Limiter limiter;

    private final Method method;

    private final Object[] args;

    private final Object target;

    private final Class<?> targetClass;


    public LimiterExpressionRootObject(
            Limiter limiter, Method method, Object[] args, Object target, Class<?> targetClass) {

        this.method = method;
        this.target = target;
        this.targetClass = targetClass;
        this.args = args;
        this.limiter = limiter;
    }


    public Limiter getLimiter() {
        return limiter;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object getTarget() {
        return target;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }
}
