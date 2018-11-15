package site.higgs.limiter.interceptor;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import site.higgs.limiter.Limiter;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class LimiterOperationExpressionEvaluator extends ExpressionEvaluator {

    private final Map<ExpressionKey, Expression> keyCache = new ConcurrentHashMap<>(64);

    private final Map<ExpressionKey, Expression> conditionCache = new ConcurrentHashMap<>(64);

    private final Map<ExpressionKey, Expression> unlessCache = new ConcurrentHashMap<>(64);


    public EvaluationContext createEvaluationContext(Limiter limiter, Method method, Object[] args, Object target, Class<?> targetClass, Method targetMethod,
                                                     Map<String, Object> injectArgs, BeanFactory beanFactory) {

        LimiterExpressionRootObject rootObject = new LimiterExpressionRootObject(limiter, method, args, target, targetClass);
        EvaluationContext evaluationContext = new EvaluationContext(rootObject, targetMethod, args, getParameterNameDiscoverer());
        for (String key : injectArgs.keySet()) {
            evaluationContext.setVariable(key, injectArgs.get(key));
        }

        if (beanFactory != null) {
            evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        return evaluationContext;
    }

    public Object key(String keyExpression, AnnotatedElementKey methodKey, org.springframework.expression.EvaluationContext evalContext) {
        return getExpression(this.keyCache, methodKey, keyExpression).getValue(evalContext);
    }

    public boolean condition(String conditionExpression, AnnotatedElementKey methodKey, org.springframework.expression.EvaluationContext evalContext) {
        return (Boolean.TRUE.equals(getExpression(this.conditionCache, methodKey, conditionExpression).getValue(
                evalContext, Boolean.class)));
    }

    public boolean unless(String unlessExpression, AnnotatedElementKey methodKey, org.springframework.expression.EvaluationContext evalContext) {
        return (Boolean.TRUE.equals(getExpression(this.unlessCache, methodKey, unlessExpression).getValue(
                evalContext, Boolean.class)));
    }

    void clear() {
        this.keyCache.clear();
        this.conditionCache.clear();
        this.unlessCache.clear();
    }

}
