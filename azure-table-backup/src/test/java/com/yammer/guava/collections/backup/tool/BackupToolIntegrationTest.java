package com.yammer.guava.collections.backup.tool;


import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.guava.collections.backup.azure.AzureBackupTableFactory;
import com.yammer.guava.collections.backup.azure.AzureSourceTableFactory;
import com.yammer.guava.collections.backup.lib.Backup;
import com.yammer.guava.collections.backup.lib.BackupTableFactory;
import com.yammer.guava.collections.backup.lib.SourceTableFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * This test may take some time to run - deleting of tables in azure can take a while, and it is used during the restore procedure.
 * This test uses the commands used by the command line tool, and for that reason,
 * there is a degree of awkwardness in how assertions are made about backups - they have to be retrieved from commands printed output.
 */
@Ignore("Ignored as it talks to azure, should be used to integration test changes to this project")
@RunWith(MockitoJUnitRunner.class)
public class BackupToolIntegrationTest {
    private static final BackupConfiguration BACKUP_CONFIGURATION = createBackupConfiguration();
    private static final String SRC_TABLE_NAME = "backupToolIntegrationTestTable";
    private static final String BACKUP_INFO_PATTERN_STRING = "Backup: NAME=" + SRC_TABLE_NAME + ".*TIMESTAMP=(\\d*) STATUS=" + Backup.BackupStatus.COMPLETED;
    private static final Pattern BACKUP_CREATED_PATTERN = Pattern.compile("Created. " + BACKUP_INFO_PATTERN_STRING);
    private static final Pattern BACKUP_LIST_ITEM_PATTERN = Pattern.compile(BACKUP_INFO_PATTERN_STRING);
    private static final String ACCOUNT_NAME = "secretietest";
    private static final String ACCOUNT_KEY = "e5LnQoZei2cFH+56TFxDmO6AhnzMKill1NyVUs1M3R7OFNfCLnIGe17TLUex0mYYGQFjNvmArsLa8Iq3b0FNAg==";
    private static final String ROW_1 = "row1";
    private static final String ROW_2 = "row2";
    private static final String COLUMN_1 = "column1";
    private static final String COLUMN_2 = "column2";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";
    private static final String ROW_3 = "row3";
    private static final String COLUMN_3 = "column3";
    private static final String VALUE_3 = "value3";
    private static final Table.Cell<String, String, String> CELL_1 = Tables.immutableCell(ROW_1, COLUMN_1, VALUE_1);
    private static final Table.Cell<String, String, String> CELL_2 = Tables.immutableCell(ROW_2, COLUMN_2, VALUE_2);
    @Mock
    private Printer printerMock;
    private SourceTableFactory sourceTableFactory;
    private BackupTableFactory backupTableFactory;

    private static BackupConfiguration createBackupConfiguration() {
        BackupConfiguration configuration = new BackupConfiguration();
        configuration.setSourceTableName(SRC_TABLE_NAME);
        configuration.setSourceAccountName(ACCOUNT_NAME);
        configuration.setSourceAccountKey(ACCOUNT_KEY);
        configuration.setBackupAccountName(ACCOUNT_NAME);
        configuration.setBackupAccountKey(ACCOUNT_KEY);

        return configuration;
    }

    @Before
    public void setUp() throws Exception {
        CloudTableClient sourceTableClient = CloudStorageAccount.parse(BACKUP_CONFIGURATION.getSourceConnectionString()).createCloudTableClient();
        sourceTableFactory = new AzureSourceTableFactory(sourceTableClient, BACKUP_CONFIGURATION.getSourceTableName());
        CloudTableClient backupTableClient = CloudStorageAccount.parse(BACKUP_CONFIGURATION.getBackupConnectionString()).createCloudTableClient();
        backupTableFactory = new AzureBackupTableFactory(backupTableClient);

        clearDB();

        assertNoBackups();
    }

    @Test
    public void do_backup_command_backs_up_correctly() throws Exception {
        setupSourceTableToContain(CELL_1, CELL_2);

        DoBackupCommand doBackupCommand = new DoBackupCommand(BACKUP_CONFIGURATION, printerMock);
        doBackupCommand.run();

        Table<String, String, String> backupTable = getJustCreatedBackup(printerMock);

        assertThat(backupTable.cellSet(), containsInAnyOrder(CELL_1, CELL_2));
    }

    @Test
    public void restore_command_restores_backedup_state() throws Exception {
        // initial table state
        setupSourceTableToContain(CELL_1, CELL_2);

        // backup creation
        DoBackupCommand doBackupCommand = new DoBackupCommand(BACKUP_CONFIGURATION, printerMock);
        doBackupCommand.run();
        Date backupDate = getJustCreatedBackupDate(printerMock);

        // table update
        Table<String, String, String> sourceTable = sourceTableFactory.getSourceTable();
        sourceTable.put(ROW_3, COLUMN_3, VALUE_3);

        // restore
        RestoreCommand restoreCommand = new RestoreCommand(BACKUP_CONFIGURATION, printerMock, backupDate.getTime());
        restoreCommand.run();

        // check state prior to update
        assertThat(sourceTable.cellSet(), containsInAnyOrder(CELL_1, CELL_2));
    }

    @Test
    public void list_command_lists_all_backups() throws Exception {
        setupSourceTableToContain(CELL_1, CELL_2);

        // create backups
        DoBackupCommand doBackupCommand = new DoBackupCommand(BACKUP_CONFIGURATION, printerMock);
        doBackupCommand.run();
        Date backup1date = getJustCreatedBackupDate(printerMock);
        doBackupCommand.run();
        Date backup2date = getJustCreatedBackupDate(printerMock);

        // list backups
        ListBackupsCommand listBackupsCommand = new ListBackupsCommand(BACKUP_CONFIGURATION, printerMock, 0);
        listBackupsCommand.run();

        assertThatListedBackupsOnDates(backup1date, backup2date);
    }

