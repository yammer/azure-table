package com.yammer.collections.guava.azure.backup.tool;

import com.yammer.collections.guava.azure.backup.lib.Backup;
import com.yammer.collections.guava.azure.backup.lib.BackupService;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Date;

class ListBackupsCommand extends AbstractBackupCommand {
    private final Date thresholdDate;


    ListBackupsCommand(BackupService backupService, PrintStream infoStream, PrintStream errorStream, long timeSince) {
        super(backupService, infoStream, errorStream);
        thresholdDate = new Date(timeSince);
    }

    @Override
    public void unsafeRun() {
        Collection<Backup> backups = getBackupService().listAllBackups(thresholdDate);
        for (Backup backup : backups) {
            println(format(backup));
        }
    }
}
