package com.yh.krealmextensions

import android.os.Handler
import android.os.Looper
import io.realm.OrderedCollectionChangeSet
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.RealmChangeListener
import io.realm.RealmModel
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.addChangeListener
import io.realm.kotlin.removeChangeListener

/**
 * Extensions for Realm. All methods here are asynchronous, and only notify changes once.
 */

/**
 * Returns first entity in database asynchronously.
 */
fun <T : RealmModel> T.queryFirstAsync(callback: (T?) -> Unit) = queryFirstAsync(callback, this.javaClass)

inline fun <reified T : RealmModel> queryFirstAsync(noinline callback: (T?) -> Unit) = queryFirstAsync(callback, T::class.java)

@PublishedApi
internal fun <T : RealmModel> queryFirstAsync(callback: (T?) -> Unit, javaClass: Class<T>) {
    onLooperThread {
        val realm = getRealmInstance(javaClass)
        
        val result = realm.where(javaClass).findFirstAsync()
        result.addChangeListener(object : RealmChangeListener<T> {
            override fun onChange(t: T) {
                result.removeChangeListener(this)
                realm.use {
                    callback.invoke(realm.copy(t))
                }
                if(isRealmThread()) {
                    Looper.myLooper()?.thread?.interrupt()
                }
            }
        })
    }
}

/**
 * Returns last entity in database asynchronously.
 */
fun <T : RealmModel> T.queryLastAsync(callback: (T?) -> Unit) {
    onLooperThread {
        val realm = getRealmInstance(javaClass)
        
        val result = realm.where(javaClass).findAllAsync()
        result.addChangeListener(object : RealmChangeListener<RealmResults<T>> {
            override fun onChange(rr: RealmResults<T>) {
                result.removeChangeListener(this)
                realm.use {
                    callback.invoke(realm.copy(rr.lastOrNull()))
                }
                if(isRealmThread()) {
                    Looper.myLooper()?.thread?.interrupt()
                }
            }
            
        })
    }
}

inline fun <reified T : RealmModel> queryLastAsync(crossinline callback: (T?) -> Unit) {
    onLooperThread {
        val realm = getRealmInstance(T::class.java)
        
        val result = realm.where(T::class.java).findAllAsync()
        result.addChangeListener(object : RealmChangeListener<RealmResults<T>> {
            override fun onChange(rr: RealmResults<T>) {
                result.removeChangeListener(this)
                realm.use {
                    callback.invoke(realm.copy(rr.lastOrNull()))
                }
                if(isRealmThread()) {
                    Looper.myLooper()?.thread?.interrupt()
                }
            }
        })
    }
}

fun <T : RealmModel> T.querySortedAsync(callback: (List<T>) -> Unit, fieldName: String, order: Sort, query: Query<T>?) =
    querySortedAsync(callback, listOf(fieldName), listOf(order), query, this.javaClass)
inline fun <reified T : RealmModel> querySortedAsync(noinline callback: (List<T>) -> Unit, fieldName: String, order: Sort, noinline query: Query<T>?) =
    querySortedAsync(callback, listOf(fieldName), listOf(order), query, T::class.java)

fun <T : RealmModel> T.querySortedAsync(callback: (List<T>) -> Unit, fieldName: List<String>, order: List<Sort>, query: Query<T>?) =
    querySortedAsync(callback, fieldName, order, query, this.javaClass)
inline fun <reified T : RealmModel> querySortedAsync(noinline callback: (List<T>) -> Unit, fieldName: List<String>, order: List<Sort>, noinline query: Query<T>?) =
    querySortedAsync(callback, fieldName, order, query, T::class.java)

fun <T: RealmModel> T.querySortedAsync(callback: (List<T>) -> Unit, fieldName: String, order: Sort) =
    querySortedAsync(callback, listOf(fieldName), listOf(order), null, this.javaClass)
inline fun <reified T:RealmModel> querySortedAsync(noinline callback: (List<T>) -> Unit, fieldName: String, order: Sort) =
    querySortedAsync(callback, listOf(fieldName), listOf(order), null, T::class.java)

fun <T: RealmModel> T.querySortedAsync(callback: (List<T>) -> Unit, fieldName: List<String>, order: List<Sort>) =
    querySortedAsync(callback, fieldName, order, null, this.javaClass)
inline fun <reified T:RealmModel> querySortedAsync(noinline callback: (List<T>) -> Unit, fieldName: List<String>, order: List<Sort>) =
    querySortedAsync(callback, fieldName, order, null, T::class.java)

@PublishedApi
internal fun <T : RealmModel> querySortedAsync(callback: (List<T>) -> Unit, fieldName: List<String>, order: List<Sort>, query: Query<T>?, javaClass: Class<T>) {
    onLooperThread {
        val realm = getRealmInstance(javaClass)
        
        val realmQuery = realm.where(javaClass)
        query?.invoke(realmQuery)
        val result = realmQuery.findAllAsync().sort(fieldName.toTypedArray(), order.toTypedArray())
        result.addChangeListener(object : RealmChangeListener<RealmResults<T>> {
            override fun onChange(t: RealmResults<T>) {
                result.removeChangeListener(this)
                realm.use {
                    callback.invoke(realm.copy(t))
                }
                if(isRealmThread()) {
                    Looper.myLooper()?.thread?.interrupt()
                }
            }
        })
    }
}

