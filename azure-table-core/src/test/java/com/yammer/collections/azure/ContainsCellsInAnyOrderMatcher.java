package com.yammer.collections.azure;

import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Arrays;


class ContainsCellsInAnyOrderMatcher extends BaseMatcher<Iterable<Table.Cell<byte[], byte[], byte[]>>> {


    private final Table.Cell<byte[], byte[], byte[]>[] cells;

    private ContainsCellsInAnyOrderMatcher(Table.Cell<byte[], byte[], byte[]>[] cells) {
        this.cells = cells;
    }

    @SafeVarargs
    static Matcher<Iterable<Table.Cell<byte[], byte[], byte[]>>> containsCellsInAnyOrder(final Table.Cell<byte[], byte[], byte[]>... cells) {
        return new ContainsCellsInAnyOrderMatcher(cells);
    }

    @Override
    public boolean matches(Object item) {
        if (!(item instanceof Iterable)) {
            return false;
        }

        Iterable<?> iterable = (Iterable<?>) item;
        if (cells.length != Iterables.size(iterable)) {
            return false;
        }

        for (Object thing : iterable) {
            if (!checkIfContained(thing, cells)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkIfContained(Object thing, Table.Cell<byte[], byte[], byte[]>[] cells) {
        try {
            Table.Cell<byte[], byte[], byte[]> cell = (Table.Cell<byte[], byte[], byte[]>) thing;
            for (Table.Cell<byte[], byte[], byte[]> candidateCell : cells) {
                if (Arrays.equals(cell.getRowKey(), candidateCell.getRowKey()) &&
                        Arrays.equals(cell.getColumnKey(), candidateCell.getColumnKey()) &&
                        Arrays.equals(cell.getValue(), candidateCell.getValue())) {
                    return true;
                }
            }
        } catch (ClassCastException e) {
            return false;
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValueList("", ",", "", cells);
    }
}
