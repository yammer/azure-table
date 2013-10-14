package com.yammer.collections.guava.azure.backup.tool;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.yammer.collections.guava.azure.backup.lib.Backup;
import com.yammer.collections.guava.azure.backup.lib.BackupService;

import java.io.PrintStream;
import java.util.Date;

class DeleteBadBackupsCommand extends AbstractBackupCommand {

    public DeleteBadBackupsCommand(BackupService backupService, PrintStream infoStream, PrintStream errorStream) {
        super(backupService, infoStream, errorStream);
    }

    @Override
    public void unsafeRun() {
        Iterable<Backup> allBackups = getBackupService().listAllBackups(new Date(0));
        Iterable<Backup> badBackups = Iterables.filter(allBackups, new Predicate<Backup>() {
            @Override
            public boolean apply(Backup input) {
                return input.getStatus() != Backup.BackupStatus.COMPLETED;
            }
        });
        for (Backup badBackup : badBackups) {
            getBackupService().removeBackup(badBackup);
        }
    }
}
