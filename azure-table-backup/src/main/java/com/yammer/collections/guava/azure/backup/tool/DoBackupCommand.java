package com.yammer.collections.guava.azure.backup.tool;

import com.google.common.base.Optional;
import com.yammer.collections.guava.azure.backup.lib.BackupService;

import java.io.PrintStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

class DoBackupCommand extends AbstractBackupCommand {

    DoBackupCommand(BackupConfiguration backupConfiguration, PrintStream infoStream, PrintStream errorStream) throws URISyntaxException, InvalidKeyException {
        super(backupConfiguration, infoStream, errorStream);
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
