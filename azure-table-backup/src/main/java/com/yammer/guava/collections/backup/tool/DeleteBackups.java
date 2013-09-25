package com.yammer.guava.collections.backup.tool;

import java.util.Date;

class DeleteBackups extends BackupToolCommand {
    private final Date thresholdDate;


    DeleteBackups(BackupConfiguration configuration, long time) throws Exception {
        super(configuration);
        this.thresholdDate = new Date(time);
    }

    @Override
    public void run() throws Exception {
        getBackupService().removeBackupsNotOlderThan(thresholdDate);
    }

}
