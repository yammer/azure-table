package com.yammer.collections.guava.azure.backup.tool;

import com.google.common.base.Optional;
import com.yammer.collections.guava.azure.backup.lib.BackupService;

class DoBackupCommand extends AbstractBackupToolCommand {

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
