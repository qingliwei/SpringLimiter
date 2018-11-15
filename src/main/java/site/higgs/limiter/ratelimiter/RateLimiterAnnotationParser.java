package site.higgs.limiter.ratelimiter;

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
public class RateLimiterAnnotationParser extends AbstractLimiterAnnotationParser<RateLimiter> {

    @Override
    public Collection<LimiterOperation<RateLimiter>> parseAnnotations(AnnotatedElement ae) {
        Collection<HRateLimiter> hRateLimiters = AnnotatedElementUtils.findAllMergedAnnotations(ae, HRateLimiter.class);

        if (CollectionUtils.isEmpty(hRateLimiters)) {
            return null;
        }
        Collection<LimiterOperation<RateLimiter>> retVal = new ArrayList<>();
        for (HRateLimiter hRateLimiter : hRateLimiters) {
            retVal.addAll(parseRateLimiterAnnotations(ae, hRateLimiter));
        }

        return retVal;
    }

    private Collection<LimiterOperation<RateLimiter>> parseRateLimiterAnnotations(AnnotatedElement ae, HRateLimiter hRateLimiter) {
        Collection<LimiterOperation<RateLimiter>> retVal = new ArrayList<>();
        for (int a = 0; a < hRateLimiter.keys().length; a++) {
            RateLimiterOperation.Builder builder = new RateLimiterOperation.Builder();
            builder.setName(ae.toString());
            builder.setLimiterName(hRateLimiter.rateLimiterName());
            builder.setCondition(hRateLimiter.condition());
            builder.setKey(hRateLimiter.keys()[a]);
            builder.setKeyGenerator(hRateLimiter.keyGenerator());
            builder.setLimiterManager(hRateLimiter.rateLimiterManager());
            String[] argInjects = hRateLimiter.argInjecters();
            Collection<String> argInjectCollection = new ArrayList<>();
            for (int i = 0; i < argInjects.length; i++) {
                argInjectCollection.add(argInjects[i]);
            }
            builder.setArgumentInjecters(argInjectCollection);
            builder.setFallbackResolver(hRateLimiter.fallbackResolver());
            builder.setPps(hRateLimiter.pps());
            retVal.add(builder.build());

        }
        return retVal;
    }
}
