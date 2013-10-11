package com.yammer.collections.guava.azure.backup.lib;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
public class TableCopyTest {
    private static final String ROW_1 = "row1";
    private static final String ROW_2 = "row2";
    private static final String COLUMN_1 = "column1";
    private static final String COLUMN_2 = "column2";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";
    private static final Table.Cell<String, String, String> CELL_1 = Tables.immutableCell(ROW_1, COLUMN_1, VALUE_1);
    private static final Table.Cell<String, String, String> CELL_2 = Tables.immutableCell(ROW_2, COLUMN_2, VALUE_2);
    private static final Exception RUNTIME_EXCEPTION = new RuntimeException();
    @Mock
    private Table<String, String, String> exceptionThrowingDestTable;
    private TableCopy<String, String, String> tableCopy;
    private Table<String, String, String> srcTable;
    private Table<String, String, String> dstTable;

    @Before
    public void setUp() {
        srcTable = HashBasedTable.create();
        srcTable.put(ROW_1, COLUMN_1, VALUE_1);
        srcTable.put(ROW_2, COLUMN_2, VALUE_2);

        dstTable = HashBasedTable.create();

        doThrow(RUNTIME_EXCEPTION).when(exceptionThrowingDestTable).put(any(String.class), any(String.class), any(String.class));

        tableCopy = new TableCopy<>();
    }

    @Test
    public void given_source_table_and_destination_table_backup_moves_all_entries() throws TableCopyException {
        assertThat(dstTable.cellSet().size(), is(equalTo(0)));

        tableCopy.perform(srcTable, dstTable);

        //noinspection unchecked
        assertThat(dstTable.cellSet(), containsInAnyOrder(CELL_1, CELL_2));
    }

    @Test(expected = TableCopyException.class)
    public void when_copy_throws_exception_then_table_copy_exception_thrown_with_that_as_cause() throws TableCopyException {
        try {
            tableCopy.perform(srcTable, exceptionThrowingDestTable);
        } catch (TableCopyException e) {
            assertThat(e.getCause(), is(equalTo((Throwable) RUNTIME_EXCEPTION)));
            throw e;
        }
    }

}
