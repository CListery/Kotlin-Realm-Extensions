package com.yh.krealmextensions

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmModel

fun <T : RealmModel> Realm.copy(realmObject: T?, maxDepth: Int = Int.MAX_VALUE): T? {
    return when(realmObject) {
        is T -> {
            if(realmObject.isValidWithSafe()) {
                copyFromRealm(realmObject, maxDepth)
            } else {
                null
            }
        }
        else -> null
    }
}

fun <T : RealmModel> Realm.copy(realmObjects: Iterable<T?>?, maxDepth: Int = Int.MAX_VALUE): List<T> {
    return when(realmObjects) {
        is Iterable -> copyFromRealm(
            realmObjects.filter { it.isValidWithSafe() },
            maxDepth
        ).filterNotNull().toList()
        else        -> emptyList()
    }
}

fun RealmConfiguration.Builder.applyUiThreadOption(enable: Boolean): RealmConfiguration.Builder {
    allowWritesOnUiThread(enable).allowQueriesOnUiThread(enable)
    return this
}
