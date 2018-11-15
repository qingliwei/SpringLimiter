package site.higgs.limiter.annotation;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.*;

import static org.springframework.context.annotation.AdviceMode.PROXY;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LimiterConfigurationSelector.class)
public @interface EnableLimiter {
    boolean proxyTargetClass() default false;

    int order() default Ordered.LOWEST_PRECEDENCE;

    String[] modules() default {};

    AdviceMode mode() default PROXY;
}
