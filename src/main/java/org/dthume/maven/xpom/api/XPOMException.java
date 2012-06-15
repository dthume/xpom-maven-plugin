package org.dthume.maven.xpom.api;

public class XPOMException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public XPOMException() {
        super();
    }

    public XPOMException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public XPOMException(String arg0) {
        super(arg0);
    }

    public XPOMException(Throwable arg0) {
        super(arg0);
    }
}
