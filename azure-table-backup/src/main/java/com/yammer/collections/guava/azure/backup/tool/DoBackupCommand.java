package com.yammer.collections.guava.azure.backup.tool;

import com.google.common.base.Optional;
import com.yammer.collections.guava.azure.backup.lib.BackupService;

import java.io.PrintStream;

class DoBackupCommand extends AbstractBackupCommand {

    DoBackupCommand(BackupService backupService, PrintStream infoStream, PrintStream errorStream) {
        super(backupService, infoStream, errorStream);
    }

    @Override
    public void run() throws Exception {
        BackupService.BackupResult result = getBackupService().backup();
        Optional<Exception> failureCause = result.getFailureCause();
        if (failureCause.isPresent()) {
            throw failureCause.get();
        }
        println("Created. " + format(result.getBackup()));
    }
}
