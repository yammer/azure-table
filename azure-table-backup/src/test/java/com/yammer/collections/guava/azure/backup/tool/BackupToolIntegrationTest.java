package com.yammer.collections.guava.azure.backup.tool;

import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.collections.guava.azure.backup.adapter.AzureBackupTableFactory;
import com.yammer.collections.guava.azure.backup.adapter.AzureSourceTableFactory;
import com.yammer.collections.guava.azure.backup.lib.Backup;
import com.yammer.collections.guava.azure.backup.lib.BackupTableFactory;
import com.yammer.collections.guava.azure.backup.lib.SourceTableFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * This test may take some time to run - deleting of tables in azure can take a while, and it is used during the restore procedure.
 * This test uses the commands used by the command line tool, and for that reason,
 * there is a degree of awkwardness in how assertions are made about backups - they have to be retrieved from commands printed output.
 */
@Ignore("Ignored as it talks to azure, should be used to integration test changes to this project")
@RunWith(MockitoJUnitRunner.class)
public class BackupToolIntegrationTest {
    private static final String CONFIG_FILE_PATH = BackupToolIntegrationTest.class.getResource("testBackupAccountConfiguration.yml").getPath();
    private static final String[] DO_BACKUP_COMMAND_LINE = {"-cf", CONFIG_FILE_PATH, "-b"};
    private static final String[] DELETE_ALL_BACKUPS_COMMAND_LINE = {"-cf", CONFIG_FILE_PATH, "-d", "" + Long.MAX_VALUE};
    private static final String[] DELETE_BAD_BACKUPS_COMMAND_LINE = {"-cf", CONFIG_FILE_PATH, "-db"};
    private static final String[] LIST_ALL_BACKUPS_COMMAND_LINE = {"-cf", CONFIG_FILE_PATH, "-l", "0"};
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
    private PrintStream infoPrintStreamMock;
    private SourceTableFactory sourceTableFactory;
    private BackupTableFactory backupTableFactory;
    private BackupCLI backupCLI;

    private static BackupConfiguration createBackupConfiguration() {
        BackupConfiguration configuration =
                new BackupConfiguration(
                        SRC_TABLE_NAME,
                        ACCOUNT_NAME,
                        ACCOUNT_KEY,
                        ACCOUNT_NAME,
                        ACCOUNT_KEY
                );

        return configuration;
    }

    @Before
    public void setAzure() throws Exception {
        CloudTableClient sourceTableClient = CloudStorageAccount.parse(BACKUP_CONFIGURATION.getSourceConnectionString()).createCloudTableClient();
        sourceTableFactory = new AzureSourceTableFactory(sourceTableClient, BACKUP_CONFIGURATION.getSourceTableName());
        CloudTableClient backupTableClient = CloudStorageAccount.parse(BACKUP_CONFIGURATION.getBackupConnectionString()).createCloudTableClient();
        backupTableFactory = new AzureBackupTableFactory(backupTableClient);

        clearDB();

        assertNoBackups();
    }

    @Before
    public void setUpBackupCli() {
        backupCLI = new BackupCLI(infoPrintStreamMock, System.err);
    }

    @Test
    public void do_backup_command_backs_up_correctly() throws Exception {
        setupSourceTableToContain(CELL_1, CELL_2);

        backupCLI.execute(DO_BACKUP_COMMAND_LINE);

        Table<String, String, String> backupTable = getJustCreatedBackup(infoPrintStreamMock);

        assertThat(backupTable.cellSet(), containsInAnyOrder(CELL_1, CELL_2));
    }

    @Test
    public void restore_command_restores_backedup_state() throws Exception {
        // initial table state
        setupSourceTableToContain(CELL_1, CELL_2);

        // backup creation
        backupCLI.execute(DO_BACKUP_COMMAND_LINE);
        Date backupDate = getJustCreatedBackupDate(infoPrintStreamMock);

        // table update
        Table<String, String, String> sourceTable = sourceTableFactory.getSourceTable();
        sourceTable.put(ROW_3, COLUMN_3, VALUE_3);

        // restore
        final String[] restoreCommandLine = {"-cf", CONFIG_FILE_PATH, "-r", String.valueOf(backupDate.getTime())};
        backupCLI.execute(restoreCommandLine);

        // check state prior to update
        assertThat(sourceTable.cellSet(), containsInAnyOrder(CELL_1, CELL_2));
    }

