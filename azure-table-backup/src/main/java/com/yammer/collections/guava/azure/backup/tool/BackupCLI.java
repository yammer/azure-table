package com.yammer.collections.guava.azure.backup.tool;

import com.google.common.base.Optional;
import org.apache.commons.cli.HelpFormatter;

public class BackupCLI {
    private final BackupCLIParser commandParser;


    public BackupCLI(BackupCLIParser commandParser) {
        this.commandParser = commandParser;
    }

    public static void main(String args[]) throws Exception {
        StdOutputsPrinter printer = new StdOutputsPrinter();
        BackupCLIParser parser = new BackupCLIParser(printer);
        (new BackupCLI(parser)).execute(args);
    }

    private void printHelpAndExit() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(BackupCLI.class.getName(), commandParser.getParseOptions());
        System.exit(-1);
    }

    private void executeBackupCommand(BackupCommand backupCommand) {
        final long startTime = System.currentTimeMillis();
        long duration = 0;
        try {
            backupCommand.run();
            duration = System.currentTimeMillis() - startTime;
        } catch (Exception e) {
            duration = System.currentTimeMillis() - startTime;
            e.printStackTrace();
            System.exit(-1);
        } finally {
            System.out.println("Running time was: " + duration + "[ms]");
        }
    }

    private Optional<BackupCommand> parse(String args[]) {
        try {
            return commandParser.parse(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return Optional.absent();
        }
    }

    public void execute(String args[]) throws Exception {
        Optional<BackupCommand> backupCommand = parse(args);
        if (backupCommand.isPresent()) {
            executeBackupCommand(backupCommand.get());
        } else {
            printHelpAndExit();
        }
    }

    private final static class StdOutputsPrinter implements Printer {
        // TODO make this stream dependant
        @Override
        public void println(String string) {
            System.out.println(string);
        }

        @Override
        public void printErrorln(String string) {
            System.err.println(string);
        }
    }

}
