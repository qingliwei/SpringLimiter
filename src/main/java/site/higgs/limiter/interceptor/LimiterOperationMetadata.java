package site.higgs.limiter.interceptor;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.BridgeMethodResolver;
import site.higgs.limiter.Limiter;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;


public class LimiterOperationMetadata {

    private final LimiterOperation limiterOperation;
    private final Method method;
    private final Class<?> targetClass;
    private final Method targetMethod;
    private final AnnotatedElementKey methodKey;
    private final KeyGenerator keyGenerator;
    private final LimiterResolver limiterResolver;
    private final Collection<ArgumentInjecter> argumentInjecters;
    private final LimiterFallbackResolver limiterFallbackResolver;
    private final ErrorHandler errorHandler;

    public Object resolveLimiterFail(Object[] args) {
        return this.limiterFallbackResolver.resolve(method, targetClass, args, limiterOperation.getKey());
    }

    public LimiterOperationMetadata(LimiterOperation limiterOperation, Method method, Class<?> targetClass, KeyGenerator keyGenerator, LimiterResolver limiterResolver, Collection<ArgumentInjecter> argumentInjecters, LimiterFallbackResolver limiterFallbackResolver, ErrorHandler errorHandler) {
        this.limiterOperation = limiterOperation;
        this.method = BridgeMethodResolver.findBridgedMethod(method);
        this.targetClass = targetClass;
        this.targetMethod = !Proxy.isProxyClass(targetClass) ? AopUtils.getMostSpecificMethod(method, targetClass) : this.method;
        this.methodKey = new AnnotatedElementKey(this.targetMethod, targetClass);
        this.keyGenerator = keyGenerator;
        this.limiterResolver = limiterResolver;
        this.argumentInjecters = argumentInjecters;
        this.limiterFallbackResolver = limiterFallbackResolver;
        this.errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public Method getMethod() {
        return method;
    }

    public Limiter getLimiter(LimiterOperationContext context) {
        return this.limiterResolver.resolveLimiter(context);
    }

    public LimiterOperation getLimiterOperation() {
        return limiterOperation;
    }

    public AnnotatedElementKey getMethodKey() {
        return methodKey;
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Collection<ArgumentInjecter> getArgumentInjecters() {
        return argumentInjecters;
    }

    public Map<String, Object> getCustomArgument() {
        return this.getLimiterOperation().getCustomArgument();
    }
}
