package com.yammer.collections.guava.azure.backup.tool;

import com.google.common.base.Optional;
import com.yammer.collections.guava.azure.backup.lib.Backup;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Date;

public class RestoreCommand extends AbstractBackupCommand {
    private final Date backupTime;

    public RestoreCommand(BackupConfiguration configuration, Printer printer, long backupTime) throws URISyntaxException, InvalidKeyException {
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
