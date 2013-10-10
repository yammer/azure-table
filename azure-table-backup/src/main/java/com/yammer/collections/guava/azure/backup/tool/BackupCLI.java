package com.yammer.collections.guava.azure.backup.tool;

import com.google.common.base.Optional;
import org.apache.commons.cli.HelpFormatter;

import java.io.PrintStream;

public class BackupCLI {
    private final PrintStream infoStream;
    private final PrintStream errorStream;


    public BackupCLI(PrintStream info, PrintStream err) {
        this.infoStream = info;
        this.errorStream = err;
    }

    public static void main(String args[]) throws Exception {
        boolean success = (new BackupCLI(System.out, System.err)).execute(args);

        if(!success) {
            System.exit(-1);
        }
    }

    private void printHelpAndExit() { // TODO solve this, need to provide input
        // this is just a pain, help formatter has a hard
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(BackupCLI.class.getName(), BackupCLIParser.buildCommandLineParserOptions());
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

    private BackupCLIParser createBackupCLIParser() {
        PrintStreamPrinter printer = new PrintStreamPrinter(infoStream, errorStream);
        return new BackupCLIParser(printer);
    }

    private Optional<BackupCommand> parse(String args[]) {
        try {
            return createBackupCLIParser().parse(args);
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

    private final static class PrintStreamPrinter implements Printer {
        private final PrintStream infoPrintStream;
        private final PrintStream errorPrintStream;

        public PrintStreamPrinter(PrintStream infoPrintStream, PrintStream errorPrintStream) {
            this.infoPrintStream = infoPrintStream;
            this.errorPrintStream = errorPrintStream;
        }


        @Override
        public void println(String string) {
            infoPrintStream.println(string);
        }

        @Override
        public void printErrorln(String string) {
            errorPrintStream.println(string);
        }
    }

}
