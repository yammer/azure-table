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

import com.yammer.collections.azure.backup.lib.Backup;
import com.yammer.collections.azure.backup.lib.BackupService;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Date;

class ListBackupsCommand extends AbstractBackupCommand {
    private final Date thresholdDate;


    ListBackupsCommand(BackupService backupService, PrintStream infoStream, PrintStream errorStream, long timeSince) {
        super(backupService, infoStream, errorStream);
        thresholdDate = new Date(timeSince);
    }

    @Override
    public void unsafeRun() {
        Collection<Backup> backups = getBackupService().listAllBackups(thresholdDate);
        for (Backup backup : backups) {
            println(format(backup));
        }
    }
}
