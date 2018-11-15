package site.higgs.limiter.semaphore;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Created by caocg on 2018/9/23.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface HSemaphore {


    @AliasFor("semaphoreName")
    String value() default "default";

    @AliasFor("value")
    String semaphoreName() default "default";

    String[] keys() default {};

    int permits() default 1;

    String keyGenerator() default "";

    String semaphoreManager() default "";

    String condition() default "";

    String[] argInjecters() default {};

    String fallbackResolver() default "";
}
