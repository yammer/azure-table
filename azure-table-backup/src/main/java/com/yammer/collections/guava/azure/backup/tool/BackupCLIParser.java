package com.yammer.collections.guava.azure.backup.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Optional;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

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
    private static final Object CONST_DUMMY_ARG = "const"; // TODO this is a hack, as no access do documentation
    private final BackupServiceFactory backupServiceFactory;
    private final PrintStream infoStream;
    private final PrintStream errorStream;

    public BackupCLIParser(BackupServiceFactory backupServiceFactory, PrintStream infoStream, PrintStream errorStream) {
        this.backupServiceFactory = backupServiceFactory;
        this.infoStream = infoStream;
        this.errorStream = errorStream;
    }

    public void configureParser(ArgumentParser parser) {
        parser.addArgument(OPTION_DESIGNATOR + CONFIG_FILE_OPTION).
                required(true).
                metavar("config_file").
                help("config file with the details of the source and target backup accoutns");
        MutuallyExclusiveGroup mutexGroup = parser.addMutuallyExclusiveGroup();
        mutexGroup.
                addArgument(OPTION_DESIGNATOR + LIST_BACKUPS_OPTION).
                metavar("timestamp").
                type(Long.class).
                help("lists existing backups since timestamp");
        mutexGroup.
                addArgument(OPTION_DESIGNATOR + DELETE_BACKUP_OPTION).
                metavar("timestamp").
                type(Long.class).
                help("deletes all backups until timestamp");
        mutexGroup.addArgument(OPTION_DESIGNATOR + RESTORE_BACKUP_OPTION).
                metavar("timestamp").
                type(Long.class).
                help("restores the backup performed at the given timestamp");
        mutexGroup.
                addArgument(OPTION_DESIGNATOR + LIST_ALL_BACKUPS_OPTION).
                nargs("?").
                setConst(CONST_DUMMY_ARG).
                help("lists all backups");
        mutexGroup.
                addArgument(OPTION_DESIGNATOR + BACKUP_OPTION).
                nargs("?").
                setConst(CONST_DUMMY_ARG).
                help("perform a backup");
        mutexGroup.
                addArgument(OPTION_DESIGNATOR + DELETE_BAD_BACKUPS_OPTION).
                nargs("?").
                setConst(CONST_DUMMY_ARG).
                help("delete backups in bad state, i.e., not in COMPLETED");
    }

    public Optional<BackupCommand> constructBackupCommand(Namespace namespace) throws IOException, URISyntaxException, InvalidKeyException {
        BackupConfiguration backupConfiguration = getBackupConfiguration(namespace.getString(CONFIG_FILE_OPTION));

        BackupCommand backupCommand = null;
        if (hasOption(namespace, BACKUP_OPTION)) {
            backupCommand = new DoBackupCommand(backupServiceFactory.createBackupService(backupConfiguration), infoStream, errorStream);
        } else if (hasOption(namespace, LIST_BACKUPS_OPTION)) {
            backupCommand = new ListBackupsCommand(backupServiceFactory.createBackupService(backupConfiguration), infoStream,
                    errorStream, namespace.getLong(LIST_BACKUPS_OPTION));
        } else if (hasOption(namespace, LIST_ALL_BACKUPS_OPTION)) {
            backupCommand = new ListBackupsCommand(backupServiceFactory.createBackupService(backupConfiguration), infoStream,
                    errorStream, BEGINING_OF_TIME);
        } else if (hasOption(namespace, DELETE_BACKUP_OPTION)) {
            backupCommand = new DeleteBackupsCommand(backupServiceFactory.createBackupService(backupConfiguration), infoStream,
                    errorStream, namespace.getLong(DELETE_BACKUP_OPTION));
        } else if (hasOption(namespace, DELETE_BAD_BACKUPS_OPTION)) {
            backupCommand = new DeleteBadBackupsCommand(backupServiceFactory.createBackupService(backupConfiguration), infoStream,
                    errorStream);
        } else if (hasOption(namespace, RESTORE_BACKUP_OPTION)) {
            backupCommand = new RestoreFromBackupCommand(
                    backupServiceFactory.createBackupService(backupConfiguration),
                    backupConfiguration.getSourceTableName(),
                    infoStream,
                    errorStream,
                    namespace.getLong(RESTORE_BACKUP_OPTION));
        }

        return Optional.fromNullable(backupCommand);
    }

    private boolean hasOption(Namespace namespace, String option) {
        return namespace.get(option) != null;
    }

    private BackupConfiguration getBackupConfiguration(String configPath) throws IOException {
        final File configurationFile = new File(configPath);
        final ObjectMapper configurationObjectMapper = new ObjectMapper(new YAMLFactory());
        final JsonNode node = configurationObjectMapper.readTree(configurationFile);
        return configurationObjectMapper.readValue(new TreeTraversingParser(node), BackupConfiguration.class);
    }

}
