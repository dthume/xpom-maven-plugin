package org.dthume.maven.xpom.impl;

import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.dthume.maven.xpom.api.EvaluationException;
import org.dthume.maven.xpom.api.ExpressionEvaluator;

public class DefaultExpressionEvaluator implements ExpressionEvaluator {
    private final PluginParameterExpressionEvaluator evaluator;

    public DefaultExpressionEvaluator(PluginParameterExpressionEvaluator eval) {
        this.evaluator = eval;
    }

    public Object evaluate(final String expr) {
        try {
            return evaluator.evaluate(expr);
        } catch (final ExpressionEvaluationException e) {
            throw new EvaluationException(e);
        }
    }
}
