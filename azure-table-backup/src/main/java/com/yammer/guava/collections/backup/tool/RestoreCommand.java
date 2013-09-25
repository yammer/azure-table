package com.yammer.guava.collections.backup.tool;

import com.google.common.base.Optional;
import com.yammer.guava.collections.backup.lib.Backup;

import java.util.Date;

public class RestoreCommand extends BackupToolCommand {
    private final Date backupTime;

    public RestoreCommand(BackupConfiguration configuration, long backupTime) throws Exception {
        super(configuration);
        this.backupTime = new Date(backupTime);
    }


    @Override
    public void run() throws Exception {
        Optional<Backup> backup = getBackupService().findBackup(getBackupName(), backupTime);
        if(backup.isPresent()) {
            getBackupService().restore(backup.get());
        } else {
            System.err.println("No backup found for table="+getBackupName()+" at timestamp="+backupTime.getTime());
        }
    }
}
