package org.dthume.maven.xpom.discovery;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface Fact {
    String value();
}
