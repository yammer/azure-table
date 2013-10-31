package com.yammer.collections.azure.backup.tool;

import com.yammer.collections.azure.backup.lib.Backup;
import com.yammer.collections.azure.backup.lib.BackupService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SuppressWarnings("InstanceVariableMayNotBeInitialized")
@RunWith(MockitoJUnitRunner.class)
public class DeleteBadBackupsCommandTest {
    private static final long BEGINING_OF_TIME = 0;
    @Mock
    private DeleteBadBackupsCommand deleteBadBackupsCommand;
    @Mock
    private BackupService backupServiceMock;
    @Mock
    private PrintStream infoStreamMock;
    @Mock
    private PrintStream errorStreamMock;
    @Mock
    private Backup backupMock1;
    @Mock
    private Backup backupMock2;
    @Mock
    private Backup backupMock3;

    @Before
    public void setUp() {
        when(backupServiceMock.listAllBackups(new Date(BEGINING_OF_TIME))).thenReturn(Arrays.asList(backupMock1, backupMock2, backupMock3));
        when(backupMock1.getStatus()).thenReturn(Backup.BackupStatus.IN_PROGRESS);
        when(backupMock2.getStatus()).thenReturn(Backup.BackupStatus.COMPLETED);
        when(backupMock3.getStatus()).thenReturn(Backup.BackupStatus.BEING_DELETED);

        deleteBadBackupsCommand = new DeleteBadBackupsCommand(backupServiceMock, infoStreamMock, errorStreamMock);
    }

    @Test
    public void delete_bad_backups_command_deletes_bad_backups() {
        deleteBadBackupsCommand.run();

        verify(backupServiceMock).removeBackup(backupMock1);
        verify(backupServiceMock).removeBackup(backupMock3);
    }

    @Test
    public void delete_bad_backups_command_does_not_delete_completed_backups() {
        deleteBadBackupsCommand.run();

        verify(backupServiceMock, never()).removeBackup(backupMock2);
    }

}