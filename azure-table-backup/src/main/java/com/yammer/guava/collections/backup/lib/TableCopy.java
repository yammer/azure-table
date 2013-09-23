package com.yammer.guava.collections.backup.lib;

import com.google.common.collect.Table;

import static com.google.common.base.Preconditions.checkNotNull;

public class TableCopy<R, C, V> {

    public void perform(Table<R, C, V> sourceTable, Table<R, C, V> backupTable) throws TableCopyException {
        checkNotNull(sourceTable);
        checkNotNull(backupTable);
        try {
            for (Table.Cell<R, C, V> cell : sourceTable.cellSet()) {
                backupTable.put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
            }
        } catch (Exception e) {
            throw new TableCopyException(e);
        }
    }
}
