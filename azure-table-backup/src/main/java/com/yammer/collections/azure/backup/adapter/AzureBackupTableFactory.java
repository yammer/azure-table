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
package com.yammer.collections.azure.backup.adapter;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Table;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.collections.azure.backup.lib.BackupTableFactory;
import com.yammer.collections.azure.util.AzureTables;

import java.util.Date;

import static com.yammer.collections.azure.backup.lib.Backup.BackupStatus;
import static com.yammer.collections.azure.util.AzureTables.tableWithName;

public class AzureBackupTableFactory implements BackupTableFactory {
    private static final String BACKUP_LIST_TABLE_NAME = "comYammerAzureTableBackupListTable";
    private static final String BACKUP_TABLE_NAME_TEMPLATE = "BackupNAME%sDATE%s";
    private static final Function<Date, String> DATE_SERIALIZER_FUNCTION = new Function<Date, String>() {
        @Override
        public String apply(Date input) {
            return Long.toString(input.getTime());
        }
    };
    private static final Function<String, Date> DATE_DESERIALIZER_FUNCTION = new Function<String, Date>() {
        @Override
        public Date apply(String input) {
            return new Date(Long.parseLong(input));
        }
    };
    @SuppressWarnings("ConstantNamingConvention")
    private static final Function<BackupStatus, String> BACKUP_STATUS_SERIALIZER_FUNCTION = new Function<BackupStatus, String>() {
        @Override
        public String apply(BackupStatus input) {
            return input.toString();
        }
    };
    @SuppressWarnings("ConstantNamingConvention")
    private static final Function<String, BackupStatus> BACKUP_STATUS_DESERIALIZER_FUNCTION = new Function<String, BackupStatus>() {
        @Override
        public BackupStatus apply(String input) {
            return BackupStatus.valueOf(input);
        }
    };
    private final CloudTableClient cloudTableClient;

    public AzureBackupTableFactory(CloudTableClient cloudTableClient) {
        this.cloudTableClient = cloudTableClient;
    }

    private static String createBackupTableName(Date backupDate, String backupName) {
        return String.format(BACKUP_TABLE_NAME_TEMPLATE, backupName, backupDate.getTime());
    }

    @Override
    public Table<String, Date, BackupStatus> getBackupListTable() {
        try {
            return tableWithName(BACKUP_LIST_TABLE_NAME).
                    using(cloudTableClient).
                    createIfDoesNotExist().
                    buildUsingCustomSerialization(
                            Functions.<String>identity(), Functions.<String>identity(),
                            DATE_SERIALIZER_FUNCTION, DATE_DESERIALIZER_FUNCTION,
                            BACKUP_STATUS_SERIALIZER_FUNCTION, BACKUP_STATUS_DESERIALIZER_FUNCTION);
        } catch (StorageException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Table<String, String, String> createBackupTable(Date backupDate, String backupName) {
        try {
            return tableWithName(createBackupTableName(backupDate, backupName)).
                    using(cloudTableClient).
                    createIfDoesNotExist().
                    buildWithNoSerialization();
        } catch (StorageException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void removeTable(Date backupDate, String backupName) {
        try {
            tableWithName(createBackupTableName(backupDate, backupName)).
                    using(cloudTableClient).
                    deleteIfExists();
        } catch (StorageException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Table<String, String, String> getBackupTable(Date backupDate, String backupName) {
        try {
            Optional<AzureTables.TableBuilder> tableBuilder =
                    tableWithName(createBackupTableName(backupDate, backupName)).
                            using(cloudTableClient).
                            ifExists();
            if (tableBuilder.isPresent()) {
                return tableBuilder.get().buildWithNoSerialization();
            }
            return null;
        } catch (StorageException e) {
            throw Throwables.propagate(e);
        }
    }

}
