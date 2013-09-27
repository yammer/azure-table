package com.yammer.collections.guava.azure.backup.tool;

import java.util.Date;

class DeleteBackupsCommand extends AbstractBackupToolCommand {
    private final Date thresholdDate;


    DeleteBackupsCommand(BackupConfiguration configuration, Printer printer, long time) throws Exception {
        super(configuration, printer);
        this.thresholdDate = new Date(time);
    }

    @Override
    public void run() throws Exception {
        getBackupService().removeBackupsNotOlderThan(thresholdDate);
    }

}
