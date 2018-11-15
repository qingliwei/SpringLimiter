package site.higgs.limiter.interceptor;

import org.springframework.util.Assert;
import site.higgs.limiter.Limiter;
import site.higgs.limiter.LimiterManager;

import java.util.Collection;
import java.util.Map;



public abstract class LimiterOperation<T extends Limiter> {


    private final String name;

    private final String limiterName;

    private final String keyGenerator;


    private final String limiterManager;

    private final String key;

    private final String condition;


    private final Collection<String> argumentInjecters;

    private final String fallbackResolver;


    private final Map<String, Object> customArgument;

    private final String errorHandler;

    protected LimiterOperation(LimiterOperation.Builder b) {
        this.name = b.name;
        this.limiterName = b.limiterName;
        this.key = b.key;
        this.keyGenerator = b.keyGenerator;
        this.limiterManager = b.limiterManager;
        this.condition = b.condition;
        this.argumentInjecters = b.argumentInjecters;
        this.fallbackResolver = b.fallbackResolver;
        this.customArgument = b.customArgument;
        this.errorHandler = b.errorHandler;
    }

    public Map<String, Object> getCustomArgument() {
        return customArgument;
    }

    public abstract Class<? extends LimiterManager<T>> getDefaultLimiterManagerClass();

    public String getErrorHandler() {
        return errorHandler;
    }

    public String getName() {
        return name;
    }

    public String getLimiterName() {
        return limiterName;
    }

    public String getKeyGenerator() {
        return keyGenerator;
    }


    public String getLimiterManager() {
        return limiterManager;
    }

    public String getKey() {
        return key;
    }

    public String getCondition() {
        return condition;
    }

    public String getFallbackResolver() {
        return fallbackResolver;
    }

    public Collection<String> getArgumentInjecters() {
        return argumentInjecters;
    }


    public abstract static class Builder {

        private String name = "";

        private String limiterName;

        private String key = "";

        private String keyGenerator = "";

        private String limiterManager = "";


        private String condition = "";

        private Collection<String> argumentInjecters;

        private String fallbackResolver;


        private Map<String, Object> customArgument;

        private String errorHandler;

        public void setName(String name) {
            Assert.hasText(name, "Name must not be empty");
            this.name = name;
        }

        public void setCustomArgument(Map<String, Object> customArgument) {
            this.customArgument = customArgument;
        }

        public void setLimiterName(String limiterName) {
            this.limiterName = limiterName;
        }


        public void setKey(String key) {
            Assert.notNull(key, "Key must not be null");
            this.key = key;
        }

        public void setArgumentInjecters(Collection<String> argumentInjecters) {
            this.argumentInjecters = argumentInjecters;
        }

        public String getErrorHandler() {
            return errorHandler;
        }

        public void setErrorHandler(String errorHandler) {
            this.errorHandler = errorHandler;
        }

        public String getKey() {
            return this.key;
        }


        public void setKeyGenerator(String keyGenerator) {
            Assert.notNull(keyGenerator, "KeyGenerator name must not be null");
            this.keyGenerator = keyGenerator;
        }

        public void setLimiterManager(String limiterManager) {
            Assert.notNull(limiterManager, "limiterManager name must not be null");
            this.limiterManager = limiterManager;
        }



        public void setCondition(String condition) {
            Assert.notNull(condition, "Condition must not be null");
            this.condition = condition;
        }

        public Collection<String> getArgumentInjecters() {
            return this.argumentInjecters;
        }

        public String getFallbackResolver() {
            return fallbackResolver;
        }

        public void setFallbackResolver(String fallbackResolver) {
            this.fallbackResolver = fallbackResolver;
        }

        public abstract LimiterOperation build();
    }

}
