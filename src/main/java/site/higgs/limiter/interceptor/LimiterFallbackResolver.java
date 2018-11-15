package site.higgs.limiter.interceptor;

import java.lang.reflect.Method;


public interface LimiterFallbackResolver<T> {

    T resolve(Method method, Class<?> clazz, Object[] args, String key);
}
