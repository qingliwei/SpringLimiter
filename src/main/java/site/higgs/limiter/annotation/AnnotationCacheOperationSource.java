package site.higgs.limiter.annotation;

import org.springframework.util.Assert;
import site.higgs.limiter.interceptor.AbstractFallbackLimiterOperationSource;
import site.higgs.limiter.interceptor.LimiterOperation;
import site.higgs.limiter.lock.LockAnnotationParser;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;


public class AnnotationCacheOperationSource extends AbstractFallbackLimiterOperationSource implements Serializable {

    private final boolean publicMethodsOnly;

    private final Set<LimiterAnnotationParser> annotationParsers;


    public AnnotationCacheOperationSource() {
        this(true);
    }


    public AnnotationCacheOperationSource(boolean publicMethodsOnly) {
        this.publicMethodsOnly = publicMethodsOnly;
        this.annotationParsers = new LinkedHashSet<>(1);
        this.annotationParsers.add(new LockAnnotationParser());
    }

    public AnnotationCacheOperationSource(LimiterAnnotationParser annotationParser) {
        this.publicMethodsOnly = true;
        Assert.notNull(annotationParser, "LimiterAnnotationParser must not be null");
        this.annotationParsers = Collections.singleton(annotationParser);
    }


    public AnnotationCacheOperationSource(LimiterAnnotationParser... annotationParsers) {
        this.publicMethodsOnly = true;
        Assert.notEmpty(annotationParsers, "At least one LimiterAnnotationParser needs to be specified");
        Set<LimiterAnnotationParser> parsers = new LinkedHashSet<>(annotationParsers.length);
        Collections.addAll(parsers, annotationParsers);
        this.annotationParsers = parsers;
    }


    public AnnotationCacheOperationSource(Set<LimiterAnnotationParser> annotationParsers) {
        this.publicMethodsOnly = true;
        Assert.notEmpty(annotationParsers, "At least one LimiterAnnotationParser needs to be specified");
        this.annotationParsers = annotationParsers;
    }


    @Override

    protected Collection<LimiterOperation> findLimiterOperations(final Class<?> clazz) {
        return determineCacheOperations(parser -> parser.parseLimiterAnnotations(clazz));
    }

    @Override

    protected Collection<LimiterOperation> findLimiterOperations(final Method method) {
        return determineCacheOperations(parser -> parser.parseLimiterAnnotations(method));
    }



    protected Collection<LimiterOperation> determineCacheOperations(CacheOperationProvider provider) {
        Collection<LimiterOperation> ops = null;
        for (LimiterAnnotationParser annotationParser : this.annotationParsers) {
            Collection<LimiterOperation> annOps = provider.getCacheOperations(annotationParser);
            if (annOps != null) {
                if (ops == null) {
                    ops = new ArrayList<>();
                }
                ops.addAll(annOps);
            }
        }
        return ops;
    }

    @Override
    protected boolean allowPublicMethodsOnly() {
        return this.publicMethodsOnly;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AnnotationCacheOperationSource)) {
            return false;
        }
        AnnotationCacheOperationSource otherCos = (AnnotationCacheOperationSource) other;
        return (this.annotationParsers.equals(otherCos.annotationParsers) &&
                this.publicMethodsOnly == otherCos.publicMethodsOnly);
    }

    @Override
    public int hashCode() {
        return this.annotationParsers.hashCode();
    }


    @FunctionalInterface
    protected interface CacheOperationProvider {


        Collection<LimiterOperation> getCacheOperations(LimiterAnnotationParser parser);
    }

}
