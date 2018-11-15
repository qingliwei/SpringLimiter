package site.higgs.limiter.annotation;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class LimiterConfigurationSelector  extends AdviceModeImportSelector<EnableLimiter> {
    @Override
    protected String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode) {
            case PROXY:
                return getProxyImports();
            case ASPECTJ:
                throw new RuntimeException("NotImplemented");
            default:
                return null;
        }
    }

    private String[] getProxyImports() {
        List<String> result = new ArrayList<>();
        result.add(AutoProxyRegistrar.class.getName());
        result.add(ProxyLimiterConfiguration.class.getName());
        return StringUtils.toStringArray(result);
    }



}
