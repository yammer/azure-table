package com.yammer.collections.guava.azure.backup.tool;

import com.yammer.collections.guava.azure.backup.lib.Backup;
import com.yammer.collections.guava.azure.backup.lib.BackupService;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

// TODO provide tests for all commands, requires decoupling from configuration and azure
abstract class AbstractBackupCommand implements BackupCommand {
    private final DateFormat dateFormat = new SimpleDateFormat();
    private final BackupService backupService;
    private final PrintStream infoStream;
    private final PrintStream errorStrem;

    AbstractBackupCommand(BackupService backupService, PrintStream infoStream, PrintStream errorStrem) {
        this.infoStream = infoStream;
        this.errorStrem = errorStrem;
        this.backupService = backupService;
    }

    protected final BackupService getBackupService() {
        return backupService;
    }

    protected String format(Backup backup) {
        return String.format("Backup: NAME=%s DATE=%s TIMESTAMP=%s STATUS=%s", backup.getName(), dateFormat.format(backup.getDate()),
                backup.getDate().getTime(), backup.getStatus());
    }

    public abstract void run() throws Exception;

    public final void println(String str) {
        infoStream.println(str);
    }

    public final void printErrorln(String str) {
        errorStrem.println(str);
    }

}
