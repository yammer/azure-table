/**
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED WARRANTIES OR CONDITIONS
 * OF TITLE, FITNESS FOR A PARTICULAR PURPOSE, MERCHANTABLITY OR NON-INFRINGEMENT.
 *
 * See the Apache Version 2.0 License for specific language governing permissions and limitations under
 * the License.
 */
package com.yammer.collections.azure.backup.tool;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class BackupCLI {
    private final BackupCLICommandUtil backupCliCommandUtil;
    private final String usageString;


    public BackupCLI(String usageString, BackupCLICommandUtil backupCliCommandUtil) {
        this.usageString = usageString;
        this.backupCliCommandUtil = backupCliCommandUtil;
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(String args[]) {
        BackupCLICommandUtil backupCliCommandUtil = new BackupCLICommandUtil(
                new BackupServiceFactory(),
                System.out,
                System.err);
        new BackupCLI("java -jar " + getJarName(), backupCliCommandUtil).execute(args);
    }

    public static String getJarName() {
        URL location = BackupCLI.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            String jar = new File(location.toURI()).getName();
            if (jar.endsWith(".jar")) {
                return jar;
            }
            return "project.jar";
        } catch (URISyntaxException ignored) {
            return "project.jar";
        }
    }

    public void execute(String args[]) {
        ArgumentParser argumentParser = ArgumentParsers.newArgumentParser(usageString);
        try {
            backupCliCommandUtil.configureParser(argumentParser);
            Namespace namespace = argumentParser.parseArgs(args);
            backupCliCommandUtil.constructBackupCommand(namespace).run();
        } catch (ArgumentParserException e) {
            argumentParser.handleError(e);
        }
    }

}
