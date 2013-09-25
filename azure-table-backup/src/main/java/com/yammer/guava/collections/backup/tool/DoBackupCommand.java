package com.yammer.guava.collections.backup.tool;

import com.google.common.base.Optional;
import com.yammer.guava.collections.backup.lib.BackupService;

class DoBackupCommand extends BackupToolCommand {

    DoBackupCommand(BackupConfiguration configuration) throws Exception {
        super(configuration);
    }

    @Override
    public void run() throws Exception {
        BackupService.BackupResult result = getBackupService().backup();
        format(result.getBackup());
        Optional<Exception> failureCause = result.getFailureCause();
        if (failureCause.isPresent()) {
            throw failureCause.get();
        }

    }
}
