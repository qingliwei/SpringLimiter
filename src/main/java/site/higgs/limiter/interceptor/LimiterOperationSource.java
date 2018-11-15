package site.higgs.limiter.interceptor;

import java.lang.reflect.Method;
import java.util.Collection;


public interface LimiterOperationSource {


    Collection<LimiterOperation> getLimiterOperations(Method method,  Class<?> clazz);
}
