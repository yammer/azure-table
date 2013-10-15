package com.yammer.collections.guava.azure.backup.tool;


public interface BackupCommand {
    // TODO make it return result, rather than write to stdin/out
    void run();

}
