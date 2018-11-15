package site.higgs.limiter.annotation;

import site.higgs.limiter.Limiter;
import site.higgs.limiter.interceptor.LimiterOperation;
import java.lang.reflect.Method;
import java.util.Collection;


public interface LimiterAnnotationParser<T extends Limiter> {


    Collection<LimiterOperation<T>> parseLimiterAnnotations(Class<?> clazz);

    Collection<LimiterOperation<T>> parseLimiterAnnotations(Method method);

}
