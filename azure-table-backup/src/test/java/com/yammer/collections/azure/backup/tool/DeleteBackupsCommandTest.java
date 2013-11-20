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
public class DeleteBackupsCommandTest {
    private static final long TIME_TILL = 20l;
    @Mock
    private DeleteBackupsCommand deleteBackupsCommand;
    @Mock
    private BackupService backupServiceMock;
    @Mock
    private PrintStream infoStreamMock;
    @Mock
    private PrintStream errorStreamMock;

    @Before
    public void setUp() {
        deleteBackupsCommand = new DeleteBackupsCommand(backupServiceMock, infoStreamMock, errorStreamMock, TIME_TILL);
    }

    @Test
    public void delete_backups_command_deletes_backups_not_older_than() {
        deleteBackupsCommand.run();

        verify(backupServiceMock).removeBackupsNotOlderThan(new Date(TIME_TILL));
    }

}
