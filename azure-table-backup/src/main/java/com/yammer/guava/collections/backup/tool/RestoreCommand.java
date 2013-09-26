package com.yammer.guava.collections.backup.tool;

import com.google.common.base.Optional;
import com.yammer.guava.collections.backup.lib.Backup;

import java.util.Date;

public class RestoreCommand extends AbstractBackupToolCommand {
    private final Date backupTime;

    public RestoreCommand(BackupConfiguration configuration, Printer printer, long backupTime) throws Exception {
        super(configuration, printer);
        this.backupTime = new Date(backupTime);
    }


    @Override
    public void run() throws Exception {
        Optional<Backup> backup = getBackupService().findBackup(getBackupName(), backupTime);
        if(backup.isPresent()) {
            getBackupService().restore(backup.get());
            println("Restored backup: "+format(backup.get()));
        } else {
            printErrorln("No backup found for table="+getBackupName()+" at timestamp="+backupTime.getTime());
        }
    }
}
