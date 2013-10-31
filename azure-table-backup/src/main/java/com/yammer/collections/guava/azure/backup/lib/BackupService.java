package com.yammer.collections.guava.azure.backup.lib;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import java.util.Collection;
import java.util.Date;

/**
 * This is a backup service which operates on the Guava Table abstraction. There are two caveats:
 * - it reads all the data into memmory, so it is applicable to small data sets
 * - it is oblivious of the backed up data type, it treats everything as strings
 * - it is not transactional, but updates progress to enable external retries
 * - this class is not concurrency safe.
 */
public class BackupService {
    private final Function<Table.Cell<String, Date, Backup.BackupStatus>, Backup> createBackupEntry = new Function<Table.Cell<String, Date, Backup.BackupStatus>, Backup>() {
        @Override
        public Backup apply(Table.Cell<String, Date, Backup.BackupStatus> input) {
            return Backup.fromTableCell(input);

        }
    };
    private final String tableName;
    private final TableCopy<String, String, String> tableCopy;
    private final SourceTableFactory sourceTableFactory;
    private final BackupTableFactory backupTableFactory;
    private final Table<String, Date, Backup.BackupStatus> backupListTable;

    public BackupService(TableCopy<String, String, String> tableCopy,
                         SourceTableFactory sourceTableFactory,
                         BackupTableFactory backupTableFactory) {
        this.tableCopy = tableCopy;
        this.sourceTableFactory = sourceTableFactory;
        this.backupTableFactory = backupTableFactory;
        tableName = sourceTableFactory.getTableName();
        backupListTable = backupTableFactory.getBackupListTable();
    }

    @SuppressWarnings("OverlyBroadCatchBlock")
    public BackupResult backup() {
        Backup backup = Backup.startBackup(tableName);
        try {
            performBackup(backup);
            Backup completedBackup = Backup.completeBackup(backup);
            return BackupResult.success(completedBackup);
        } catch (Exception e) {
            return BackupResult.failure(backup, e);
        }
    }

    private void performBackup(Backup backup) throws TableCopyException {
        Date backupDate = backup.getDate();
        setBackupStarted(backupDate);
        tableCopy.perform(
                sourceTableFactory.getSourceTable(),
                backupTableFactory.createBackupTable(backupDate, tableName)
        );
        setBackupCompleted(backupDate);
    }

    private void setBackupStarted(Date date) {
        backupListTable.put(tableName, date, Backup.BackupStatus.IN_PROGRESS);
    }

    private void setBackupCompleted(Date date) {
        backupListTable.put(tableName, date, Backup.BackupStatus.COMPLETED);
    }

    private void setBeingDeleted(Backup backup) {
        backupListTable.put(backup.getName(), backup.getDate(), Backup.BackupStatus.BEING_DELETED);
    }

    public Collection<Backup> listAllBackups(final Date afterThresholdDate) {
        Predicate<Table.Cell<String, Date, Backup.BackupStatus>> thresholdDatePredicate = new Predicate<Table.Cell<String, Date, Backup.BackupStatus>>() {
            @Override
            public boolean apply(Table.Cell<String, Date, Backup.BackupStatus> input) {
                return input.getColumnKey().compareTo(afterThresholdDate) >= 0;
            }
        };

        Collection<Table.Cell<String, Date, Backup.BackupStatus>>
                notEarlierThanThresholdDate = Collections2.filter(backupListTable.cellSet(), thresholdDatePredicate);
        return Collections2.transform(notEarlierThanThresholdDate, createBackupEntry);
    }

    public void removeBackup(Backup backup) {
        setBeingDeleted(backup);
        backupTableFactory.removeTable(backup.getDate(), backup.getName());
        backupListTable.remove(backup.getName(), backup.getDate());
    }

    public Optional<Backup> findBackup(String backupName, Date backupDate) {
        Backup.BackupStatus status = backupListTable.get(backupName, backupDate);
        if (status == null) {
            return Optional.absent();
        }

        return Optional.of(new Backup(backupName, backupDate, status));
    }

    public void removeBackupsNotOlderThan(final Date thresholdDate) {
        Predicate<Table.Cell<String, Date, Backup.BackupStatus>> thresholdDatePredicate = new Predicate<Table.Cell<String, Date, Backup.BackupStatus>>() {
            @Override
            public boolean apply(Table.Cell<String, Date, Backup.BackupStatus> input) {
                return input.getColumnKey().compareTo(thresholdDate) <= 0;
            }
        };


        Collection<Table.Cell<String, Date, Backup.BackupStatus>>
                notLaterThanThresholdDate = Collections2.filter(backupListTable.cellSet(), thresholdDatePredicate);
        Collection<Backup>
                backupsToBeDeleted = Lists.newArrayList(Collections2.transform(notLaterThanThresholdDate, createBackupEntry));

        for (Backup backup : backupsToBeDeleted) {
            removeBackup(backup);
        }
    }

    public void restore(Backup backupToBeRestored) throws TableCopyException {
        Backup freshBackupToBeRestored = findBackup(backupToBeRestored.getName(), backupToBeRestored.getDate()).get();
        assertBackupCompleted(freshBackupToBeRestored);
        sourceTableFactory.clearSourceTable();
        tableCopy.perform(
                backupTableFactory.getBackupTable(freshBackupToBeRestored.getDate(), freshBackupToBeRestored.getName()),
                sourceTableFactory.getSourceTable());
    }

    private static void assertBackupCompleted(Backup backupToBeRestored) {
        if (backupToBeRestored.getStatus() != Backup.BackupStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "Expected backup in status: " +
                            Backup.BackupStatus.COMPLETED +
                            " but got status: "
                            + backupToBeRestored.getStatus());
        }
    }

    public static class BackupResult {
        private final Optional<Exception> failureCause;
        private final Backup backup;

        private BackupResult(Backup backup, Optional<Exception> failureCause) {
            this.backup = backup;
            this.failureCause = failureCause;
        }

        public static BackupResult success(Backup backup) {
            return new BackupResult(backup, Optional.<Exception>absent());
        }

        public static BackupResult failure(Backup backup, Exception e) {
            return new BackupResult(backup, Optional.of(e));
        }

        public Optional<Exception> getFailureCause() {
            return failureCause;
        }

        public Backup getBackup() {
            return backup;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BackupResult that = (BackupResult) o;

            if (backup != null ? !backup.equals(that.backup) : that.backup != null) return false;
            if (failureCause != null ? !failureCause.equals(that.failureCause) : that.failureCause != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = failureCause != null ? failureCause.hashCode() : 0;
            result = 31 * result + (backup != null ? backup.hashCode() : 0);
            return result;
        }
    }
}
