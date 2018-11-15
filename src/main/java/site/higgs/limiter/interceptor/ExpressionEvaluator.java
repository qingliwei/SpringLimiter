package site.higgs.limiter.interceptor;

import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import java.util.Map;


public class ExpressionEvaluator {

    private final SpelExpressionParser parser;

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();



    protected ExpressionEvaluator(SpelExpressionParser parser) {
        Assert.notNull(parser, "SpelExpressionParser must not be null");
        this.parser = parser;
    }


    protected ExpressionEvaluator() {
        this(new SpelExpressionParser());
    }



    protected SpelExpressionParser getParser() {
        return this.parser;
    }


    protected ParameterNameDiscoverer getParameterNameDiscoverer() {
        return this.parameterNameDiscoverer;
    }



    protected Expression getExpression(Map<ExpressionKey, Expression> cache,AnnotatedElementKey elementKey, String expression) {

        ExpressionEvaluator.ExpressionKey expressionKey = createKey(elementKey, expression);
        Expression expr = cache.get(expressionKey);
        if (expr == null) {
            expr = getParser().parseExpression(expression);
            cache.put(expressionKey, expr);
        }
        return expr;
    }

    private ExpressionEvaluator.ExpressionKey createKey(AnnotatedElementKey elementKey, String expression) {
        return new ExpressionEvaluator.ExpressionKey(elementKey, expression);
    }


    protected static class ExpressionKey implements Comparable<ExpressionEvaluator.ExpressionKey> {

        private final AnnotatedElementKey element;

        private final String expression;

        protected ExpressionKey(AnnotatedElementKey element, String expression) {
            Assert.notNull(element, "AnnotatedElementKey must not be null");
            Assert.notNull(expression, "Expression must not be null");
            this.element = element;
            this.expression = expression;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ExpressionEvaluator.ExpressionKey)) {
                return false;
            }
            ExpressionEvaluator.ExpressionKey otherKey = (ExpressionEvaluator.ExpressionKey) other;
            return (this.element.equals(otherKey.element) &&
                    ObjectUtils.nullSafeEquals(this.expression, otherKey.expression));
        }

        @Override
        public int hashCode() {
            return this.element.hashCode() * 29 + this.expression.hashCode();
        }

        @Override
        public String toString() {
            return this.element + " with expression \"" + this.expression + "\"";
        }

        @Override
        public int compareTo(ExpressionEvaluator.ExpressionKey other) {
            int result = this.element.toString().compareTo(other.element.toString());
            if (result == 0) {
                result = this.expression.compareTo(other.expression);
            }
            return result;
        }
    }
}
