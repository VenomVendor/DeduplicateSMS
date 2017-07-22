package com.venomvendor.sms.deduplicate.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DuplicateList<T> extends ArrayList<T> {

    public DuplicateList(Collection<? extends T> collection) {
        super(collection);
    }

    public List<T> splice(int fromIndex, int toIndex) {
        DuplicateList<T> splicedList = new DuplicateList<>(subList(fromIndex, toIndex));
        removeRange(fromIndex, splicedList.size());
        return splicedList;
    }
}
