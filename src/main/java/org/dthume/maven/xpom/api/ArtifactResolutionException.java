package org.dthume.maven.xpom.api;


public class ArtifactResolutionException extends XPOMException {
    private static final long serialVersionUID = 1L;

    public ArtifactResolutionException() {
        super();
    }

    public ArtifactResolutionException(String arg0, Throwable arg1,
            boolean arg2, boolean arg3) {
        super(arg0, arg1, arg2, arg3);
    }

    public ArtifactResolutionException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public ArtifactResolutionException(String arg0) {
        super(arg0);
    }

    public ArtifactResolutionException(Throwable arg0) {
        super(arg0);
    }
}
