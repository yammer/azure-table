package com.yammer.collections.guava.azure.backup.tool;

import com.google.common.base.Optional;
import com.yammer.collections.guava.azure.backup.lib.BackupService;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

class DoBackupCommand extends AbstractBackupCommand {

    DoBackupCommand(BackupConfiguration configuration, Printer printer) throws URISyntaxException, InvalidKeyException {
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
