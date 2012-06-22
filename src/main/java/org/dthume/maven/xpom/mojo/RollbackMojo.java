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
package org.dthume.maven.xpom.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Rollback project POM transformation.
 * 
 * @requiresProject true
 * @goal rollback
 * 
 * @author dth
 */
public final class RollbackMojo extends AbstractBackupMojo {
    protected void executeLocally()
            throws MojoExecutionException, MojoFailureException {
        if (getBackupPOMFile().exists() && getProjectPOMFile().delete()) {
            if (getBackupPOMFile().renameTo(getProjectPOMFile()))
                getLog().info("Successfully rolled back project POM");
            else getLog().warn("Failed to rollback project POM");
        } else {
            getLog().warn("Backup file did not exist, or failed to delete POM");
        }
    }    
}
