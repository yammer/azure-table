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
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

public class BackupCLIParser {
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
    private static final Options COMMAND_LINE_OPTIONS = setupOptions();
    private static final long BEGINING_OF_TIME = 0l;

    private static Options setupOptions() { // TODO move up
        CONFIG_FILE.setRequired(true);
        BACKUP_OPTIONS.setRequired(true);

        return new Options().
                addOption(CONFIG_FILE).
                addOptionGroup(BACKUP_OPTIONS);
    }

    public BackupCLIParser(Printer printer) {
        this.printer = printer;
    }

    private final Printer printer;


    public Options getParseOptions() {
        return COMMAND_LINE_OPTIONS;
    }

    public Optional<BackupCommand> parse(String args[]) throws ParseException, IOException, URISyntaxException, InvalidKeyException {
        final CommandLine commandLine = parseCommandLine(args);
        final BackupConfiguration backupConfiguration = getBackupConfiguration(commandLine);

        BackupCommand backupCommand = null;
        if (commandLine.hasOption(BACKUP.getOpt())) {
            backupCommand = new DoBackupCommand(backupConfiguration, printer);
        } else if (commandLine.hasOption(LIST.getOpt())) {
            long timeSince = parseTimestamp(commandLine.getOptionValue(LIST.getOpt()));
            backupCommand = new ListBackupsCommand(backupConfiguration, printer, timeSince);
        } else if (commandLine.hasOption(LIST_ALL.getOpt())) {
            backupCommand = new ListBackupsCommand(backupConfiguration, printer, BEGINING_OF_TIME);
        } else if (commandLine.hasOption(DELETE.getOpt())) {
            long timeTill = parseTimestamp(commandLine.getOptionValue(DELETE.getOpt()));
            backupCommand = new DeleteBackupsCommand(backupConfiguration, printer, timeTill);
        } else if (commandLine.hasOption(DELETE_BAD_BACKUPS.getOpt())) {
            backupCommand = new DeleteBadBackupsCommand(backupConfiguration, printer);
        } else if (commandLine.hasOption(RESTORE.getOpt())) {
            long backupTime = parseTimestamp(commandLine.getOptionValue(RESTORE.getOpt()));
            backupCommand = new RestoreCommand(backupConfiguration, printer, backupTime);
        }

        return Optional.fromNullable(backupCommand);
    }

    private BackupConfiguration getBackupConfiguration(CommandLine commandLine) throws IOException {
        final String configPath = commandLine.getOptionValue(CONFIG_FILE.getOpt());
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
        return parser.parse(COMMAND_LINE_OPTIONS, args);
    }

}
