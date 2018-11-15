package site.higgs.limiter.lock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.CollectionUtils;
import site.higgs.limiter.annotation.AbstractLimiterAnnotationParser;
import site.higgs.limiter.interceptor.LimiterOperation;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Created by caocg on 2018/9/21.
 */
public class LockAnnotationParser extends AbstractLimiterAnnotationParser<Lock> {

    protected static final Log logger = LogFactory.getLog(LockAnnotationParser.class);

    @Override
    public Collection<LimiterOperation<Lock>> parseAnnotations(AnnotatedElement ae) {

        Collection<HLock> hLocks = AnnotatedElementUtils.findAllMergedAnnotations(ae, HLock.class);

        if (CollectionUtils.isEmpty(hLocks)) {
            return null;
        }
        Collection<LimiterOperation<Lock>> retVal = new ArrayList<>();
        for (HLock hLock : hLocks) {
            retVal.addAll(parseLockAnnotations(ae, hLock));
        }

        return retVal;
    }

    private Collection<LimiterOperation<Lock>> parseLockAnnotations(AnnotatedElement ae, HLock hLock) {
        Collection<LimiterOperation<Lock>> retVal = new ArrayList<>();
        for (int a = 0; a < hLock.keys().length; a++) {
            LockOperation.Builder builder = new LockOperation.Builder();
            builder.setName(ae.toString());
            builder.setLimiterName(hLock.lockName());
            builder.setCondition(hLock.condition());
            builder.setKey(hLock.keys()[a]);
            builder.setKeyGenerator(hLock.keyGenerator());
            builder.setLimiterManager(hLock.lockManager());
            String[] lockArgInjects = hLock.argInjecters();
            Collection<String> lockArgInjectCollection = new ArrayList<>();
            for (int i = 0; i < lockArgInjects.length; i++) {
                lockArgInjectCollection.add(lockArgInjects[i]);
            }
            builder.setArgumentInjecters(lockArgInjectCollection);
            builder.setFallbackResolver(hLock.fallbackResolver());
            retVal.add(builder.build());

        }
        return retVal;
    }
}
