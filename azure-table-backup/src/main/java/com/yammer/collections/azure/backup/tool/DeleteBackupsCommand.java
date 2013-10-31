package com.yammer.collections.azure.backup.tool;

import com.yammer.collections.azure.backup.lib.BackupService;

import java.io.PrintStream;
import java.util.Date;

class DeleteBackupsCommand extends AbstractBackupCommand {
    private final Date thresholdDate;


    public DeleteBackupsCommand(BackupService backupService, PrintStream infoStream, PrintStream errorStream, long timeTill) {
        super(backupService, infoStream, errorStream);
        thresholdDate = new Date(timeTill);
    }

    @Override
    public void unsafeRun() {
        getBackupService().removeBackupsNotOlderThan(thresholdDate);
    }

}
