package site.higgs.limiter.ratelimiter;

import site.higgs.limiter.LimiterManager;
import site.higgs.limiter.interceptor.LimiterOperation;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by caocg on 2018/9/23.
 */
public class RateLimiterOperation extends LimiterOperation<RateLimiter> {

    protected RateLimiterOperation(RateLimiterOperation.Builder b) {
        super(b);

    }

    @Override
    public Class<? extends LimiterManager<RateLimiter>> getDefaultLimiterManagerClass() {
        return RateLimiterManager.class;
    }

    public static class Builder extends LimiterOperation.Builder {

        private double pps;

        public void setPps(double pps) {
            this.pps = pps;
        }

        public double getPps() {
            return pps;
        }

        @Override
        public RateLimiterOperation build() {
            Map<String, Object> customeArgs = new HashMap<>();
            customeArgs.put("pps", this.pps);
            this.setCustomArgument(customeArgs);
            return new RateLimiterOperation(this);
        }
    }
}
