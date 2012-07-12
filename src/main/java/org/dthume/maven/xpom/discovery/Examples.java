package org.dthume.maven.xpom.discovery;

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
