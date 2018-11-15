package site.higgs.limiter.interceptor;



public interface ErrorHandler {

    boolean handleError(RuntimeException runtimeException);

}
