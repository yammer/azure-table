package com.yammer.collections.guava.azure.backup.tool;

import com.yammer.collections.guava.azure.backup.lib.BackupService;

import java.io.PrintStream;
import java.util.Date;

class DeleteBackupsCommand extends AbstractBackupCommand {
    private final Date thresholdDate;


    public DeleteBackupsCommand(BackupService backupService, BackupConfiguration backupConfiguration, PrintStream infoStream, PrintStream errorStream, long timeTill) {
        super(backupService, backupConfiguration, infoStream, errorStream);
        this.thresholdDate = new Date(timeTill);
    }

    @Override
    public void run() throws Exception {
        getBackupService().removeBackupsNotOlderThan(thresholdDate);
    }

}
