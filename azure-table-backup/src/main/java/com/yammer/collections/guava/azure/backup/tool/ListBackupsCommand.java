package com.yammer.collections.guava.azure.backup.tool;

import com.yammer.collections.guava.azure.backup.lib.Backup;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Collection;
import java.util.Date;

class ListBackupsCommand extends AbstractBackupCommand {
    private final Date thresholdDate;


    ListBackupsCommand(BackupConfiguration configuration, Printer printer, long time) throws URISyntaxException, InvalidKeyException {
        super(configuration, printer);
        this.thresholdDate = new Date(time);
    }

    @Override
    public void run() throws Exception {
        Collection<Backup> backups = getBackupService().listAllBackups(thresholdDate);
        for (Backup backup : backups) {
            println(format(backup));
        }
    }
}
