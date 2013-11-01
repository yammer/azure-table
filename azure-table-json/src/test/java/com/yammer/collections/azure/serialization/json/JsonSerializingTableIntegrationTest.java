package com.yammer.collections.azure.serialization.json;

import com.google.common.collect.Table;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTable;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.collections.azure.BaseAzureTable;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@SuppressWarnings({"InstanceVariableMayNotBeInitialized", "ConstantNamingConvention"})
@Ignore("This talks directly to azure so ignored by default")
public class JsonSerializingTableIntegrationTest {
    @SuppressWarnings("ConstantNamingConvention")
    private static final String ACCOUNT_NAME = "secretietest";
    private static final String ACCOUNT_KEY = "e5LnQoZei2cFH+56TFxDmO6AhnzMKill1NyVUs1M3R7OFNfCLnIGe17TLUex0mYYGQFjNvmArsLa8Iq3b0FNAg==";
    private static final String CONNECTION_STRING = String.format("DefaultEndpointsProtocol=http;AccountName=%s;AccountKey=%s", ACCOUNT_NAME, ACCOUNT_KEY);
    private static final String TABLE_NAME = "jsonTableIntegrationTest";
    private static final Float ROW = 11.34f;
    private static final Long COLUMN = 123l;
    private static final TestValuePojo VALUE = new TestValuePojo("Michal", Arrays.asList(29, 1, 1980));
    private static final String SERIALIED_ROW = ROW.toString();
    private static final String SERIALIZED_COLUMN = COLUMN.toString();
    private static final String SERIALIZED_VALUE = "{\"name\":\"Michal\",\"numbers\":[29,1,1980]}";
    private Table<String, String, String> backingTable;
    private Table<Float, Long, TestValuePojo> jsonSerializingTable;

    @Before
    public void setUp() throws URISyntaxException, InvalidKeyException, StorageException {
        CloudTableClient cloudTableClient = CloudStorageAccount.parse(CONNECTION_STRING).createCloudTableClient();
        CloudTable table = cloudTableClient.getTableReference(TABLE_NAME);
        table.createIfNotExist();
        backingTable = BaseAzureTable.create(TABLE_NAME, cloudTableClient);
        jsonSerializingTable = JsonSerializingTable.create(
                backingTable, Float.class, Long.class, TestValuePojo.class);

        backingTable.clear();
    }

    @After
    public void cleanUp() {
        backingTable.clear();
    }

    @Test
    public void put_correctly_serializes() {
        jsonSerializingTable.put(ROW, COLUMN, VALUE);

        assertThat(backingTable.get(SERIALIED_ROW, SERIALIZED_COLUMN), is(equalTo(SERIALIZED_VALUE)));
    }

    @Test
    public void get_correctly_deserializes() {
        backingTable.put(SERIALIED_ROW, SERIALIZED_COLUMN, SERIALIZED_VALUE);

        assertThat(jsonSerializingTable.get(ROW, COLUMN), is(equalTo(VALUE)));
    }

}
