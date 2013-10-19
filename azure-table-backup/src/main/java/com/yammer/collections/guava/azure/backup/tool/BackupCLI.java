package com.yammer.collections.guava.azure.backup.tool;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.net.URL;

public class BackupCLI {
    private final BackupCLICommandUtil backupCliCommandUtil;
    private final String usageString;


    public BackupCLI(String usageString, BackupCLICommandUtil backupCliCommandUtil) {
        this.usageString = usageString;
        this.backupCliCommandUtil = backupCliCommandUtil;
    }

    public static void main(String args[]) throws Exception {
        BackupCLICommandUtil backupCliCommandUtil = new BackupCLICommandUtil(
                new BackupServiceFactory(),
                System.out,
                System.err);
        new BackupCLI("java -jar " + getJarName(), backupCliCommandUtil).execute(args);
    }

    public static String getJarName() {
        URL location = BackupCLI.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            final String jar = new File(location.toURI()).getName();
            if (jar.endsWith(".jar")) {
                return jar;
            }
            return "project.jar";
        } catch (Exception ignored) {
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
