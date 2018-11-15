package site.higgs.limiter.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.io.Serializable;
import java.lang.reflect.Method;


public class LimiterInterceptor extends LimiterAspectSupport implements MethodInterceptor, Serializable {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        return execute(methodInvocation, methodInvocation.getThis(), method, methodInvocation.getArguments());

    }

}
