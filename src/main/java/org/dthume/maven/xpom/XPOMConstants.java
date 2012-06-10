package org.dthume.maven.xpom;

public final class XPOMConstants {
    private XPOMConstants() {}
    
    public final static String BASE_NS = "urn:xpom";
    
    public final static String CORE_NS = BASE_NS + ":core";
    
    public final static String INTERNAL_NS = BASE_NS + ":internal";
    
    public static String xpomName(final String name) {
        return new StringBuilder("{")
            .append(CORE_NS)
            .append("}")
            .append(name)
            .toString();
    }
}
