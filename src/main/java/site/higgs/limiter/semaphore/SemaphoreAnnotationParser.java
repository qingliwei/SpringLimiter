package site.higgs.limiter.semaphore;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.CollectionUtils;
import site.higgs.limiter.annotation.AbstractLimiterAnnotationParser;
import site.higgs.limiter.interceptor.LimiterOperation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by caocg on 2018/9/23.
 */
public class SemaphoreAnnotationParser extends AbstractLimiterAnnotationParser<Semaphore> {

    @Override
    public Collection<LimiterOperation<Semaphore>> parseAnnotations(AnnotatedElement ae) {
        Collection<HSemaphore> hSemaphores = AnnotatedElementUtils.findAllMergedAnnotations(ae, HSemaphore.class);

        if (CollectionUtils.isEmpty(hSemaphores)) {
            return null;
        }
        Collection<LimiterOperation<Semaphore>> retVal = new ArrayList<>();
        for (HSemaphore hSemaphore : hSemaphores) {
            retVal.addAll(parseSemaphoreAnnotations(ae, hSemaphore));
        }

        return retVal;
    }

    private Collection<LimiterOperation<Semaphore>> parseSemaphoreAnnotations(AnnotatedElement ae, HSemaphore hSemaphore) {
        Collection<LimiterOperation<Semaphore>> retVal = new ArrayList<>();
        for (int a = 0; a < hSemaphore.keys().length; a++) {
            SemaphoreOperation.Builder builder = new SemaphoreOperation.Builder();
            builder.setName(ae.toString());
            builder.setLimiterName(hSemaphore.semaphoreName());
            builder.setCondition(hSemaphore.condition());
            builder.setKey(hSemaphore.keys()[a]);
            builder.setKeyGenerator(hSemaphore.keyGenerator());
            builder.setLimiterManager(hSemaphore.semaphoreManager());
            String[] argInjects = hSemaphore.argInjecters();
            Collection<String> argInjectCollection = new ArrayList<>();
            for (int i = 0; i < argInjects.length; i++) {
                argInjectCollection.add(argInjects[i]);
            }
            builder.setArgumentInjecters(argInjectCollection);
            builder.setFallbackResolver(hSemaphore.fallbackResolver());
            builder.setPermits(hSemaphore.permits());
            retVal.add(builder.build());
        }
        return retVal;
    }
}
