package org.dthume.maven.xpom.api;


public class EvaluationException extends XPOMException {    

    private static final long serialVersionUID = 1L;

    public EvaluationException() {
        super();
    }

    public EvaluationException(final String arg0, final Throwable arg1) {
        super(arg0, arg1);
    }

    public EvaluationException(final String arg0) {
        super(arg0);
    }

    public EvaluationException(final Throwable arg0) {
        super(arg0);
    }
}
