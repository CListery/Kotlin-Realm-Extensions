package com.yh.krealmextensions

import io.realm.OrderedCollectionChangeSet
import io.realm.RealmModel

class RealmResultChangeSet<T : RealmModel>(
    val setState: OrderedCollectionChangeSet.State = OrderedCollectionChangeSet.State.INITIAL,
    val insert: List<T> = emptyList(),
    val change: List<T> = emptyList(),
    val delete: List<T> = emptyList(),
    val error: Throwable? = null
) {
    
    val isOk get() = null == error && OrderedCollectionChangeSet.State.ERROR != setState
}