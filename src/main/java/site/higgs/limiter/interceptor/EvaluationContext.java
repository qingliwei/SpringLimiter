package site.higgs.limiter.interceptor;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;


public class EvaluationContext extends MethodBasedEvaluationContext {

    private final Set<String> unavailableVariables = new HashSet<>(1);


    public EvaluationContext(Object rootObject, Method method, Object[] arguments,
                             ParameterNameDiscoverer parameterNameDiscoverer) {

        super(rootObject, method, arguments, parameterNameDiscoverer);
    }


    public void addUnavailableVariable(String name) {
        this.unavailableVariables.add(name);
    }


    @Override
    public Object lookupVariable(String name) {
        if (this.unavailableVariables.contains(name)) {
            throw new VariableNotAvailableException(name);
        }
        return super.lookupVariable(name);
    }
}
