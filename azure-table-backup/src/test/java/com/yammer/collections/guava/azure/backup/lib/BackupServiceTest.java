package com.yammer.collections.guava.azure.backup.lib;

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class BackupServiceTest {
    private static final String TABLE_NAME = "table_name";
    @Mock
    private SourceTableFactory sourceTableFactoryMock;
    @Mock
    private BackupTableFactory backupTableFactoryMock;
    @Mock
    private TableCopy<String, String, String> tableCopyMock;
    private BackupService secretieBackup;
    // name of backup, date created, status
    private final Table<String, Date, Backup.BackupStatus> backupListTable = HashBasedTable.create();
    @Mock
    private Table<String, String, String> sourceTableMock;
    @Mock
    private Table<String, String, String> backupTableMock;
    @Mock
    private Table<String, String, String> tableWithBackupMock;

    private static Table.Cell<String, Date, Backup.BackupStatus> getBackupEntry(Table<String, Date, Backup.BackupStatus> backupListTable) {
        return backupListTable.cellSet().iterator().next();
    }

    @Before
    public void setUp() {
        when(backupTableFactoryMock.getBackupListTable()).thenReturn(backupListTable);
        when(backupTableFactoryMock.createBackupTable(any(Date.class), eq(TABLE_NAME))).thenReturn(backupTableMock);
        when(sourceTableFactoryMock.getSourceTable()).thenReturn(sourceTableMock);
        when(sourceTableFactoryMock.getTableName()).thenReturn(TABLE_NAME);

        secretieBackup = new BackupService(tableCopyMock, sourceTableFactoryMock, backupTableFactoryMock);
    }

    @Test
    public void on_successfull_backup_whole_table_copied() throws TableCopyException {
        secretieBackup.backup();

        verify(tableCopyMock).perform(sourceTableMock, backupTableMock);
    }

    @Test
    public void on_successfull_backup_success_is_returned() {
        BackupService.BackupResult backupResult = secretieBackup.backup();

        assertThat(backupResult.getBackup().getStatus(), is(equalTo(Backup.BackupStatus.COMPLETED)));
        assertThat(backupResult.getFailureCause(), is(equalTo(Optional.<Exception>absent())));
    }

    @Test
    public void backup_name_corresponds_to_table_name() {
        Backup backup = secretieBackup.backup().getBackup();
        assertThat(backup.getName(), is(equalTo(TABLE_NAME)));
    }

    @Test
    public void backup_date_corresponds_to_backup_time() {
        final Date startDate = new Date();
        Backup backup = secretieBackup.backup().getBackup();
        final Date endDate = new Date();

        assertThat(backup.getDate(), is(greaterThanOrEqualTo(startDate)));
        assertThat(backup.getDate(), is(lessThanOrEqualTo(endDate)));
    }

    @Test
    public void on_successfull_backup_backup_entry_updated_as_completed_with_correct_date_and_table_name() {
        BackupService.BackupResult backupResult = secretieBackup.backup();

        verifyThat(backupListTable).
                hasBackupEntryFor(TABLE_NAME).
                on(backupResult.getBackup().getDate()).
                withStatus(Backup.BackupStatus.COMPLETED);
    }

    @Test
    public void backup_table_requested_for_backup_name_and_date() {
        Backup backup = secretieBackup.backup().getBackup();

        verify(backupTableFactoryMock).createBackupTable(backup.getDate(), backup.getName());
    }

    @Test
    public void on_failed_backup_failure_result_is_returned() throws TableCopyException {
        final Exception failureCause = new TableCopyException(new RuntimeException());
        doThrow(failureCause).when(tableCopyMock).perform(sourceTableMock, backupTableMock);


        BackupService.BackupResult backupResult = secretieBackup.backup();

        assertThat(backupResult.getBackup().getStatus(), is(equalTo(Backup.BackupStatus.IN_PROGRESS)));
        assertThat(backupResult.getFailureCause(), is(equalTo(Optional.of(failureCause))));
    }

    @Test
    public void on_failed_backup_backup_entry_updated_as_not_completed_with_correct_date_and_table_name() throws TableCopyException {
        doThrow(new TableCopyException(new RuntimeException())).when(tableCopyMock).perform(sourceTableMock, backupTableMock);

        BackupService.BackupResult backupResult = secretieBackup.backup();

        verifyThat(backupListTable).
                hasBackupEntryFor(TABLE_NAME).
                on(backupResult.getBackup().getDate()).
                withStatus(Backup.BackupStatus.IN_PROGRESS);
    }

    @Test
    public void backup_list_table_update_fails_backup() {
        final Exception updateErrorException = new RuntimeException();
        updatingBackupListTable().resultsIn(updateErrorException);

        BackupService.BackupResult backupResult = secretieBackup.backup();

        assertThat(backupResult.getBackup().getStatus(), is(equalTo(Backup.BackupStatus.IN_PROGRESS)));
        assertThat(backupResult.getFailureCause(), is(equalTo(Optional.of(updateErrorException))));
    }

    @Test
    public void delete_backup_removes_the_backup_from_list() throws InterruptedException {
        Backup backupOne = secretieBackup.backup().getBackup();
        Thread.sleep(1);
        Backup backupTwo = secretieBackup.backup().getBackup();

        secretieBackup.removeBackup(backupOne);

        assertThat(secretieBackup.listAllBackups(backupOne.getDate()), containsInAnyOrder(backupTwo));
    }

    @Test
    public void delete_backup_removes_the_backup_table() throws InterruptedException {
        Backup backupOne = secretieBackup.backup().getBackup();

        secretieBackup.removeBackup(backupOne);

        verify(backupTableFactoryMock).removeTable(backupOne.getDate(), backupOne.getName());
    }

    @Test
    public void if_exception_occurs_mid_delete_backup_in_being_deleted_state() {
        Backup backupToBeRemoved = secretieBackup.backup().getBackup();

        doThrow(new RuntimeException()).when(backupTableFactoryMock).removeTable(backupToBeRemoved.getDate(), backupToBeRemoved.getName());

        try {
            secretieBackup.removeBackup(backupToBeRemoved);
        } catch (Exception e) {
        }

        Optional<Backup> partiallyDeletedBackup = secretieBackup.findBackup(backupToBeRemoved.getName(), backupToBeRemoved.getDate());
        assertThat(partiallyDeletedBackup.isPresent(), is(equalTo(true)));
        assertThat(partiallyDeletedBackup.get().getStatus(), is(equalTo(Backup.BackupStatus.BEING_DELETED)));

    }

    @Test
    public void when_backup_exists_it_is_returned_by_find_backup() {
        Backup backup = secretieBackup.backup().getBackup();

        assertThat(secretieBackup.findBackup(backup.getName(), backup.getDate()), is(equalTo(Optional.of(backup))));
    }

    @Test
    public void when_backup_does_not_exists_absent_value_is_returned() {
        assertThat(secretieBackup.findBackup("some random name", new Date()), is(equalTo(Optional.<Backup>absent())));
    }

    @Test
    public void when_given_threshold_date_later_backups_not_removed_from_list() throws InterruptedException {
        final Date startDate = new Date();
        secretieBackup.backup().getBackup();
        Thread.sleep(1);
        secretieBackup.backup().getBackup();
        final Date thresholdDate = new Date();
        Thread.sleep(1);
        Backup backupAfterThresholdDate = secretieBackup.backup().getBackup();

        secretieBackup.removeBackupsNotOlderThan(thresholdDate);

        assertThat(secretieBackup.listAllBackups(startDate), containsInAnyOrder(backupAfterThresholdDate));
    }

    @Test
    public void when_given_threshold_date_backups_to_that_date_removed() throws InterruptedException {
        Backup backup1 = secretieBackup.backup().getBackup();
        Thread.sleep(1);
        Backup backup2 = secretieBackup.backup().getBackup();
        final Date thresholdDate = new Date();
        Thread.sleep(1);
        secretieBackup.backup().getBackup();

        secretieBackup.removeBackupsNotOlderThan(thresholdDate);

        verify(backupTableFactoryMock).removeTable(backup1.getDate(), backup1.getName());
        verify(backupTableFactoryMock).removeTable(backup2.getDate(), backup2.getName());
    }

    @Test
    public void listing_of_backups_lists_all_backups_after_specified_date() throws InterruptedException {
        final Date startDate = new Date();
        final Backup toEarlyBackup = secretieBackup.backup().getBackup();
        Thread.sleep(1);
        final Date afterThresholdDate = new Date();
        Backup backupAfterThresholdDate1 = secretieBackup.backup().getBackup();
        Thread.sleep(1);
        Backup backupAfterThresholdDate2 = secretieBackup.backup().getBackup();

        assertThat(secretieBackup.listAllBackups(startDate), containsInAnyOrder(toEarlyBackup, backupAfterThresholdDate1, backupAfterThresholdDate2));
        assertThat(secretieBackup.listAllBackups(afterThresholdDate), containsInAnyOrder(backupAfterThresholdDate1, backupAfterThresholdDate2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_backup_in_in_progress_state_restore_fails() throws TableCopyException {
        Backup incompleteBackup = createIncompleteBackup();

        secretieBackup.restore(incompleteBackup);
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_backup_in_being_deleted_state_restore_fails() throws TableCopyException {
        Backup notFullyDeletedBackup = createNotFullyDeletedBackup();

        secretieBackup.restore(notFullyDeletedBackup);
    }

    @Test
    public void given_backup_when_restore_main_table_cleared_and_backup_moved_to_the_main_table() throws TableCopyException {
        Backup backupToBeRestored = secretieBackup.backup().getBackup();
        when(backupTableFactoryMock.getBackupTable(backupToBeRestored.getDate(), backupToBeRestored.getName())).thenReturn(tableWithBackupMock);

        secretieBackup.restore(backupToBeRestored);

        InOrder inOrder = inOrder(sourceTableFactoryMock, tableCopyMock);

        inOrder.verify(sourceTableFactoryMock).clearSourceTable();
        inOrder.verify(tableCopyMock).perform(tableWithBackupMock, sourceTableMock);
    }

    // fluent assertions utilities

    private Backup createIncompleteBackup() throws TableCopyException {
        doThrow(new TableCopyException(new RuntimeException())).when(tableCopyMock).perform(sourceTableMock, backupTableMock);
        return secretieBackup.backup().getBackup();
    }

    private Backup createNotFullyDeletedBackup() {
        Backup notFullyDeletedBackup = secretieBackup.backup().getBackup();

        doThrow(new RuntimeException()).when(backupTableFactoryMock).removeTable(notFullyDeletedBackup.getDate(), notFullyDeletedBackup.getName());

        try {
            secretieBackup.removeBackup(notFullyDeletedBackup);
        } catch (Exception e) {
            return notFullyDeletedBackup;
        }
        fail();
        return null;
    }


    private BackupListTableFailureBuilder updatingBackupListTable() {
        return new BackupListTableFailureBuilder();
    }

    private BackupTableAssertionBuilder verifyThat(Table<String, Date, Backup.BackupStatus> backupTable) {
        return new BackupTableAssertionBuilder(backupTable);
    }

    private static class BackupTableAssertionBuilder {

        private final Table<String, Date, Backup.BackupStatus> backupTable;
        private String tableName;
        private Date backupDate;

        public BackupTableAssertionBuilder(Table<String, Date, Backup.BackupStatus> backupTable) {
            this.backupTable = backupTable;
        }

        BackupTableAssertionBuilder hasBackupEntryFor(String tableName) {
            this.tableName = tableName;
            return this;
        }

        BackupTableAssertionBuilder on(Date backupDate) {
            this.backupDate = backupDate;
            return this;
        }

        void withStatus(Backup.BackupStatus status) {
            Table.Cell<String, Date, Backup.BackupStatus> backup_entry = getBackupEntry(backupTable);
            assertThat(backup_entry.getValue(), is(equalTo(status)));
            assertThat(backup_entry.getRowKey(), is(equalTo(tableName)));
            assertThat(backup_entry.getColumnKey(), is(equalTo(backupDate)));
        }
    }

    private class BackupListTableFailureBuilder {
        void resultsIn(Exception e) {
            @SuppressWarnings("unchecked") Table<String, Date, Backup.BackupStatus> backupListTableMock = mock(Table.class);
            when(backupTableFactoryMock.getBackupListTable()).thenReturn(backupListTableMock);
            secretieBackup = new BackupService(tableCopyMock, sourceTableFactoryMock, backupTableFactoryMock);

            when(backupListTableMock.put(any(String.class), any(Date.class), any(Backup.BackupStatus.class))).thenThrow(e);
        }
    }
}
