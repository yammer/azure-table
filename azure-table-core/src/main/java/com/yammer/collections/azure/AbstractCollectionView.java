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
import com.google.common.collect.Iterables;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

abstract class AbstractCollectionView<E> extends AbstractCollection<E> {
    private final Function<AzureEntity, E> typeExtractor;

    AbstractCollectionView(Function<AzureEntity, E> typeExtractor) {
        this.typeExtractor = typeExtractor;
    }

    @Override
    public int size() {
        return Iterables.size(getBackingIterable());
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public boolean contains(Object o) {
        return o != null &&
                Iterables.contains(
                        Iterables.transform(getBackingIterable(), typeExtractor),
                        o);
    }

    protected abstract Iterable<AzureEntity> getBackingIterable();

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<E> iterator() {
        return Iterables.transform(
                getBackingIterable(),
                typeExtractor).iterator();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
