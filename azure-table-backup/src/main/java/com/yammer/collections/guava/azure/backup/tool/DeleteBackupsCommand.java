package com.yammer.collections.guava.azure.backup.tool;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Date;

class DeleteBackupsCommand extends AbstractBackupCommand {
    private final Date thresholdDate;


    DeleteBackupsCommand(BackupConfiguration configuration, Printer printer, long time) throws URISyntaxException, InvalidKeyException {
        super(configuration, printer);
        this.thresholdDate = new Date(time);
    }

    @Override
    public void run() throws Exception {
        getBackupService().removeBackupsNotOlderThan(thresholdDate);
    }

}
