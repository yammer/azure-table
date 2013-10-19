package com.yammer.collections.guava.azure.backup.tool;


import com.google.common.base.Optional;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class BackupCLICommandUtilTest {
    private static final String CONFIG_FILE_PATH = BackupCLICommandUtilTest.class.getResource("testBackupAccountConfiguration.yml").getPath();
    private static final String[] DO_BACKUP_COMMAND_LINE = {"-cf", CONFIG_FILE_PATH, "-b"};
    private static final String[] DELETE_BAD_BACKUPS_COMMAND_LINE = {"-cf", CONFIG_FILE_PATH, "-db"};
    private static final String[] DELETE_BACKUPS_COMMAND_LINE = {"-cf", CONFIG_FILE_PATH, "-d", "0"};
    private static final String[] LIST_BACKUPS_COMMAND_LINE = {"-cf", CONFIG_FILE_PATH, "-l", "0"};
    private static final String[] LIST_ALL_BACKUPS_COMMAND_LINE = {"-cf", CONFIG_FILE_PATH, "-la"};
    private static final String[] RESTORE_COMMAND_LINE = {"-cf", CONFIG_FILE_PATH, "-r", "" + Long.MAX_VALUE};
    private BackupCLICommandUtil backupCLICommandUtil;
    private ArgumentParser argumentParser;
    @Mock
    private BackupServiceFactory backupServiceFactoryMock;

    @Before
    public void setUp() {
        backupCLICommandUtil = new BackupCLICommandUtil(backupServiceFactoryMock, System.out, System.err);
        argumentParser = ArgumentParsers.newArgumentParser("test parser");
        backupCLICommandUtil.configureParser(argumentParser);
    }

    private Optional<BackupCommand> parse(String args[]) throws ArgumentParserException {
        return backupCLICommandUtil.constructBackupCommand(argumentParser.parseArgs(args));
    }

    @Test
    public void backupCommandLineOptionsParsedCorrectly() throws ArgumentParserException {
        Optional<BackupCommand> createdCommand = parse(DO_BACKUP_COMMAND_LINE);

        assertThat(createdCommand.get(), is(instanceOf(DoBackupCommand.class)));
    }

    @Test
    public void deleteBadBackupsCommandLineOptionsParsedCorrectly() throws ArgumentParserException {
        Optional<BackupCommand> createdCommand = parse(DELETE_BAD_BACKUPS_COMMAND_LINE);

        assertThat(createdCommand.get(), is(instanceOf(DeleteBadBackupsCommand.class)));
    }

    @Test
    public void deleteBackupCommandLineOptionsParsedCorrectly() throws ArgumentParserException {
        Optional<BackupCommand> createdCommand = parse(DELETE_BACKUPS_COMMAND_LINE);

        assertThat(createdCommand.get(), is(instanceOf(DeleteBackupsCommand.class)));
    }

    @Test
    public void listBackupCommandLineOptionsParsedCorrectly() throws ArgumentParserException {
        Optional<BackupCommand> createdCommand = parse(LIST_BACKUPS_COMMAND_LINE);

        assertThat(createdCommand.get(), is(instanceOf(ListBackupsCommand.class)));
    }

    @Test
    public void listAllBackupCommandLineOptionsParsedCorrectly() throws ArgumentParserException {
        Optional<BackupCommand> createdCommand = parse(LIST_ALL_BACKUPS_COMMAND_LINE);

        assertThat(createdCommand.get(), is(instanceOf(ListBackupsCommand.class)));
    }

    @Test
    public void restoreBackupCommandLineOptionsParsedCorrectly() throws ArgumentParserException {
        Optional<BackupCommand> createdCommand = parse(RESTORE_COMMAND_LINE);

        assertThat(createdCommand.get(), is(instanceOf(RestoreFromBackupCommand.class)));
    }

    // TODO test for no option
}
