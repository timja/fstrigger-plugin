package org.jenkinsci.plugins.fstrigger.service;

import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Node;
import hudson.remoting.Callable;
import org.jenkinsci.lib.envinject.EnvInjectException;
import org.jenkinsci.lib.envinject.service.EnvVarsResolver;
import org.jenkinsci.lib.xtrigger.XTriggerException;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.jenkinsci.plugins.fstrigger.triggers.FileNameTriggerInfo;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import org.jenkinsci.remoting.Role;
import org.jenkinsci.remoting.RoleChecker;


/**
 * @author Gregory Boissinot
 */
public class FSTriggerComputeFileService implements Serializable {

    /**
     * Gets the file to poll
     *
     * @param fileInfo
     * @return a FilePath object to the file, null if the object can't be determined or doesn't exist
     * @throws XTriggerException
     */
    public FilePath computedFile(Node node, AbstractProject project, final FileNameTriggerInfo fileInfo, final XTriggerLog log) throws XTriggerException {
        
        if (node == null) {
            throw new XTriggerException("A valid node must be set.");
        }
        FilePath rootPath = node.getRootPath();
        if (rootPath == null) {
            throw new XTriggerException("Node must be online.");
        }

        EnvVarsResolver varsRetriever = new EnvVarsResolver();
        try {
            final Map<String, String> envVars = varsRetriever.getPollingEnvVars(project, node);
            return rootPath.act(new Callable<FilePath, XTriggerException>() {
                @Override
                public void checkRoles(RoleChecker roleChecker) throws SecurityException {
                    roleChecker.check(this, Role.UNKNOWN_SET);
                }

                public FilePath call() throws XTriggerException {
                    File file = new FSTriggerFileNameRetriever(fileInfo, log, envVars).getFile();
                    if (file == null) {
                        return null;
                    }
                    return new FilePath(file);
                }
            });

        } catch (EnvInjectException e) {
            throw new XTriggerException(e);

        } catch (IOException e) {
            throw new XTriggerException(e);
        } catch (InterruptedException e) {
            throw new XTriggerException(e);
        }
    }

}