    @Test
    public void delete_command_deletes_backups() throws Exception {
        setupSourceTableToContain(CELL_1, CELL_2);

        // create backups
        DoBackupCommand doBackupCommand = new DoBackupCommand(BACKUP_CONFIGURATION, printerMock);
        doBackupCommand.run();
        Date backup1date = getJustCreatedBackupDate(printerMock);
        doBackupCommand.run();
        Date backup2date = getJustCreatedBackupDate(printerMock);


        // delete backups
        DeleteBackupsCommand deleteBackupsCommand = new DeleteBackupsCommand(BACKUP_CONFIGURATION, printerMock, Long.MAX_VALUE);
        deleteBackupsCommand.run();

        assertNoBackupsOnDates(backup1date, backup2date);
    }

    @Test
    public void delete_bad_backups_command_deletes_only_bad_backups() throws Exception {
        setupSourceTableToContain(CELL_1, CELL_2);

        // create backups
        DoBackupCommand doBackupCommand = new DoBackupCommand(BACKUP_CONFIGURATION, printerMock);
        doBackupCommand.run();
        Date backup1date = getJustCreatedBackupDate(printerMock);
        doBackupCommand.run();
        Date backup2date = getJustCreatedBackupDate(printerMock);
        doBackupCommand.run();
        Date backup3date = getJustCreatedBackupDate(printerMock);

        setBackupOnDate(backup1date, Backup.BackupStatus.BEING_DELETED);
        setBackupOnDate(backup2date, Backup.BackupStatus.IN_PROGRESS);

        DeleteBadBackupsCommand deleteBadBackupsCommand = new DeleteBadBackupsCommand(BACKUP_CONFIGURATION, printerMock);
        deleteBadBackupsCommand.run();

        // deleted backups
        assertNoBackupsOnDates(backup1date, backup2date);
        assertBackupOnDateContainsCells(backup3date, CELL_1, CELL_2);
    }

    //
    // helper methods
    //

    private void assertNoBackups() throws Exception {
        (new ListBackupsCommand(BACKUP_CONFIGURATION, printerMock, 0)).run();

        verify(printerMock, never()).println(any(String.class));
    }

    private void clearDB() throws Exception {
        (new DeleteBackupsCommand(BACKUP_CONFIGURATION, printerMock, Long.MAX_VALUE)).run();
        sourceTableFactory.clearSourceTable();
    }

    /**
     * Requires DoBackupCommand to be run beforehand, it reads the backup data from the commands output
     * sent to printerMock.
     */
    private Table<String, String, String> getJustCreatedBackup(Printer printerMock) throws Exception {
        return backupTableFactory.getBackupTable(getJustCreatedBackupDate(printerMock), BACKUP_CONFIGURATION.getSourceTableName());
    }

    /**
     * Requires DoBackupCommand to be run beforehand, it reads the backup data from the commands output
     * sent to printerMock.
     */
    private Date getJustCreatedBackupDate(Printer printerMock) {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(printerMock, times(1)).println(stringCaptor.capture());

        Matcher matcher = BACKUP_CREATED_PATTERN.matcher(stringCaptor.getValue());
        assertThat(matcher.matches(), is(equalTo(true)));
        Long backupTimeStamp = Long.parseLong(matcher.group(1));
        reset(printerMock);
        return new Date(backupTimeStamp);
    }

    private void assertThatListedBackupsOnDates(Date... backupDates) throws Exception {
        // Lists backups, one backup should have been created, and we get its details from the printout
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(printerMock, times(backupDates.length)).println(stringCaptor.capture());


        List<Date> observedBackupDates = Lists.newArrayList();
        for (String backupListItem : stringCaptor.getAllValues()) {
            Matcher matcher = BACKUP_LIST_ITEM_PATTERN.matcher(backupListItem);
            matcher.matches();
            observedBackupDates.add(new Date(Long.parseLong(matcher.group(1))));
        }

        assertThat(observedBackupDates, containsInAnyOrder(backupDates));
        reset(printerMock);
    }

    private void assertNoBackupsOnDates(Date... backupDates) throws Exception {
        Table<String, Date, Backup.BackupStatus> backupListTable = backupTableFactory.getBackupListTable();
        for (Date backupDate : backupDates) {
            assertThat(backupListTable.get(SRC_TABLE_NAME, backupDate), is(nullValue()));
            assertThat(backupTableFactory.getBackupTable(backupDate, SRC_TABLE_NAME), is(nullValue()));
        }
    }

    private void assertBackupOnDateContainsCells(Date backupDate, Table.Cell<String, String, String>... cells) {
        Table<String, Date, Backup.BackupStatus> backupListTable = backupTableFactory.getBackupListTable();
        assertThat(backupListTable.get(SRC_TABLE_NAME, backupDate), is(equalTo(Backup.BackupStatus.COMPLETED)));
        Table<String, String, String> backupTable = backupTableFactory.getBackupTable(backupDate, SRC_TABLE_NAME);
        assertThat(backupTable, is(notNullValue()));
        assertThat(backupTable.cellSet(), containsInAnyOrder(cells));
    }

    private void setBackupOnDate(Date backupDate, Backup.BackupStatus status) {
        Table<String, Date, Backup.BackupStatus> backupListTable = backupTableFactory.getBackupListTable();
        backupListTable.put(SRC_TABLE_NAME, backupDate, status);
    }

    private void setupSourceTableToContain(Table.Cell<String, String, String>... cells) {
        Table<String, String, String> sourceTable = sourceTableFactory.getSourceTable();
        for (Table.Cell<String, String, String> cell : cells) {
            sourceTable.put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
        }
    }


}
