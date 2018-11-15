package site.higgs.limiter.annotation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import site.higgs.limiter.interceptor.BeanFactoryLimiterOperationSourceAdvisor;
import site.higgs.limiter.interceptor.LimiterInterceptor;
import site.higgs.limiter.interceptor.LimiterOperationSource;
import site.higgs.limiter.lock.LockAnnotationParser;
import site.higgs.limiter.ratelimiter.RateLimiterAnnotationParser;
import site.higgs.limiter.semaphore.SemaphoreAnnotationParser;

import java.util.*;


@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ProxyLimiterConfiguration extends AbstractLimiterConfiguration {

    @Bean(name = "site.higgs.limiter.config.internalLimiterAdvisor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryLimiterOperationSourceAdvisor limiterAdvisor() {
        BeanFactoryLimiterOperationSourceAdvisor advisor =
                new BeanFactoryLimiterOperationSourceAdvisor();
        advisor.setLimiterOperationSource(limiterOperationSource());
        advisor.setAdvice(limiterInterceptor());
        if (this.enableLimiter != null) {
            advisor.setOrder(this.enableLimiter.<Integer>getNumber("order"));
        }
        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LimiterOperationSource limiterOperationSource() {
        String[] modules = this.enableLimiter.getStringArray("modules");
        if (modules == null || modules.length == 0) {
            return new AnnotationCacheOperationSource(new LockAnnotationParser(), new RateLimiterAnnotationParser(), new SemaphoreAnnotationParser());
        }
        Set<LimiterAnnotationParser> selected = new HashSet<>();
        for (String m : modules) {
            if ("lock".equals(m)) {
                selected.add(new LockAnnotationParser());
            }
            if ("rateLimiter".equals(m)) {
                selected.add(new RateLimiterAnnotationParser());
            }
            if ("semaphore".equals(m)) {
                selected.add(new SemaphoreAnnotationParser());
            }
        }

        return new AnnotationCacheOperationSource(selected);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LimiterInterceptor limiterInterceptor() {
        LimiterInterceptor interceptor = new LimiterInterceptor();
        interceptor.setLimiterOperationSource(limiterOperationSource());
        return interceptor;
    }

}
