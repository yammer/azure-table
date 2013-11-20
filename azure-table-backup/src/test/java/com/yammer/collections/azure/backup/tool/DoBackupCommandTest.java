/**
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED WARRANTIES OR CONDITIONS
 * OF TITLE, FITNESS FOR A PARTICULAR PURPOSE, MERCHANTABLITY OR NON-INFRINGEMENT.
 *
 * See the Apache Version 2.0 License for specific language governing permissions and limitations under
 * the License.
 */
package com.yammer.collections.azure.backup.tool;


import com.google.common.base.Optional;
import com.yammer.collections.azure.backup.lib.Backup;
import com.yammer.collections.azure.backup.lib.BackupService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintStream;
import java.util.Date;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SuppressWarnings("InstanceVariableMayNotBeInitialized")
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
    public void doBackupCommand_runs_a_backup() {
        when(backupResultMock.getFailureCause()).thenReturn(Optional.<Exception>absent());

        doBackupCommand.run();

        verify(backupServiceMock).backup();
    }

    @Test(expected = RuntimeException.class)
    public void doBackupCommand_rethrows_the_cause_of_backup_failure() {
        when(backupResultMock.getFailureCause()).thenReturn(Optional.of(new Exception("test exception")));

        doBackupCommand.run();
    }

}