/**
 * Returns all entities in database asynchronously.
 */
fun <T : RealmModel> T.queryAllAsync(callback: (List<T>) -> Unit) = queryAllAsync(callback, this.javaClass)

inline fun <reified T : RealmModel> queryAllAsync(noinline callback: (List<T>) -> Unit) = queryAllAsync(callback, T::class.java)

@PublishedApi
internal fun <T : RealmModel> queryAllAsync(callback: (List<T>) -> Unit, javaClass: Class<T>) {
    onLooperThread {
        val realm = getRealmInstance(javaClass)
        
        val result = realm.where(javaClass).findAllAsync()
        result.addChangeListener(object : RealmChangeListener<RealmResults<T>> {
            override fun onChange(t: RealmResults<T>) {
                result.removeChangeListener(this)
                realm.use {
                    callback.invoke(realm.copy(t))
                }
                if(isRealmThread()) {
                    Looper.myLooper()?.thread?.interrupt()
                }
            }
        })
    }
}

@PublishedApi
internal fun <T : RealmModel> queryAllAsync(callback: (List<T>, OrderedCollectionChangeSet) -> Unit, javaClass: Class<T>) {
    onLooperThread {
        val realm = getRealmInstance(javaClass)
        
        val result = realm.where(javaClass).findAllAsync()
        result.addChangeListener(object : OrderedRealmCollectionChangeListener<RealmResults<T>> {
            override fun onChange(t: RealmResults<T>, changeSet: OrderedCollectionChangeSet) {
                result.removeChangeListener(this)
                realm.use {
                    callback.invoke(realm.copy(t), changeSet)
                }
                if(isRealmThread()) {
                    Looper.myLooper()?.thread?.interrupt()
                }
            }
        })
    }
}

/**
 * Queries for entities in database asynchronously.
 */
fun <T : RealmModel> T.queryAsync(query: Query<T>, callback: (List<T>) -> Unit) = queryAsync(query, callback, this.javaClass)

inline fun <reified T : RealmModel> queryAsync(noinline query: Query<T>, noinline callback: (List<T>) -> Unit) = queryAsync(query, callback, T::class.java)

@PublishedApi
internal fun <T : RealmModel> queryAsync(query: Query<T>, callback: (List<T>) -> Unit, javaClass: Class<T>) {
    onLooperThread {
        val realm = getRealmInstance(javaClass)
        
        val realmQuery = realm.where(javaClass)
        query.invoke(realmQuery)
        val result = realmQuery.findAllAsync()
        result.addChangeListener(object : RealmChangeListener<RealmResults<T>> {
            override fun onChange(t: RealmResults<T>) {
                result.removeChangeListener(this)
                realm.use {
                    callback.invoke(realm.copy(t))
                }
                if(isRealmThread()) {
                    Looper.myLooper()?.thread?.interrupt()
                }
            }
        })
    }
}

@PublishedApi
internal fun <T : RealmModel> queryAllAsync(
    query: Query<T>? = null,
    javaClass: Class<T>,
    callback: (insert: List<T>, change: List<T>, delete: List<T>, error: Throwable?) -> Unit
) {
    onLooperThread {
        val realm = getRealmInstance(javaClass)
        val realmQuery = realm.where(javaClass)
        query?.invoke(realmQuery)
        val result = realmQuery.findAllAsync()
        
        result.addChangeListener(object : OrderedRealmCollectionChangeListener<RealmResults<T>> {
            override fun onChange(t: RealmResults<T>, changeSet: OrderedCollectionChangeSet) {
                result.removeChangeListener(this)
                when(changeSet.state) {
                    OrderedCollectionChangeSet.State.UPDATE -> {
                        val safeResult = realm.copy(t)
                        val insert = safeResult.filterIndexed { index, _ -> changeSet.insertions.contains(index) }
                        val change = safeResult.filterIndexed { index, _ -> changeSet.changes.contains(index) }
                        val delete = safeResult.filterIndexed { index, _ -> changeSet.deletions.contains(index) }
                        
                        callback.invoke(insert, change, delete, null)
                    }
                    OrderedCollectionChangeSet.State.ERROR  -> {
                        val exception = changeSet.error
                        if(null != exception) {
                            callback.invoke(emptyList(), emptyList(), emptyList(), exception)
                        }
                    }
                    else                                    -> {
                    }
                }
            }
        })
    }
}

fun onLooperThread(block: () -> Unit) {
    if(Looper.myLooper() != null) {
        block()
    } else {
        Handler(getLooper()).post(block)
    }
}
