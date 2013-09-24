package com.yammer.guava.collections.backup.tool;

import com.google.common.base.Throwables;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.guava.collections.backup.azure.AzureBackupTableFactory;
import com.yammer.guava.collections.backup.azure.AzureSourceTableFactory;
import com.yammer.guava.collections.backup.lib.*;
import org.apache.commons.cli.*;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

// TODO restore should clear the table prior to restoring
public class BackupCLI {
    private static final Option CONFIG_FILE = new Option("cf", true, "azure account configuration");
    private static final Option BACKUP = new Option("b", "perform a backup");
    private static final Option LIST = new Option("l", "list existing backups");
    private static final OptionGroup BACKUP_OPTIONS = new OptionGroup().
            addOption(BACKUP).
            addOption(LIST);

    {
        CONFIG_FILE.setRequired(true);
        BACKUP_OPTIONS.setRequired(true);
    }

    private static final Options OPTIONS = new Options().
            addOption(CONFIG_FILE).
            addOptionGroup(BACKUP_OPTIONS);

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
        final String configPath = commandLine.getOptionValue(CONFIG_FILE.getOpt());
        if (commandLine.hasOption(BACKUP.getOpt())) {
            backupCommand = new DoBackupCommand(configPath);
        } else if (commandLine.hasOption(LIST.getOpt())) {
            backupCommand = new ListBackups(configPath, 0); // TODO parse time
        } else {
            printHelpAndExit();
        }

        try {
            backupCommand.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    private static class DoBackupCommand extends BackupToolCommand {

        private DoBackupCommand(String configPath) throws Exception {
            super(configPath);
        }

        @Override
        public void run() {
            getBackupService().backup();
        }
    }

    private static class ListBackups extends BackupToolCommand {
        private final DateFormat dateFormat = new SimpleDateFormat();
        private final Date thresholdDate;


        private ListBackups(String configPath, long time) throws Exception {
            super(configPath);
            this.thresholdDate = new Date(time);
        }

        @Override
        public void run() throws Exception {
            Collection<Backup> backups = getBackupService().listAllBackups(thresholdDate);
            for (Backup backup : backups) {
                System.out.println(format(backup));
            }
        }

        private String format(Backup backup) {
            return String.format("Backup: NAME=%s DATE=&s STATUS=%s", backup.getName(), dateFormat.format(backup.getDate()), backup.getStatus());
        }
    }

    private static abstract class BackupToolCommand {
        private final BackupService backupService;

        protected BackupToolCommand(String configPath) throws Exception {
            BackupConfiguration configuration = parseConfiguration(configPath);
            backupService = createBackupService(configuration);
        }

        private static BackupConfiguration parseConfiguration(String configPath) {
            // TODO add parsing
            BackupConfiguration configuration = new BackupConfiguration();
            configuration.setSourceTableName("integrationTestSecrets");
            configuration.setSourceAccountName("secretietest");
            configuration.setSourceAccountKey("e5LnQoZei2cFH+56TFxDmO6AhnzMKill1NyVUs1M3R7OFNfCLnIGe17TLUex0mYYGQFjNvmArsLa8Iq3b0FNAg==");
            configuration.setBackupAccountName("secretietest");
            configuration.setBackupAccountKey("e5LnQoZei2cFH+56TFxDmO6AhnzMKill1NyVUs1M3R7OFNfCLnIGe17TLUex0mYYGQFjNvmArsLa8Iq3b0FNAg==");
            return configuration;
        }

        private static CloudTableClient createCloudTableClient(String connectionString) throws URISyntaxException, InvalidKeyException {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
            return storageAccount.createCloudTableClient();
        }

        private static BackupTableFactory getBackupTableFactory(BackupConfiguration configuration) throws URISyntaxException, InvalidKeyException {
            CloudTableClient tableClient = createCloudTableClient(configuration.getBackupConnectionString());
            return new AzureBackupTableFactory(tableClient);
        }

        private static SourceTableFactory getSourceTableFactory(BackupConfiguration configuration) throws URISyntaxException, InvalidKeyException {
            CloudTableClient tableClient = createCloudTableClient(configuration.getSourceConnectionString());
            return new AzureSourceTableFactory(tableClient, configuration.getSourceTableName());
        }

        private static BackupService createBackupService(BackupConfiguration configuration) throws URISyntaxException, InvalidKeyException {
            BackupTableFactory backupTableFactory = getBackupTableFactory(configuration);
            SourceTableFactory sourceTableFactory = getSourceTableFactory(configuration);
            TableCopy<String, String, String> tableCopy = new TableCopy<>();
            return new BackupService(tableCopy, sourceTableFactory, backupTableFactory);
        }

        protected final BackupService getBackupService() {
            return backupService;
        }

        public abstract void run() throws Exception;

    }

}
