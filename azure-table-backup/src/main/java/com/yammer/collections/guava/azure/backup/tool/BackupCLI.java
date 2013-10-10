package com.yammer.collections.guava.azure.backup.tool;

import com.google.common.base.Optional;
import org.apache.commons.cli.HelpFormatter;

import java.io.PrintStream;
import java.io.PrintWriter;

public class BackupCLI {
    private final BackupCLIParser parser;
    private final PrintStream infoStream;
    private final PrintStream errorStream;


    public BackupCLI(BackupCLIParser parser, PrintStream info, PrintStream err) {
        this.parser = parser;
        this.infoStream = info;
        this.errorStream = err;
    }

    public static void main(String args[]) throws Exception {
        final BackupCLIParser parser = new BackupCLIParser(System.out, System.err);
        boolean success = (new BackupCLI(parser, System.out, System.err)).execute(args);

        if (!success) {
            System.exit(-1);
        }
    }

    private void printHelpAndExit() {
        PrintWriter pw = new PrintWriter(infoStream);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH, BackupCLI.class.getName(), null, BackupCLIParser.buildCommandLineParserOptions(),
                HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null, false);
        pw.flush();
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
            return parser.parse(args);
        } catch (Exception e) {
            errorStream.println(e.getMessage());
            return Optional.absent();
        }
    }

    public boolean execute(String args[]) throws Exception {
        Optional<BackupCommand> backupCommand = parse(args);
        if (backupCommand.isPresent()) {
            return executeBackupCommand(backupCommand.get());
        } else {
            printHelpAndExit();
            return false;
        }
    }

}
