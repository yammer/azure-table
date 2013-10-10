package com.yammer.collections.guava.azure.backup.tool;

import java.io.PrintStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Date;

class DeleteBackupsCommand extends AbstractBackupCommand {
    private final Date thresholdDate;


    public DeleteBackupsCommand(BackupConfiguration backupConfiguration, PrintStream infoStream, PrintStream errorStream, long timeTill) throws URISyntaxException, InvalidKeyException {
        super(backupConfiguration, infoStream, errorStream);
        this.thresholdDate = new Date(timeTill);
    }

    @Override
    public void run() throws Exception {
        getBackupService().removeBackupsNotOlderThan(thresholdDate);
    }

}
