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

import com.google.common.base.Throwables;
import com.yammer.collections.azure.backup.lib.Backup;
import com.yammer.collections.azure.backup.lib.BackupService;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

abstract class AbstractBackupCommand implements BackupCommand {
    private final DateFormat dateFormat = new SimpleDateFormat();
    private final BackupService backupService;
    private final PrintStream infoStream;
    private final PrintStream errorStrem;

    AbstractBackupCommand(BackupService backupService, PrintStream infoStream, PrintStream errorStrem) {
        this.infoStream = infoStream;
        this.errorStrem = errorStrem;
        this.backupService = backupService;
    }

    final BackupService getBackupService() {
        return backupService;
    }

    String format(Backup backup) {
        return String.format("Backup: NAME=%s DATE=%s TIMESTAMP=%s STATUS=%s", backup.getName(), dateFormat.format(backup.getDate()),
                backup.getDate().getTime(), backup.getStatus());
    }

    @Override
    public final void run() {
        try {
            unsafeRun();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @SuppressWarnings("ProhibitedExceptionDeclared")
    protected abstract void unsafeRun() throws Exception;

    final void println(String str) {
        infoStream.println(str);
    }

    final void printErrorln(String str) {
        errorStrem.println(str);
    }

}
