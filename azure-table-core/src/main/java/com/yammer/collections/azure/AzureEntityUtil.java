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

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.microsoft.windowsazure.services.core.storage.utils.Base64;

import java.io.UnsupportedEncodingException;

final class AzureEntityUtil {
    static final Function<AzureEntity, String> EXTRACT_VALUE = new Function<AzureEntity, String>() {
        @Override
        public String apply(AzureEntity input) {
            return decode(input.getValue());
        }
    };
    private static final String ENCODING = "UTF-8";

    private AzureEntityUtil() {
    }

    static String encode(String stringToBeEncoded) {
        try {
            return Base64.encode(stringToBeEncoded.getBytes(ENCODING));
        } catch (UnsupportedEncodingException e) {
            // shouldn't happen but
            throw Throwables.propagate(e);
        }
    }

    static String decode(String stringToBeDecoded) {
        try {
            return new String(Base64.decode(stringToBeDecoded), ENCODING);
        } catch (UnsupportedEncodingException e) {
            // shouldn't happen but
            throw Throwables.propagate(e);
        }
    }

}
