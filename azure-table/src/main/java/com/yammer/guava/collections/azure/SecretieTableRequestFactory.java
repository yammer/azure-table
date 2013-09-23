package com.yammer.guava.collections.azure;

import com.microsoft.windowsazure.services.table.client.TableOperation;
import com.microsoft.windowsazure.services.table.client.TableQuery;
import com.yammer.secretie.api.model.Key;
import com.yammer.secretie.api.model.Secret;
// TODO to be removed

/* package */ class SecretieTableRequestFactory {


    TableOperation retrieve(String row, Key column) {
        return TableOperation.retrieve(row, AzureKeyCodec.encode(column), SecretieEntity.class);
    }

    TableOperation put(String row, Key key, Secret secret) {
        SecretieEntity secretieEntity = new SecretieEntity(row, key, secret);
        return TableOperation.insertOrReplace(secretieEntity);
    }

    TableOperation delete(SecretieEntity secretieEntity) {
        return TableOperation.delete(secretieEntity);
    }

    TableQuery<SecretieEntity> selectAll(String secretieTableName) {
        return TableQuery.from(secretieTableName, SecretieEntity.class);
    }

}
