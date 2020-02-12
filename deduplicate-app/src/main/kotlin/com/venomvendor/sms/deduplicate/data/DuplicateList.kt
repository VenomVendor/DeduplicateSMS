package com.venomvendor.sms.deduplicate.data

import java.util.ArrayList

class DuplicateList<T>(collection: Collection<T>) : ArrayList<T>(collection) {

    fun splice(fromIndex: Int, toIndex: Int): List<T> {
        val splicedList: List<T> = DuplicateList(subList(fromIndex, toIndex))
        removeRange(fromIndex, splicedList.size)
        return splicedList
    }
}
