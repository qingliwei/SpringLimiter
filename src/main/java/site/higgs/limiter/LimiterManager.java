package site.higgs.limiter;


import java.util.Collection;

/**
 * Created by caocg on 2018/9/14.
 */
public interface LimiterManager<T extends Limiter> {

    T getLimiter(String name);

    Collection<String> getLimiterNames();
}
