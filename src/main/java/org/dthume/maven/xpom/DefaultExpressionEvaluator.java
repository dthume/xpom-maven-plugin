package org.dthume.maven.xpom;

import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

public class DefaultExpressionEvaluator implements ExpressionEvaluator {
    private final PluginParameterExpressionEvaluator evaluator;

    public DefaultExpressionEvaluator(PluginParameterExpressionEvaluator eval) {
        this.evaluator = eval;
    }

    public Object evaluate(final String expr) {
        try {
            return evaluator.evaluate(expr);
        } catch (final ExpressionEvaluationException e) {
            throw new RuntimeException(e); // FIXME
        }
    }
}
