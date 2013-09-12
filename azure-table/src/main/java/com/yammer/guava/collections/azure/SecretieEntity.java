package com.yammer.guava.collections.azure;

import com.microsoft.windowsazure.services.table.client.Ignore;
import com.microsoft.windowsazure.services.table.client.TableServiceEntity;
import com.yammer.secretie.api.model.Key;
import com.yammer.secretie.api.model.Secret;

// THIS should be turned into JSON operable class
public class SecretieEntity extends TableServiceEntity {
    private byte[] secret;

    public SecretieEntity() { // needed by azure java api
    }

    public SecretieEntity(String rowKey, Key columnKey, Secret secret) {
        this.partitionKey = rowKey;
        this.rowKey = AzureKeyCodec.encode(columnKey);
        this.secret = secret.asBytes();
    }

    @Ignore
    public Key getKey() {
        return AzureKeyCodec.decode(this.rowKey);
    }

    @Ignore
    public Secret getSecret() {
        return new Secret(this.secret);
    }

    public byte[] getSecretValue() {
        return secret;
    }

    public void setSecretValue(byte[] secret) {
        this.secret = secret;
    }
}
