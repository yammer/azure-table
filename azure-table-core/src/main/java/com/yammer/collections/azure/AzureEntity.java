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

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

/**
 * Internal class, not to be used outside. Requires to be public with a public constructor due to the Azure library using reflection for
 * serialization/deserialization,
 */
public class AzureEntity extends TableServiceEntity {
    public static final String VALUE = "Value";
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    private String value; // cannot be final

    public AzureEntity() { // needed by azure java api
    }

    public AzureEntity(String rowKey, String columnKey, String value) {
        partitionKey = rowKey;
        this.rowKey = columnKey;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}