package com.yammer.collections.guava.azure.backup.tool;


import com.google.common.base.Optional;
import com.yammer.collections.guava.azure.backup.lib.Backup;
import com.yammer.collections.guava.azure.backup.lib.BackupService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintStream;
import java.util.Date;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DoBackupCommandTest {
    @Mock
    private DoBackupCommand doBackupCommand;
    @Mock
    private BackupService backupServiceMock;
    @Mock
    private PrintStream infoStreamMock;
    @Mock
    private PrintStream errorStreamMock;
    @Mock
    private Backup backupMock;
    @Mock
    private BackupService.BackupResult backupResultMock;

    @Before
    public void setUp() {
        when(backupMock.getName()).thenReturn("name");
        when(backupMock.getDate()).thenReturn(new Date());
        when(backupMock.getStatus()).thenReturn(Backup.BackupStatus.COMPLETED);
        when(backupServiceMock.backup()).thenReturn(backupResultMock);
        when(backupResultMock.getBackup()).thenReturn(backupMock);

        doBackupCommand = new DoBackupCommand(backupServiceMock, infoStreamMock, errorStreamMock);
    }

    @Test
    public void doBackupCommand_runs_a_backup() throws Exception {
        when(backupResultMock.getFailureCause()).thenReturn(Optional.<Exception>absent());

        doBackupCommand.run();

        verify(backupServiceMock).backup();
    }

    @Test(expected = Exception.class)
    public void doBackupCommand_rethrows_the_cause_of_backup_failure() throws Exception {
        when(backupResultMock.getFailureCause()).thenReturn(Optional.of(new Exception("test exception")));

        doBackupCommand.run();
    }

}