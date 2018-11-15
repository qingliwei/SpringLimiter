package site.higgs.limiter.lock;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Created by caocg on 2018/9/21.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface HLock {


    @AliasFor("lockName")
    String value() default "default";


    @AliasFor("value")
    String lockName() default "default";


    String[] keys() default {};


    String keyGenerator() default "";

    String lockManager() default "";



    String condition() default "";

    String[] argInjecters() default {};

    String fallbackResolver() default "";
    
}
