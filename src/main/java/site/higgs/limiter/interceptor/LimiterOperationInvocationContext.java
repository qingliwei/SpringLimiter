package site.higgs.limiter.interceptor;

import java.lang.reflect.Method;


public interface LimiterOperationInvocationContext<O extends LimiterOperation> {

    LimiterOperation getLimiterOperation();

    Object getTarget();

    Method getMethod();

    Object[] getArgs();
}
