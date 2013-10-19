package com.yammer.collections.guava.azure.backup.tool;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class BackupCLI {
    private final BackupCLICommandUtil parser; // TODO rename


    public BackupCLI(BackupCLICommandUtil parser) {
        this.parser = parser;
    }

    public static void main(String args[]) throws Exception {
        final BackupCLICommandUtil parser = new BackupCLICommandUtil(new BackupServiceFactory(), System.out, System.err);
        new BackupCLI(parser).execute(args);
    }

    private BackupCommand parse(String args[]) throws ArgumentParserException {
        ArgumentParser argumentParser = ArgumentParsers.newArgumentParser("blah");// TODO rewrite this, pass in stuff here
        parser.configureParser(argumentParser);
        Namespace namespace = argumentParser.parseArgs(args);
        return parser.constructBackupCommand(namespace);
    }

    public void execute(String args[]) {
        try {
            parse(args).run();
        } catch (ArgumentParserException e) {
            e.printStackTrace();// TODO change around, to the tool
        }
    }

}
