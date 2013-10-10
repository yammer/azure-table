package com.yammer.collections.guava.azure.backup.tool;

import com.google.common.base.Optional;
import com.yammer.collections.guava.azure.backup.lib.Backup;
import com.yammer.collections.guava.azure.backup.lib.BackupService;

import java.io.PrintStream;
import java.util.Date;

public class RestoreFromBackupCommand extends AbstractBackupCommand {
    private final Date backupTime;


    public RestoreFromBackupCommand(BackupService backupService, BackupConfiguration backupConfiguration, PrintStream infoStream, PrintStream errorStream,
                                    long backupTime) {
        super(backupService, backupConfiguration, infoStream, errorStream);
        this.backupTime = new Date(backupTime);
    }

    @Override
    public void run() throws Exception {
        Optional<Backup> backup = getBackupService().findBackup(getBackupName(), backupTime);
        if (backup.isPresent()) {
            getBackupService().restore(backup.get());
            println("Restored backup: " + format(backup.get()));
        } else {
            printErrorln("No backup found for table=" + getBackupName() + " at timestamp=" + backupTime.getTime());
        }
    }
}
