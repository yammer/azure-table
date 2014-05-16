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
package com.yammer.collections.azure;


import com.google.common.base.Throwables;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;

import java.io.FileInputStream;
import java.util.Properties;

public final class AzureTestCloudTableClientFactory {
    private static final String PROPERTIES_FILE = "azure-table-test-connection.properties";
    private static final String ACCOUNT_NAME = "account.name";
    private static final String ACCOUNT_KEY = "account.key";
    private static final String CONNECTION_STRING = "DefaultEndpointsProtocol=http;AccountName=%s;AccountKey=%s";

    private AzureTestCloudTableClientFactory() {
    }

    public static CloudTableClient create() {

        //noinspection OverlyBroadCatchBlock
        try {
            Properties azureTableTestAccountConnectionProperties = new Properties();
            //noinspection NestedTryStatement
            try (FileInputStream fis = new FileInputStream(BaseAzureTableIT.class.getResource(PROPERTIES_FILE).getPath())) {
                azureTableTestAccountConnectionProperties.load(fis);

            }
            String connectionString = String.format(CONNECTION_STRING,
                    azureTableTestAccountConnectionProperties.getProperty(ACCOUNT_NAME),
                    azureTableTestAccountConnectionProperties.getProperty(ACCOUNT_KEY));
            return CloudStorageAccount.parse(connectionString).createCloudTableClient();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
