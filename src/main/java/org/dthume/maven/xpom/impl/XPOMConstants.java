/*
 * #%L
 * XPOM Maven Plugin
 * %%
 * Copyright (C) 2012 David Thomas Hume
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.dthume.maven.xpom.impl;

public final class XPOMConstants {
    private XPOMConstants() {}
    
    public final static String BASE_NS = "urn:xpom";
    
    public final static String CORE_NS = BASE_NS + ":core";
    
    public final static String INTERNAL_NS = BASE_NS + ":internal";
    
    public final static String SETTINGS_URI = BASE_NS + ":maven:settings";
    
    public static String xpomName(final String name) {
        return new StringBuilder("{")
            .append(CORE_NS)
            .append("}")
            .append(name)
            .toString();
    }
}
