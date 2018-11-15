package site.higgs.limiter.ratelimiter;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Created by caocg on 2018/9/23.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface HRateLimiter {

    @AliasFor("rateLimiterName")
    String value() default "default";

    @AliasFor("value")
    String rateLimiterName() default "default";

    String[] keys() default {};

    double pps() default 1.0d;

    String keyGenerator() default "";

    String rateLimiterManager() default "";


    String condition() default "";

    String[] argInjecters() default {};

    String fallbackResolver() default "";
}
