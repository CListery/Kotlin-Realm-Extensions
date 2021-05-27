package com.yh.krealmextensions

import android.os.Handler
import android.os.Looper
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmChangeListener
import io.realm.RealmModel
import io.realm.RealmResults
import io.realm.kotlin.addChangeListener
import io.realm.kotlin.removeChangeListener
import io.realm.OrderedRealmCollectionChangeListener as OrderedRealmCollectionChangeListener1

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
        result.addChangeListener(object : OrderedRealmCollectionChangeListener1<RealmResults<T>> {
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

fun onLooperThread(block: () -> Unit) {
    if(Looper.myLooper() != null) {
        block()
    } else {
        Handler(getLooper()).post(block)
    }
}
