package site.higgs.limiter;

import java.util.Map;

/**
 * Created by caocg on 2018/9/20.
 */
public interface Limiter {

    String getLimiterName();

    boolean limite(Object key, Map<String, Object> args);

    void unlimite(Object key, Map<String, Object> args);
}
