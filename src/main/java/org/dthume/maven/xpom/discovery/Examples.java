package org.dthume.maven.xpom.discovery;/*
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


import java.io.File;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

public class Examples {
    @Discover("os")
    class OSAgent {        
        @Discover("type")
        public Object discoverType() {
            return "todo - type";
        }
        
        @Discover("version")
        @Requires(@Fact("os/type = 'windows'"))
        public String discoverWindowsVersion() {
            return "todo - version";
        }
        
    }
    
    @Discover("kana/sem")
    @Requires(@Fact("exists(wps)"))
    class SEMAgent {
        @Inject
        private WPSInstance wpsInstanceService;
    }
    
    @Component("wpsInstance")
    @Requires(@Fact("exists(wps)"))
    class WPSInstance {
        @Inject
        @Expression("wps/homedir")
        private File wpsHome;
        
    }
    
    final static String ARTIFACT_URI =
            "xpom://group.id/artifact-id/extension/version/no;classifier/some/file/in/archive.xml";
}
