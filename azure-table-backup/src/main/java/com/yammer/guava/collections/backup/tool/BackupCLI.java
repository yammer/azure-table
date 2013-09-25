package com.yammer.guava.collections.backup.tool;

import com.google.common.base.Throwables;
import com.google.common.collect.Table;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.guava.collections.azure.StringAzureTable;
import org.apache.commons.cli.*;

// TODO proper azure table confiugration parsing is required for this tool, currently only test setup is being used
// TODO change to hash values for backup table names
public class BackupCLI {
    private static final Option CONFIG_FILE = OptionBuilder.
            withArgName("file").
            hasArg().
            withDescription("azure src and backup accounts configuration file").
            isRequired().
            create("cf");
    private static final Option LIST = OptionBuilder.
            withArgName("timestamp").
            hasArg().
            withDescription("lists existing backups since timestamp").
            create("l");
    private static final Option DELETE = OptionBuilder.
            withArgName("timestamp").
            hasArg().
            withDescription("deletes all backups until timestamp").
            create("d");
    private static final Option RESTORE = OptionBuilder.
            withArgName("timestamp").
            hasArg().
            withDescription("restores the backup performed at the given timestamp").
            create("r");
    private static final Option LIST_ALL = new Option("la", "list all backups");
    private static final Option BACKUP = new Option("b", "perform a backup");
    private static final Option DELETE_BAD_BACKUPS = new Option("db", "delete backups in bad state, i.e., not in COMPLETED");
    private static final OptionGroup BACKUP_OPTIONS = new OptionGroup().
            addOption(BACKUP).
            addOption(LIST).
            addOption(LIST_ALL).
            addOption(DELETE).
            addOption(RESTORE).
            addOption(DELETE_BAD_BACKUPS);
    private static final Options OPTIONS = setupOptions();


    private static Options setupOptions() {
        CONFIG_FILE.setRequired(true);
        BACKUP_OPTIONS.setRequired(true);

        return new Options().
                addOption(CONFIG_FILE).
                addOptionGroup(BACKUP_OPTIONS);
    }

    private static void printHelpAndExit() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(BackupCLI.class.getName(), OPTIONS);
        System.exit(-1);
    }

    private static void printHelpAndExit(Throwable e) {
        System.err.println(e.getMessage());
        printHelpAndExit();
    }

    private static CommandLine parse(String args[]) {
        PosixParser parser = new PosixParser();
        try {
            return parser.parse(OPTIONS, args);
        } catch (ParseException e) {
            printHelpAndExit(e);
            throw Throwables.propagate(e); // never happens
        }
    }

    public static void main(String args[]) throws Exception {
        CommandLine commandLine = parse(args);

        BackupToolCommand backupCommand = null;
        final Printer stdPrinter = new StdOutputsPrinter();
        final BackupConfiguration backupConfiguration = getBackupConfiguration(commandLine);
        if (commandLine.hasOption(BACKUP.getOpt())) {
            backupCommand = new DoBackupCommand(backupConfiguration, stdPrinter);
        } else if (commandLine.hasOption(LIST.getOpt())) {
            long timeSince = parseTimestamp(commandLine.getOptionValue(LIST.getOpt()));
            backupCommand = new ListBackups(backupConfiguration, stdPrinter, timeSince);
        } else if (commandLine.hasOption(LIST_ALL.getOpt())) {
            backupCommand = new ListBackups(backupConfiguration, stdPrinter, 0);
        } else if (commandLine.hasOption(DELETE.getOpt())) {
            long timeTill = parseTimestamp(commandLine.getOptionValue(DELETE.getOpt()));
            backupCommand = new DeleteBackups(backupConfiguration, stdPrinter, timeTill);
        } else if (commandLine.hasOption(DELETE_BAD_BACKUPS.getOpt())) {
            backupCommand = new DeleteBadBackups(backupConfiguration, stdPrinter);
        } else if (commandLine.hasOption(RESTORE.getOpt())) {
            long backupTime = parseTimestamp(commandLine.getOptionValue(RESTORE.getOpt()));
            backupCommand = new RestoreCommand(backupConfiguration, stdPrinter, backupTime);
        } else {
            printHelpAndExit();
        }

        try {
            backupCommand.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        //DEBUG_INFO();
    }

    private static BackupConfiguration getBackupConfiguration(CommandLine commandLine) {
        final String configPath = commandLine.getOptionValue(CONFIG_FILE.getOpt());
        // TODO parse
        BackupConfiguration configuration = new BackupConfiguration();
        configuration.setSourceTableName("backupToolValues");
        configuration.setSourceAccountName("secretietest");
        configuration.setSourceAccountKey("e5LnQoZei2cFH+56TFxDmO6AhnzMKill1NyVUs1M3R7OFNfCLnIGe17TLUex0mYYGQFjNvmArsLa8Iq3b0FNAg==");
        configuration.setBackupAccountName("secretietest");
        configuration.setBackupAccountKey("e5LnQoZei2cFH+56TFxDmO6AhnzMKill1NyVUs1M3R7OFNfCLnIGe17TLUex0mYYGQFjNvmArsLa8Iq3b0FNAg==");
        return configuration;
    }

    private static Long parseTimestamp(String data) {
        try {
            return Long.parseLong(data);
        } catch(NumberFormatException e) {
            System.err.println("Timestamp must be provided as a numeric value. Failed to read timestamp. "+e.getMessage());
            printHelpAndExit();
            return null;
        }
    }

    private static void DEBUG_INFO() {
        //todo DELETE this method

        System.out.println("\n\n DEBUG INFO \n");

        BackupConfiguration configuration = new BackupConfiguration();
        configuration.setSourceTableName("backupToolValues");
        configuration.setSourceAccountName("secretietest");
        configuration.setSourceAccountKey("e5LnQoZei2cFH+56TFxDmO6AhnzMKill1NyVUs1M3R7OFNfCLnIGe17TLUex0mYYGQFjNvmArsLa8Iq3b0FNAg==");
        configuration.setBackupAccountName("secretietest");
        configuration.setBackupAccountKey("e5LnQoZei2cFH+56TFxDmO6AhnzMKill1NyVUs1M3R7OFNfCLnIGe17TLUex0mYYGQFjNvmArsLa8Iq3b0FNAg==");

        try {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(configuration.getBackupConnectionString());
            CloudTableClient client = storageAccount.createCloudTableClient();
            System.out.println("\n\n === TABLES ===\n");
            for (String tableName : client.listTables()) {
                System.out.println(tableName);
            }

            System.out.println("\n\n === SOURCE ===\n");
            //CloudTable table = client.getTableReference(configuration.getSourceTableName());
            Table<String, String, String> azureTable = new StringAzureTable(configuration.getSourceTableName(), client);
            for (Table.Cell<String, String, String> tableCell : azureTable.cellSet()) {
                System.out.println(String.format("partKey=%s key=%s val=%s", tableCell.getRowKey(), tableCell.getColumnKey(), tableCell.getValue()));
            }

        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    private static final class StdOutputsPrinter implements Printer {

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
