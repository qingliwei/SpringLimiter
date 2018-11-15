package site.higgs.limiter.interceptor;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;


public class BeanFactoryLimiterOperationSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {


    private LimiterOperationSource limiterOperationSource;

    private final LimiterOperationSourcePointcut pointcut = new LimiterOperationSourcePointcut() {
        @Override
        protected LimiterOperationSource getLimiterOperationSource() {
            return BeanFactoryLimiterOperationSourceAdvisor.this.limiterOperationSource;
        }
    };

    public BeanFactoryLimiterOperationSourceAdvisor() {
    }


    public void setLimiterOperationSource(LimiterOperationSource limiterOperationSource) {
        this.limiterOperationSource = limiterOperationSource;
    }


    public void setClassFilter(ClassFilter classFilter) {
        this.pointcut.setClassFilter(classFilter);
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }
}
