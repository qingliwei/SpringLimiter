package site.higgs.limiter.interceptor;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import site.higgs.limiter.Limiter;
import site.higgs.limiter.LimiterManager;
import site.higgs.limiter.config.GlobalConfig;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class LimiterAspectSupport implements BeanFactoryAware, InitializingBean, SmartInitializingSingleton {

    protected final Log logger = LogFactory.getLog(getClass());

    private final LimiterOperationExpressionEvaluator evaluator = new LimiterOperationExpressionEvaluator();

    private final Map<LimiterOperationKey, LimiterOperationMetadata> metadataCache = new ConcurrentHashMap<>(1024);


    private BeanFactory beanFactory;

    private KeyGenerator keyGenerator = new SimpleKeyGenerator();


    private boolean initialized = false;



    private LimiterOperationSource limiterOperationSource;


    protected Object execute(final MethodInvocation invocation, Object target, Method method, Object[] args) throws Throwable {

        if (this.initialized) {
            Class<?> targetClass = getTargetClass(target);
            LimiterOperationSource limiterOperationSource = getLimiterOperationSource();
            if (limiterOperationSource != null) {
                Collection<LimiterOperation> operations = limiterOperationSource.getLimiterOperations(method, targetClass);
                if (!CollectionUtils.isEmpty(operations)) {
                    Collection<LimiterOperationContext> contexts = getLimiterOperationContexts(operations, method, args, target, targetClass);
                    LimitContextsValueWrapper limitContextsValueWrapper = limitContexts(contexts);
                    if (limitContextsValueWrapper.value()) {
                        try {
                            return invocation.proceed();
                        } catch (Throwable e) {
                            throw e;
                        } finally {
                            unlimitContexts(contexts);
                        }
                    } else {
                        return limitContextsValueWrapper.getLimiterFailResolveResult();
                    }

                }
            }
        }
        return invocation.proceed();
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.initialized = true;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    //-----------------------------------core-----------------------------
    protected LimitContextsValueWrapper limitContexts(Collection<LimiterOperationContext> contexts) {
        Collection<LimiterOperationContext> havLimited = new ArrayList<>();
        for (LimiterOperationContext context : contexts) {
            if (limitContext(context)) {
                havLimited.add(context);
            } else {
                unlimitContexts(havLimited);
                Object result = context.resolveFailLimiter();
                return new LimitContextsValueWrapper(false, result);
            }

        }
        return new LimitContextsValueWrapper(true, null);
    }

    protected void unlimitContexts(Collection<LimiterOperationContext> contexts) {
        if (contexts != null && !contexts.isEmpty()) {
            for (LimiterOperationContext context : contexts) {
                unlimitContext(context);
            }
        }
    }


    protected boolean limitContext(LimiterOperationContext limiterOperationContext) {
        Map<String, Object> injectArgs = limiterOperationContext.generateInjectArgs();
        if (isConditionPassing(limiterOperationContext, injectArgs)) {
            Object key = generateKey(limiterOperationContext, injectArgs);
            Limiter limiter = limiterOperationContext.getLimiter();
            Map<String, Object> customArgs = limiterOperationContext.getCustomArgument();
            ErrorHandler errorHandler = limiterOperationContext.getMetadata().getErrorHandler();
            return limit(limiter, key, customArgs, errorHandler);
        }
        return true;
    }

    protected void unlimitContext(LimiterOperationContext limiterOperationContext) {
        Map<String, Object> injectArgs = limiterOperationContext.generateInjectArgs();
        if (isConditionPassing(limiterOperationContext, injectArgs)) {
            Object key = generateKey(limiterOperationContext, injectArgs);
            Limiter limiter = limiterOperationContext.getLimiter();
            Map<String, Object> customArgs = limiterOperationContext.getCustomArgument();
            ErrorHandler errorHandler = limiterOperationContext.getMetadata().getErrorHandler();
            unlimit(limiter, key, customArgs, errorHandler);
        }
    }


    protected boolean limit(Limiter limiter, Object key, Map<String, Object> customArgument, ErrorHandler errorHandler) {
        try {
            return limiter.limite(key, customArgument);
        } catch (RuntimeException e) {
            return errorHandler.handleError(e);
        }
    }

    protected void unlimit(Limiter limiter, Object key, Map<String, Object> customArgument, ErrorHandler errorHandler) {
        try {
            limiter.unlimite(key, customArgument);
        } catch (RuntimeException e) {
            errorHandler.handleError(e);
        }
    }


    //---------------------------------core---------------------------------


    private Class<?> getTargetClass(Object target) {
        return AopProxyUtils.ultimateTargetClass(target);
    }

    /**
     * 封装contexts
     *
     * @param operartions
     * @param method
     * @param args
     * @param target
     * @param targetClass
     * @return
     */
    protected Collection<LimiterOperationContext> getLimiterOperationContexts(Collection<LimiterOperation> operartions, Method method, Object[] args, Object target, Class<?> targetClass) {
        Collection<LimiterOperationContext> retVal = new ArrayList<>();
        for (LimiterOperation operation : operartions) {
            retVal.add(getLimiterOperationContext(operation, method, args, target, targetClass));
        }
        return retVal;
    }


    /**
     * 封装context
     *
     * @param operation
     * @param method
     * @param args
     * @param target
     * @param targetClass
     * @return
     */
    protected LimiterOperationContext getLimiterOperationContext(LimiterOperation operation, Method method, Object[] args, Object target, Class<?> targetClass) {
        LimiterOperationMetadata metadata = getLimiterOperationMetadata(operation, method, targetClass);
        return new LimiterOperationContext(metadata, args, target, this.evaluator, this.beanFactory);
    }


    protected LimiterOperationMetadata getLimiterOperationMetadata(LimiterOperation operation, Method method, Class<?> targetClass) {

        LimiterOperationKey lockKey = new LimiterOperationKey(operation, method, targetClass);
        LimiterOperationMetadata metadata = this.metadataCache.get(lockKey);
        if (metadata == null) {
            GlobalConfig globalConfig = this.beanFactory.getBean(GlobalConfig.class);
            KeyGenerator operationKeyGenerator;
            if (StringUtils.hasText(operation.getKeyGenerator())) {
                operationKeyGenerator = getBean(operation.getKeyGenerator(), KeyGenerator.class);
            } else {
                operationKeyGenerator = this.keyGenerator;
            }
            LimiterResolver limiterResolver;
            if (StringUtils.hasText(operation.getLimiterManager())) {
                LimiterManager limiterManager = getBean(operation.getLimiterManager(), LimiterManager.class);
                limiterResolver = new SimpleLimiterResolver(limiterManager);
            } else {
                Class<LimiterManager> clazz = operation.getDefaultLimiterManagerClass();
                LimiterManager limiterManager = this.beanFactory.getBean(clazz);
                limiterResolver = new SimpleLimiterResolver(limiterManager);
            }
            Collection<ArgumentInjecter> argumentInjecters = new ArrayList<>();
            Collection<ArgumentInjecter> globalArgumentInjecters = globalConfig.globalArgumentInjecters();
            if (!CollectionUtils.isEmpty(globalArgumentInjecters)) {
                argumentInjecters.addAll(globalArgumentInjecters);
            }
            if (!CollectionUtils.isEmpty(operation.getArgumentInjecters())) {
                Collection<String> allInjecter = operation.getArgumentInjecters();
                for (String argInjecter : allInjecter) {
                    argumentInjecters.add(getBean(argInjecter, ArgumentInjecter.class));
                }
            }
            LimiterFallbackResolver limiterFallbackResolver;
            if (StringUtils.hasText(operation.getFallbackResolver())) {
                limiterFallbackResolver = getBean(operation.getFallbackResolver(), LimiterFallbackResolver.class);
            } else {
                limiterFallbackResolver = globalConfig.fallbackResolver();
            }
            ErrorHandler errorHandler;
            if (StringUtils.hasText(operation.getErrorHandler())) {
                errorHandler = getBean(operation.getErrorHandler(), ErrorHandler.class);
            } else {
                errorHandler = globalConfig.errorHandler();
            }
            metadata = new LimiterOperationMetadata(operation, method, targetClass,
                    operationKeyGenerator, limiterResolver, argumentInjecters, limiterFallbackResolver, errorHandler);
            this.metadataCache.put(lockKey, metadata);
        }
        return metadata;
    }

    /**
     * 条件判断
     *
     * @param context
     * @param injectArgs
     * @return
     */
    private boolean isConditionPassing(LimiterOperationContext context, Map<String, Object> injectArgs) {
        boolean passing = context.isConditionPassing(injectArgs);
        return passing;
    }


    /**
     * 生成限制键值
     *
     * @param context
     * @param injectArgs
     * @return
     */
    private Object generateKey(LimiterOperationContext context,  Map<String, Object> injectArgs) {
        Object key = context.generateKey(injectArgs);
        if (key == null) {
            throw new IllegalArgumentException("Null key returned for lock operation (maybe you are " +
                    "using named params on classes without debug info?) " + context.getLimiterOperation().getLimiterName());
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Computed lock key '" + key + "' for operation " + context.getLimiterOperation());
        }
        return key;
    }

    protected <T> T getBean(String beanName, Class<T> expectedType) {
        if (this.beanFactory == null) {
            throw new IllegalStateException("could not find beanFactory");
        }
        return BeanFactoryAnnotationUtils.qualifiedBeanOfType(this.beanFactory, expectedType, beanName);
    }

    public LimiterOperationSource getLimiterOperationSource() {
        return limiterOperationSource;
    }

    public void setLimiterOperationSource(LimiterOperationSource limiterOperationSource) {
        this.limiterOperationSource = limiterOperationSource;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(getLimiterOperationSource() != null, "");
    }
}
