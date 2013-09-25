package com.yammer.guava.collections.backup.tool;

import java.util.Date;

class DeleteBackupsCommand extends BackupToolCommand {
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
