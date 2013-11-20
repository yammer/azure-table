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
package com.yammer.collections.azure.backup.tool;


import com.yammer.collections.azure.backup.adapter.AzureBackupTableFactory;
import com.yammer.collections.azure.backup.adapter.AzureSourceTableFactory;
import com.yammer.collections.azure.backup.lib.BackupService;
import com.yammer.collections.azure.backup.lib.BackupTableFactory;
import com.yammer.collections.azure.backup.lib.SourceTableFactory;
import com.yammer.collections.azure.backup.lib.TableCopy;

import static com.yammer.collections.azure.util.AzureTables.clientForAccount;

public class BackupServiceFactory {

    private BackupTableFactory getBackupTableFactory(BackupConfiguration configuration) {
        return new AzureBackupTableFactory(
                clientForAccount(
                        configuration.getBackupAccountName(),
                        configuration.getBackupAccountKey()
                ).build()
        );
    }

    private SourceTableFactory getSourceTableFactory(BackupConfiguration configuration) {
        return new AzureSourceTableFactory(
                clientForAccount(
                        configuration.getSourceAccountName(),
                        configuration.getSourceAccountKey()
                ).build(),
                configuration.getSourceTableName());
    }

    public BackupService createBackupService(BackupConfiguration configuration) {
        BackupTableFactory backupTableFactory = getBackupTableFactory(configuration);
        SourceTableFactory sourceTableFactory = getSourceTableFactory(configuration);
        TableCopy<String, String, String> tableCopy = new TableCopy<>();
        return new BackupService(tableCopy, sourceTableFactory, backupTableFactory);
    }
}
