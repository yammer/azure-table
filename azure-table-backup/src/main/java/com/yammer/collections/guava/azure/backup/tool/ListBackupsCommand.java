package com.yammer.collections.guava.azure.backup.tool;

import com.yammer.collections.guava.azure.backup.lib.Backup;
import com.yammer.collections.guava.azure.backup.lib.BackupService;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Date;

class ListBackupsCommand extends AbstractBackupCommand {
    private final Date thresholdDate;


    ListBackupsCommand(BackupService backupService, BackupConfiguration backupConfiguration, PrintStream infoStream, PrintStream errorStream, long timeSince) {
        super(backupService, backupConfiguration, infoStream, errorStream);
        this.thresholdDate = new Date(timeSince);
    }

    @Override
    public void run() throws Exception {
        Collection<Backup> backups = getBackupService().listAllBackups(thresholdDate);
        for (Backup backup : backups) {
            println(format(backup));
        }
    }
}
