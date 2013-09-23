package com.yammer.secretie.backup.lib;

import com.google.common.collect.Table;

import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

public class Backup {
    private final String name;
    private final Date date;
    private final BackupStatus status;

    Backup(String name, Date date, BackupStatus status) {
        checkNotNull(name);
        checkNotNull(date);
        checkNotNull(status);
        this.name = name;
        this.date = date;
        this.status = status;
    }

    static Backup completeBackup(Backup backup) {
        return new Backup(backup.getName(), backup.getDate(), BackupStatus.COMPLETED);
    }

    static Backup startBackup(String name) {
        return new Backup(name, new Date(), BackupStatus.IN_PROGRESS);
    }

    static Backup fromTableCell(Table.Cell<String, Date, BackupStatus> cell) {
        return new Backup(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Backup backup = (Backup) o;

        if (!date.equals(backup.date)) return false;
        if (!name.equals(backup.name)) return false;
        if (status != backup.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + status.hashCode();
        return result;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public BackupStatus getStatus() {
        return status;
    }

    public enum BackupStatus {
        COMPLETED("COMPLETED"),
        IN_PROGRESS("IN_PROGRESS"),
        BEING_DELETED("BEING_DELETED");
        private final String completed;

        BackupStatus(String completed) {
            this.completed = completed;
        }

        @Override
        public String toString() {
            return completed;
        }

    }
}
