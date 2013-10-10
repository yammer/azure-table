package com.yammer.collections.guava.azure.backup.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Optional;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;


class BackupCLIParser {
    public static final String CONFIG_FILE_OPTION = "cf";
    public static final String LIST_BACKUPS_OPTION = "l";
    public static final String DELETE_BACKUP_OPTION = "d";
    public static final String RESTORE_BACKUP_OPTION = "r";
    public static final String LIST_ALL_BACKUPS_OPTION = "la";
    public static final String BACKUP_OPTION = "b";
    public static final String DELETE_BAD_BACKUPS_OPTION = "db";
    private static final long BEGINING_OF_TIME = 0;
    private final BackupServiceFactory backupServiceFactory;
    private final PrintStream infoStream;
    private final PrintStream errorStream;

    public BackupCLIParser(BackupServiceFactory backupServiceFactory, PrintStream infoStream, PrintStream errorStream) {
        this.backupServiceFactory = backupServiceFactory;
        this.infoStream = infoStream;
        this.errorStream = errorStream;
    }

    public static Options buildCommandLineParserOptions() {
        final Option CONFIG_FILE = OptionBuilder.
                withArgName("file").
                hasArg().
                withDescription("azure src and backup accounts configuration file").
                isRequired().
                create(CONFIG_FILE_OPTION);
        final Option LIST = OptionBuilder.
                withArgName("timestamp").
                hasArg().
                withDescription("lists existing backups since timestamp").
                create(LIST_BACKUPS_OPTION);
        final Option DELETE = OptionBuilder.
                withArgName("timestamp").
                hasArg().
                withDescription("deletes all backups until timestamp").
                create(DELETE_BACKUP_OPTION);
        final Option RESTORE = OptionBuilder.
                withArgName("timestamp").
                hasArg().
                withDescription("restores the backup performed at the given timestamp").
                create(RESTORE_BACKUP_OPTION);
        final Option LIST_ALL = new Option(LIST_ALL_BACKUPS_OPTION, "list all backups");
        final Option BACKUP = new Option(BACKUP_OPTION, "perform a backup");
        final Option DELETE_BAD_BACKUPS = new Option(DELETE_BAD_BACKUPS_OPTION, "delete backups in bad state, i.e., not in COMPLETED");
        final OptionGroup BACKUP_OPTIONS = new OptionGroup().
                addOption(BACKUP).
                addOption(LIST).
                addOption(LIST_ALL).
                addOption(DELETE).
                addOption(RESTORE).
                addOption(DELETE_BAD_BACKUPS);
        CONFIG_FILE.setRequired(true);
        BACKUP_OPTIONS.setRequired(true);

        return new Options().
                addOption(CONFIG_FILE).
                addOptionGroup(BACKUP_OPTIONS);
    }

    public Optional<BackupCommand> parse(String args[]) throws ParseException, IOException, URISyntaxException, InvalidKeyException {
        final CommandLine commandLine = parseCommandLine(args);
        final BackupConfiguration backupConfiguration = getBackupConfiguration(commandLine.getOptionValue(CONFIG_FILE_OPTION));

        BackupCommand backupCommand = null;
        if (commandLine.hasOption(BACKUP_OPTION)) {
            backupCommand = new DoBackupCommand(backupServiceFactory.createBackupService(backupConfiguration), infoStream, errorStream);
        } else if (commandLine.hasOption(LIST_BACKUPS_OPTION)) {
            long timeSince = parseTimestamp(commandLine.getOptionValue(LIST_BACKUPS_OPTION));
            backupCommand = new ListBackupsCommand(backupServiceFactory.createBackupService(backupConfiguration), infoStream,
                    errorStream, timeSince);
        } else if (commandLine.hasOption(LIST_ALL_BACKUPS_OPTION)) {
            backupCommand = new ListBackupsCommand(backupServiceFactory.createBackupService(backupConfiguration), infoStream,
                    errorStream, BEGINING_OF_TIME);
        } else if (commandLine.hasOption(DELETE_BACKUP_OPTION)) {
            long timeTill = parseTimestamp(commandLine.getOptionValue(DELETE_BACKUP_OPTION));
            backupCommand = new DeleteBackupsCommand(backupServiceFactory.createBackupService(backupConfiguration), infoStream,
                    errorStream, timeTill);
        } else if (commandLine.hasOption(DELETE_BAD_BACKUPS_OPTION)) {
            backupCommand = new DeleteBadBackupsCommand(backupServiceFactory.createBackupService(backupConfiguration), infoStream,
                    errorStream);
        } else if (commandLine.hasOption(RESTORE_BACKUP_OPTION)) {
            long backupTime = parseTimestamp(commandLine.getOptionValue(RESTORE_BACKUP_OPTION));
            backupCommand = new RestoreFromBackupCommand(
                    backupServiceFactory.createBackupService(backupConfiguration),
                    backupConfiguration.getSourceTableName(),
                    infoStream,
                    errorStream,
                    backupTime);
        }

        return Optional.fromNullable(backupCommand);
    }

    private BackupConfiguration getBackupConfiguration(String configPath) throws IOException {
        final File configurationFile = new File(configPath);
        final ObjectMapper configurationObjectMapper = new ObjectMapper(new YAMLFactory());
        final JsonNode node = configurationObjectMapper.readTree(configurationFile);
        return configurationObjectMapper.readValue(new TreeTraversingParser(node), BackupConfiguration.class);
    }

    private Long parseTimestamp(String data) throws ParseException {
        try {
            return Long.parseLong(data);
        } catch (NumberFormatException e) {
            throw new ParseException("Timestamp must be provided as a numeric value. Failed to read timestamp. " + e.getMessage());
        }
    }

    private CommandLine parseCommandLine(String args[]) throws ParseException {
        PosixParser parser = new PosixParser();
        return parser.parse(buildCommandLineParserOptions(), args);
    }

}
