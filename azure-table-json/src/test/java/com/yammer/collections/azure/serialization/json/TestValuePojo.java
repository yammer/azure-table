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
package com.yammer.collections.azure.serialization.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

@SuppressWarnings("UnusedDeclaration")
public class TestValuePojo {
    private final String name;
    private final Collection<Integer> numbers;


    public TestValuePojo(
            @JsonProperty("name") String name,
            @JsonProperty("numbers") Collection<Integer> numbers) {
        this.name = name;
        this.numbers = numbers;
    }

    @Override
    public String toString() {
        return "TestValuePojo{" +
                "name='" + name + '\'' +
                ", numbers=" + numbers +
                '}';
    }

    public String getName() {
        return name;
    }

    public Collection<Integer> getNumbers() {
        return numbers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestValuePojo that = (TestValuePojo) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (numbers != null ? !numbers.equals(that.numbers) : that.numbers != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (numbers != null ? numbers.hashCode() : 0);
        return result;
    }
}
