package com.yammer.guava.collections.backup.tool;


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
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
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
    private static final BackupConfiguration BACKUP_CONFIGURATION = createBackupConfiguration();
    private static final String SRC_TABLE_NAME = "backupToolIntegrationTestTable";
    private static final String BACKUP_INFO_PATTERN_STRING = "Backup: NAME="+SRC_TABLE_NAME+".*TIMESTAMP=(\\d*) STATUS="+ Backup.BackupStatus.COMPLETED;
    private static final Pattern BACKUP_CREATED_PATTERN = Pattern.compile("Created. "+BACKUP_INFO_PATTERN_STRING);
    private static final String ACCOUNT_NAME = "secretietest";
    private static final String ACCOUNT_KEY = "e5LnQoZei2cFH+56TFxDmO6AhnzMKill1NyVUs1M3R7OFNfCLnIGe17TLUex0mYYGQFjNvmArsLa8Iq3b0FNAg==";
    private static final String ROW_1 = "row1";
    private static final String ROW_2 = "row2";
    private static final String COLUMN_1 = "column1";
    private static final String COLUMN_2 = "column2";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";
    private static final Table.Cell<String, String, String> CELL_1 = Tables.immutableCell(ROW_1, COLUMN_1, VALUE_1);
    private static final Table.Cell<String, String, String> CELL_2 = Tables.immutableCell(ROW_2, COLUMN_2, VALUE_2);
    private static final String ROW_3 = "row3";
    private static final String ROW_4 = "row4";
    private static final String COLUMN_3 = "column3";
    private static final String COLUMN_4 = "column4";
    private static final String VALUE_3 = "value3";
    private static final String VALUE_4 = "value4";
    private static final Table.Cell<String, String, String> CELL_3 = Tables.immutableCell(ROW_3, COLUMN_3, VALUE_3);
    private static final Table.Cell<String, String, String> CELL_4 = Tables.immutableCell(ROW_4, COLUMN_4, VALUE_4);



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
        CloudTableClient sourceTableClient =  CloudStorageAccount.parse(BACKUP_CONFIGURATION.getSourceConnectionString()).createCloudTableClient();
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

        Table<String, String, String> backupTable = getJustCreatedBackup();

        assertThat(backupTable.cellSet(), containsInAnyOrder(CELL_1, CELL_2));
    }



    @Test
    public void restore_command_restores_backedup_state() throws Exception {
        // initial table state
        setupSourceTableToContain(CELL_1, CELL_2);

        // backup creation
        DoBackupCommand doBackupCommand = new DoBackupCommand(BACKUP_CONFIGURATION, printerMock);
        doBackupCommand.run();
        Date backupDate = getJustCreatedBackupDate();

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
    public void list_command_lists_all_backups() {

    }

    @Test
    public void delete_command_deletes_specified_backups() {

    }

    @Test
    public void delete_bad_backups_command_deletes_only_bad_backups() {

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
    private Table<String, String, String> getJustCreatedBackup() throws Exception {
        return backupTableFactory.getBackupTable(getJustCreatedBackupDate(), BACKUP_CONFIGURATION.getSourceTableName());
    }

    /**
     * Requires DoBackupCommand to be run beforehand, it reads the backup data from the commands output
     * sent to printerMock.
     */
    private Date getJustCreatedBackupDate() {
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(printerMock, times(1)).println(stringCaptor.capture());

        Matcher matcher = BACKUP_CREATED_PATTERN.matcher(stringCaptor.getValue());
        assertThat(matcher.matches(), is(equalTo(true)));
        Long backupTimeStamp = Long.parseLong(matcher.group(1));

        return new Date(backupTimeStamp);
    }

//    private Table<String, String, String> assertBackupCreatedAndRetrieveItsTable() throws Exception {
//        // TODO remove, redo this method
//        // Lists backups, one backup should have been created, and we get its details from the printout
//        (new ListBackupsCommand(BACKUP_CONFIGURATION, printerMock, 0)).run();
//        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
//        verify(printerMock, times(1)).println(stringCaptor.capture());
//
//        Matcher matcher = BACKUP_INFO_PATTERN.matcher(stringCaptor.capture());
//
//        Long backupTimeStamp = Long.parseLong(matcher.group(1));
//
//        return backupTableFactory.getBackupTable(new Date(backupTimeStamp), BACKUP_CONFIGURATION.getSourceTableName());
//    }

    private void setupSourceTableToContain(Table.Cell<String, String, String>... cells) {
        Table<String, String, String> sourceTable = sourceTableFactory.getSourceTable();
        for(Table.Cell<String, String, String> cell : cells) {
            sourceTable.put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
        }
    }


}