    @Test
    public void list_command_lists_all_backups() throws Exception {
        setupSourceTableToContain(CELL_1, CELL_2);

        // create backups
        backupCLI.execute(DO_BACKUP_COMMAND_LINE);
        Date backup1date = getJustCreatedBackupDate(infoPrintStreamMock);
        backupCLI.execute(DO_BACKUP_COMMAND_LINE);
        Date backup2date = getJustCreatedBackupDate(infoPrintStreamMock);

        // list backups
        backupCLI.execute(LIST_ALL_BACKUPS_COMMAND_LINE);

        assertThatListedBackupsOnDates(backup1date, backup2date);
    }

    @Test
    public void delete_command_deletes_backups() throws Exception {
        setupSourceTableToContain(CELL_1, CELL_2);

        // create backups
        backupCLI.execute(DO_BACKUP_COMMAND_LINE);
        Date backup1date = getJustCreatedBackupDate(infoPrintStreamMock);
        backupCLI.execute(DO_BACKUP_COMMAND_LINE);
        Date backup2date = getJustCreatedBackupDate(infoPrintStreamMock);


        // delete backups
        backupCLI.execute(DELETE_ALL_BACKUPS_COMMAND_LINE);

        assertNoBackupsOnDates(backup1date, backup2date);
    }

    @Test
    public void delete_bad_backups_command_deletes_only_bad_backups() throws Exception {
        setupSourceTableToContain(CELL_1, CELL_2);

        // create backups
        backupCLI.execute(DO_BACKUP_COMMAND_LINE);
        Date backup1date = getJustCreatedBackupDate(infoPrintStreamMock);
        backupCLI.execute(DO_BACKUP_COMMAND_LINE);
        Date backup2date = getJustCreatedBackupDate(infoPrintStreamMock);
        backupCLI.execute(DO_BACKUP_COMMAND_LINE);
        Date backup3date = getJustCreatedBackupDate(infoPrintStreamMock);

        setBackupOnDate(backup1date, Backup.BackupStatus.BEING_DELETED);
        setBackupOnDate(backup2date, Backup.BackupStatus.IN_PROGRESS);

        backupCLI.execute(DELETE_BAD_BACKUPS_COMMAND_LINE);

        // deleted backups
        assertNoBackupsOnDates(backup1date, backup2date);
        assertBackupOnDateContainsCells(backup3date, CELL_1, CELL_2);
    }

    //
    // helper methods
    //

    private void assertNoBackups() throws Exception {
        new BackupCLI(System.out, System.err).execute(LIST_ALL_BACKUPS_COMMAND_LINE);
        verify(infoPrintStreamMock, never()).println(any(String.class));
    }

    private void clearDB() throws Exception {
        new BackupCLI(System.out, System.err).execute(DELETE_ALL_BACKUPS_COMMAND_LINE);
        sourceTableFactory.clearSourceTable();
    }

    /**
     * Requires DoBackupCommand to be run beforehand, it reads the backup data from the commands output
     * sent to infoPrintStreamMock.
     */
    private Table<String, String, String> getJustCreatedBackup(PrintStream infoPrintStreamMock) throws Exception {
        return backupTableFactory.getBackupTable(getJustCreatedBackupDate(infoPrintStreamMock), BACKUP_CONFIGURATION.getSourceTableName());
    }

    /**
     * Requires DoBackupCommand to be run beforehand, it reads the backup data from the commands output
     * sent to infoPrintStreamMock.
     */
    private Date getJustCreatedBackupDate(PrintStream infoPrintStreamMock) {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(infoPrintStreamMock, times(1)).println(stringCaptor.capture());

        Matcher matcher = BACKUP_CREATED_PATTERN.matcher(stringCaptor.getValue());
        assertThat(matcher.matches(), is(equalTo(true)));
        Long backupTimeStamp = Long.parseLong(matcher.group(1));
        reset(infoPrintStreamMock);
        return new Date(backupTimeStamp);
    }

    private void assertThatListedBackupsOnDates(Date... backupDates) throws Exception {
        // Lists backups, one backup should have been created, and we get its details from the printout
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(infoPrintStreamMock, times(backupDates.length)).println(stringCaptor.capture());


        List<Date> observedBackupDates = Lists.newArrayList();
        for (String backupListItem : stringCaptor.getAllValues()) {
            Matcher matcher = BACKUP_LIST_ITEM_PATTERN.matcher(backupListItem);
            matcher.matches();
            observedBackupDates.add(new Date(Long.parseLong(matcher.group(1))));
        }

        assertThat(observedBackupDates, containsInAnyOrder(backupDates));
        reset(infoPrintStreamMock);
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
