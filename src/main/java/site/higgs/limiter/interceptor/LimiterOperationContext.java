package site.higgs.limiter.interceptor;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import site.higgs.limiter.Limiter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class LimiterOperationContext implements LimiterOperationInvocationContext<LimiterOperation> {
    private final LimiterOperationMetadata metadata;
    private final Object[] args;
    private final Object target;
    private final Limiter limiter;
    private final String limiterName;
    private final LimiterOperationExpressionEvaluator evaluator;

    private final BeanFactory beanFactory;


    public LimiterOperationContext(LimiterOperationMetadata metadata, Object[] args, Object target, LimiterOperationExpressionEvaluator evaluator, BeanFactory beanFactory) {
        this.metadata = metadata;
        this.args = this.extractArgs(metadata.getMethod(), args);
        this.target = target;
        this.limiter = metadata.getLimiter(this);
        this.limiterName = this.createLimiterName(this.limiter);
        this.beanFactory = beanFactory;
        this.evaluator = evaluator;
    }

    public Object resolveFailLimiter() {
        return metadata.resolveLimiterFail(this.args);
    }


    private Object[] extractArgs(Method method, Object[] args) {
        if (!method.isVarArgs()) {
            return args;
        } else {
            Object[] varArgs = ObjectUtils.toObjectArray(args[args.length - 1]);
            Object[] combinedArgs = new Object[args.length - 1 + varArgs.length];
            System.arraycopy(args, 0, combinedArgs, 0, args.length - 1);
            System.arraycopy(varArgs, 0, combinedArgs, args.length - 1, varArgs.length);
            return combinedArgs;
        }
    }

    private String createLimiterName(Limiter limiter) {
        return limiter.getLimiterName();
    }


    @Override
    public LimiterOperation getLimiterOperation() {
        return this.metadata.getLimiterOperation();
    }

    @Override
    public Object getTarget() {
        return this.target;
    }

    @Override
    public Method getMethod() {
        return this.metadata.getMethod();
    }

    @Override
    public Object[] getArgs() {
        return this.args;
    }


    public Map<String, Object> getCustomArgument() {
        return this.getLimiterOperation().getCustomArgument();
    }


    protected Object generateKey( Map<String, Object> injectArgs) {
        if (StringUtils.hasText(this.metadata.getLimiterOperation().getKey())) {
            EvaluationContext evaluationContext = createEvaluationContext(injectArgs);
            return evaluator.key(this.metadata.getLimiterOperation().getKey(), this.metadata.getMethodKey(), evaluationContext);
        }
        return this.metadata.getKeyGenerator().generate(this.target, this.metadata.getMethod(), this.args);
    }



    public Map<String, Object> generateInjectArgs() {
        Map<String, Object> retVal = new HashMap<>();
        if (this.metadata.getArgumentInjecters() == null || this.metadata.getArgumentInjecters().isEmpty()) {
            return retVal;
        }
        for (ArgumentInjecter argumentInjecter : this.metadata.getArgumentInjecters()) {
            Map<String, Object> tempMap = argumentInjecter.inject(this.args);
            if (!tempMap.isEmpty()) {
                retVal.putAll(tempMap);
            }
        }
        return retVal;
    }

    public LimiterOperationMetadata getMetadata() {
        return metadata;
    }

    private EvaluationContext createEvaluationContext( Map<String, Object> injectArgs) {
        return evaluator.createEvaluationContext(this.limiter, this.metadata.getMethod(), this.args,
                this.target, this.metadata.getTargetClass(), this.metadata.getTargetMethod(), injectArgs, beanFactory);
    }

    protected boolean isConditionPassing(Map<String, Object> injectArgs) {
        if (StringUtils.hasText(this.metadata.getLimiterOperation().getCondition())) {
            EvaluationContext evaluationContext = createEvaluationContext(injectArgs);
            return evaluator.condition(this.metadata.getLimiterOperation().getCondition(),
                    this.metadata.getMethodKey(), evaluationContext);
        }
        return true;
    }

    public Limiter getLimiter() {
        return limiter;
    }
}
