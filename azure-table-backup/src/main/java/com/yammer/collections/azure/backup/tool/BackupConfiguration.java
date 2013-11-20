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

import com.fasterxml.jackson.annotation.JsonProperty;

public class BackupConfiguration {
    private final String sourceTableName;
    private final String sourceAccountName;
    private final String sourceAccountKey;
    private final String backupAccountName;
    private final String backupAccountKey;

    public BackupConfiguration(
            @JsonProperty("sourceTableName") String sourceTableName,
            @JsonProperty("sourceAccountName") String sourceAccountName,
            @JsonProperty("sourceAccountKey") String sourceAccountKey,
            @JsonProperty("backupAccountName") String backupAccountName,
            @JsonProperty("backupAccountKey") String backupAccountKey) {
        this.sourceTableName = sourceTableName;
        this.sourceAccountName = sourceAccountName;
        this.sourceAccountKey = sourceAccountKey;
        this.backupAccountName = backupAccountName;
        this.backupAccountKey = backupAccountKey;
    }

    public String getSourceAccountName() {
        return sourceAccountName;
    }

    public String getSourceAccountKey() {
        return sourceAccountKey;
    }

    public String getBackupAccountName() {
        return backupAccountName;
    }

    public String getBackupAccountKey() {
        return backupAccountKey;
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

}
