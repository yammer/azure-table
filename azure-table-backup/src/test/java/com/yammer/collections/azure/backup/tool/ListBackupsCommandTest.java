package com.yammer.collections.azure.backup.tool;

import com.yammer.collections.azure.backup.lib.BackupService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintStream;
import java.util.Date;

import static org.mockito.Mockito.verify;


@SuppressWarnings("InstanceVariableMayNotBeInitialized")
@RunWith(MockitoJUnitRunner.class)
public class ListBackupsCommandTest {
    private static final long TIME_SINCE = 20l;
    @Mock
    private ListBackupsCommand listBackupsCommand;
    @Mock
    private BackupService backupServiceMock;
    @Mock
    private PrintStream infoStreamMock;
    @Mock
    private PrintStream errorStreamMock;

    @Before
    public void setUp() {
        listBackupsCommand = new ListBackupsCommand(backupServiceMock, infoStreamMock, errorStreamMock, TIME_SINCE);
    }

    @Test
    public void list_backups_command_deletes_list_backups_since() {
        listBackupsCommand.run();

        verify(backupServiceMock).listAllBackups(new Date(TIME_SINCE));
    }

}