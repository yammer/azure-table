package com.yammer.collections.azure.backup.tool;

import com.google.common.base.Optional;
import com.yammer.collections.azure.backup.lib.Backup;
import com.yammer.collections.azure.backup.lib.BackupService;
import com.yammer.collections.azure.backup.lib.TableCopyException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintStream;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("InstanceVariableMayNotBeInitialized")
@RunWith(MockitoJUnitRunner.class)
public class RestoreFromBackupCommandTest {
    private static final String BACKUP_NAME = "backupName";
    private static final long BACKUP_TIME = 32l;
    @Mock
    private RestoreFromBackupCommand restoreFromBackupCommand;
    @Mock
    private BackupService backupServiceMock;
    @Mock
    private PrintStream infoStreamMock;
    @Mock
    private PrintStream errorStreamMock;
    @Mock
    private Backup backupMock;

    @Before
    public void setUp() {
        when(backupMock.getName()).thenReturn(BACKUP_NAME);
        when(backupMock.getDate()).thenReturn(new Date());
        when(backupMock.getStatus()).thenReturn(Backup.BackupStatus.COMPLETED);
        when(backupServiceMock.findBackup(BACKUP_NAME, new Date(BACKUP_TIME))).thenReturn(Optional.of(backupMock));

        restoreFromBackupCommand = new RestoreFromBackupCommand(backupServiceMock, BACKUP_NAME, infoStreamMock, errorStreamMock, BACKUP_TIME);
    }

    @Test
    public void restoreFromBackupCommand_restores_from_backup_if_it_exists() throws TableCopyException {
        when(backupServiceMock.findBackup(BACKUP_NAME, new Date(BACKUP_TIME))).thenReturn(Optional.of(backupMock));

        restoreFromBackupCommand.run();

        verify(backupServiceMock).restore(backupMock);
    }

    @Test
    public void doBackupCommand_does_nothing_if_cannot_find_backup() throws TableCopyException {
        when(backupServiceMock.findBackup(BACKUP_NAME, new Date(BACKUP_TIME))).thenReturn(Optional.<Backup>absent());

        restoreFromBackupCommand.run();

        verify(backupServiceMock, never()).restore(any(Backup.class));
    }
}
