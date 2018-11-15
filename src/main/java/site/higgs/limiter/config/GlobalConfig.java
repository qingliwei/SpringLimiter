package site.higgs.limiter.config;

import site.higgs.limiter.interceptor.ArgumentInjecter;
import site.higgs.limiter.interceptor.ErrorHandler;
import site.higgs.limiter.interceptor.LimiterFallbackResolver;

import java.util.Collection;



public interface GlobalConfig {

    ErrorHandler errorHandler();

    LimiterFallbackResolver fallbackResolver();

    Collection<ArgumentInjecter> globalArgumentInjecters();

}
