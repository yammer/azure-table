package com.yammer.guava.collections.backup.tool;

import com.google.common.base.Optional;
import com.yammer.guava.collections.backup.lib.BackupService;

class DoBackupCommand extends BackupToolCommand {

    DoBackupCommand(BackupConfiguration configuration, Printer printer) throws Exception {
        super(configuration, printer);
    }

    @Override
    public void run() throws Exception {
        BackupService.BackupResult result = getBackupService().backup();
        Optional<Exception> failureCause = result.getFailureCause();
        if (failureCause.isPresent()) {
            throw failureCause.get();
        }
        println("Created. "+format(result.getBackup()));
    }
}
