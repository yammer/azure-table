package com.yammer.collections.guava.azure.backup.tool;

import com.google.common.base.Optional;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;

import java.io.PrintStream;

public class BackupCLI {
    private final BackupCLICommandUtil parser; // TODO rename
    private final PrintStream infoStream; // TODO is this needed?
    private final PrintStream errorStream;


    public BackupCLI(BackupCLICommandUtil parser, PrintStream infoStream, PrintStream errorStream) {
        this.parser = parser;
        this.infoStream = infoStream;
        this.errorStream = errorStream;
    }

    public static void main(String args[]) throws Exception {
        final BackupCLICommandUtil parser = new BackupCLICommandUtil(new BackupServiceFactory(), System.out, System.err);
        boolean failure = !new BackupCLI(parser, System.out, System.err).execute(args);

        if (failure) {
            System.exit(-1);
        }
    }

    private void printHelpAndExit() {
        // TODO format help output

    }

    private boolean executeBackupCommand(BackupCommand backupCommand) {
        try {
            backupCommand.run();
            return true;
        } catch (Exception e) {
            e.printStackTrace(errorStream);
            return false;
        }
    }

    private Optional<BackupCommand> parse(String args[]) {
        try {
            ArgumentParser argumentParser = ArgumentParsers.newArgumentParser("blah");// TODO rewrite this, pass in stuff here
            parser.configureParser(argumentParser);
            net.sourceforge.argparse4j.inf.Namespace namespace = argumentParser.parseArgs(args);
            return parser.constructBackupCommand(namespace);
        } catch (Exception e) {
            errorStream.println(e.getMessage());
            return Optional.absent();
        }
    }

    public boolean execute(String args[]) {
        Optional<BackupCommand> backupCommand = parse(args);
        if (backupCommand.isPresent()) {
            return executeBackupCommand(backupCommand.get());
        } else {
            printHelpAndExit();
            return false;
        }
    }

}
