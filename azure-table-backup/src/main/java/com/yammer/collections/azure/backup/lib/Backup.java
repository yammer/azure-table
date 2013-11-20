/**
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED WARRANTIES OR CONDITIONS
 * OF TITLE, FITNESS FOR A PARTICULAR PURPOSE, MERCHANTABLITY OR NON-INFRINGEMENT.
 *
 * See the Apache Version 2.0 License for specific language governing permissions and limitations under
 * the License.
 */
package com.yammer.collections.azure.backup.lib;

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
