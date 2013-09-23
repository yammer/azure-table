package com.yammer.guava.collections.azure;

import com.yammer.secretie.api.model.Key;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

// TODO to be removed
public class AzureKeyCodec {
    private static final String ENCODING = "UTF-8";

    public static String encode(Key key) {
        try {
            return Base64.encodeBase64String(key.toString().getBytes(ENCODING));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // should not happen
        }
    }


    public static Key decode(String key) {
        try {
            return new Key(new String(Base64.decodeBase64(key), ENCODING));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // should not happen
        }
    }
}
