package com.yammer.collections.guava.azure.backup.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Optional;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
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
// todo migrate to argparse4j,
// TODO rename
class BackupCLIParser {
    private static final String CONFIG_FILE_OPTION = "cf";
    private static final String LIST_BACKUPS_OPTION = "l";
    private static final String DELETE_BACKUP_OPTION = "d";
    private static final String RESTORE_BACKUP_OPTION = "r";
    private static final String LIST_ALL_BACKUPS_OPTION = "la";
    private static final String BACKUP_OPTION = "b";
    private static final String DELETE_BAD_BACKUPS_OPTION = "db";
    private static final long BEGINING_OF_TIME = 0;
    private static final String OPTION_DESIGNATOR = "-";
    private final BackupServiceFactory backupServiceFactory;
    private final PrintStream infoStream;
    private final PrintStream errorStream;

    public BackupCLIParser(BackupServiceFactory backupServiceFactory, PrintStream infoStream, PrintStream errorStream) {
        this.backupServiceFactory = backupServiceFactory;
        this.infoStream = infoStream;
        this.errorStream = errorStream;
    }

    public static Options buildCommandLineParserOptions() {


        final Option listaAllOption = new Option(LIST_ALL_BACKUPS_OPTION, "list all backups");
        final Option backupOption = new Option(BACKUP_OPTION, "perform a backup");
        final Option deleteBadBackupsOption = new Option(DELETE_BAD_BACKUPS_OPTION, "delete backups in bad state, i.e., not in COMPLETED");
        final OptionGroup backupOptionsGroup = new OptionGroup().
                addOption(backupOption).
                addOption(listOption).
                addOption(listaAllOption).
                addOption(deleteOption).
                addOption(restoreOption).
                addOption(deleteBadBackupsOption);
        configFileOption.setRequired(true);
        backupOptionsGroup.setRequired(true);

        return new Options().
                addOption(configFileOption).
                addOptionGroup(backupOptionsGroup);
    }

    public static void configureParser(ArgumentParser parser) {
        parser.addArgument(OPTION_DESIGNATOR + CONFIG_FILE_OPTION).
                required(true).
                metavar("config_file").
                help("config file with the details of the source and target backup accoutns");
        MutuallyExclusiveGroup mutexGroup = parser.addMutuallyExclusiveGroup();
        mutexGroup.
                addArgument(OPTION_DESIGNATOR+LIST_BACKUPS_OPTION).
                required(true).
                metavar("timestamp").
                help("lists existing backups since timestamp");
        mutexGroup.
                addArgument(OPTION_DESIGNATOR+DELETE_BACKUP_OPTION).
                required(true).
                metavar("timestamp").
                help("deletes all backups until timestamp");
       mutexGroup.addArgument(OPTION_DESIGNATOR+RESTORE_BACKUP_OPTION).
               required(true).
               metavar("timestamp").
               help("restores the backup performed at the given timestamp");
        multimodule

        final Option listaAllOption = new Option(LIST_ALL_BACKUPS_OPTION, "list all backups");
        final Option backupOption = new Option(BACKUP_OPTION, "perform a backup");
        final Option deleteBadBackupsOption = new Option(DELETE_BAD_BACKUPS_OPTION, "delete backups in bad state, i.e., not in COMPLETED");




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
