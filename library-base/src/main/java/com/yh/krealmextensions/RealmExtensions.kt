package com.yh.krealmextensions

import androidx.annotation.VisibleForTesting
import com.yh.krealmextensions.ext.safeAccess
import io.realm.*
import org.jetbrains.annotations.TestOnly
import java.lang.reflect.Field

typealias Query<T> = RealmQuery<T>.() -> Unit

/**
 * Extensions for Realm. All methods here are synchronous.
 */

fun <T : RealmModel> T.range(startPos: Int, endPos: Int): List<T> {
    getRealmInstance().use { realm ->
        val result = realm.where(javaClass).findAll()
        return result.loadRange(startPos, endPos)
    }
}

inline fun <reified T : RealmModel> range(startPos: Int, endPos: Int): List<T> {
    getRealmInstance<T>().use { realm ->
        val result = realm.where(T::class.java).findAll()
        return result.loadRange(startPos, endPos)
    }
}

/**
 * Query to the database with RealmQuery instance as argument
 */
fun <T : RealmModel> T.query(startPos: Int = -1, endPos: Int = -1, query: Query<T>): List<T> {
    getRealmInstance().use { realm ->
        val result = realm.where(javaClass).withQuery(query).findAll()
        return result.loadRange(startPos, endPos)
    }
}

/**
 * Query to the database with RealmQuery instance as argument
 */
inline fun <reified T : RealmModel> query(startPos: Int = -1, endPos: Int = -1, query: Query<T>): List<T> {
    getRealmInstance<T>().use { realm ->
        val result = realm.where(T::class.java).runQuery(query).findAll()
        return result.loadRange(startPos, endPos)
    }
}

/**
 * Query to the database with RealmQuery instance as argument and returns all items founded
 */
fun <T : RealmModel> T.queryAll(): List<T> {
    getRealmInstance().use { realm ->
        val result = realm.where(this.javaClass).findAll()
        return realm.copy(result)
    }
}

inline fun <reified T : RealmModel> queryAll(): List<T> {
    getRealmInstance<T>().use { realm ->
        val result = realm.where(T::class.java).findAll()
        return realm.copy(result)
    }
}

/**
 * Query to the database with RealmQuery instance as argument. Return first result, or null.
 */
fun <T : RealmModel> T.queryFirst(): T? {
    getRealmInstance().use { realm ->
        val item: T? = realm.where(this.javaClass).findFirst()
        return realm.copy(item)
    }
}

inline fun <reified T : RealmModel> queryFirst(): T? {
    getRealmInstance<T>().use { realm ->
        val item: T? = realm.where(T::class.java).findFirst()
        return realm.copy(item)
    }
}

/**
 * Query to the database with RealmQuery instance as argument. Return first result, or null.
 */
fun <T : RealmModel> T.queryFirst(query: Query<T>): T? {
    getRealmInstance().use { realm ->
        val item: T? = realm.where(this.javaClass).withQuery(query).findFirst()
        return realm.copy(item)
    }
}

inline fun <reified T : RealmModel> queryFirst(query: Query<T>): T? {
    getRealmInstance<T>().use { realm ->
        val item: T? = realm.where(T::class.java).runQuery(query).findFirst()
        return realm.copy(item)
    }
}

/**
 * Query to the database with RealmQuery instance as argument. Return last result, or null.
 */
fun <T : RealmModel> T.queryLast(): T? {
    getRealmInstance().use { realm ->
        val result = realm.where(this.javaClass).findAll().lastOrNull()
        return realm.copy(result)
    }
}

inline fun <reified T : RealmModel> queryLast(): T? {
    getRealmInstance<T>().use { realm ->
        val result = realm.where(T::class.java).findAll().lastOrNull()
        return realm.copy(result)
    }
}

/**
 * Query to the database with RealmQuery instance as argument. Return last result, or null.
 */
fun <T : RealmModel> T.queryLast(query: Query<T>): T? {
    getRealmInstance().use { realm ->
        val result = realm.where(this.javaClass).withQuery(query).findAll().firstOrNull()
        return realm.copy(result)
    }
}

inline fun <reified T : RealmModel> queryLast(query: Query<T>): T? {
    getRealmInstance<T>().use { realm ->
        val result = realm.where(T::class.java).runQuery(query).findAll().lastOrNull()
        return realm.copy(result)
    }
}

/**
 * Query to the database with RealmQuery instance as argument
 */
fun <T : RealmModel> T.querySorted(fieldName: String, order: Sort, startPos: Int = -1, endPos: Int = -1, query: Query<T>): List<T> {
    getRealmInstance().use { realm ->
        val result = realm.where(this.javaClass).withQuery(query).findAll().sort(fieldName, order)
        return result.loadRange(startPos, endPos)
    }
}

inline fun <reified T : RealmModel> querySorted(fieldName: String, order: Sort, startPos: Int = -1, endPos: Int = -1, query: Query<T>): List<T> {
    getRealmInstance<T>().use { realm ->
        val result = realm.where(T::class.java).runQuery(query).findAll().sort(fieldName, order)
        return result.loadRange(startPos, endPos)
    }
}

/**
 * Query to the database with a specific order and a RealmQuery instance as argument
 */
fun <T : RealmModel> T.querySorted(fieldName: List<String>, order: List<Sort>, startPos: Int = -1, endPos: Int = -1, query: Query<T>): List<T> {
    getRealmInstance().use { realm ->
        val result = realm.where(this.javaClass).withQuery(query).findAll().sort(fieldName.toTypedArray(), order.toTypedArray())
        return result.loadRange(startPos, endPos)
    }
}

inline fun <reified T : RealmModel> querySorted(fieldName: List<String>, order: List<Sort>, startPos: Int = -1, endPos: Int = -1, query: Query<T>): List<T> {
    getRealmInstance<T>().use { realm ->
        val result = realm.where(T::class.java).runQuery(query).findAll().sort(fieldName.toTypedArray(), order.toTypedArray())
        return result.loadRange(startPos, endPos)
    }
}

/**
 * Query to the database with a specific order
 */
fun <T : RealmModel> T.querySorted(fieldName: String, order: Sort, startPos: Int = -1, endPos: Int = -1): List<T> {
    getRealmInstance().use { realm ->
        val result = realm.where(this.javaClass).findAll().sort(fieldName, order)
        return result.loadRange(startPos, endPos)
    }
}

inline fun <reified T : RealmModel> querySorted(fieldName: String, order: Sort, startPos: Int = -1, endPos: Int = -1): List<T> {
    getRealmInstance<T>().use { realm ->
        val result = realm.where(T::class.java).findAll().sort(fieldName, order)
        return result.loadRange(startPos, endPos)
    }
}

/**
 * Query to the database with a specific order
 */
fun <T : RealmModel> T.querySorted(fieldName: List<String>, order: List<Sort>, startPos: Int = -1, endPos: Int = -1): List<T> {
    getRealmInstance().use { realm ->
        val result = realm.where(this.javaClass).findAll().sort(fieldName.toTypedArray(), order.toTypedArray())
        return result.loadRange(startPos, endPos)
    }
}

inline fun <reified T : RealmModel> querySorted(fieldName: List<String>, order: List<Sort>, startPos: Int = -1, endPos: Int = -1): List<T> {
    getRealmInstance<T>().use { realm ->
        val result = realm.where(T::class.java).findAll().sort(fieldName.toTypedArray(), order.toTypedArray())
        return result.loadRange(startPos, endPos)
    }
}

/**
 * Utility extension for modifying database. Create a transaction, run the function passed as argument,
 * commit transaction and close realm instance.
 */
fun Realm.transaction(action: (Realm) -> Unit) {
    use {
        if(!isInTransaction) {
            executeTransaction { action(this) }
        } else {
            action(this)
        }
    }
}

/**
 * Utility extension for modifying database. Create a transaction, run the function passed as argument,
 * and commit transaction.
 */
fun Realm.transactionManaged(action: (Realm) -> Unit) {
    if(!isInTransaction) {
        executeTransaction { action(this) }
    } else {
        action(this)
    }
}

fun executeTransaction(realm: Realm = Realm.getDefaultInstance(), transaction: (Realm) -> Unit) {
    realm.use {
        realm.executeTransaction { transaction(it) }
    }
}

/**
 * Creates a new entry in database. Usefull for RealmObject with no primary key.
 */
fun <T : RealmModel> T.create() {
    getRealmInstance().transaction { it.copyToRealm(this) }
}

/**
 * Creates a new entry in database. Useful for RealmObject with no primary key.
 * @return a managed version of a saved object
 */
fun <T : RealmModel> T.createManaged(realm: Realm): T {
    var result: T? = null
    realm.transactionManaged { result = it.copyToRealm(this) }
    return result!!
}

/**
 * Creates or updates a entry in database. Requires a RealmObject with primary key, or IllegalArgumentException will be thrown
 */
fun <T : RealmModel> T.createOrUpdate() {
    getRealmInstance().transaction { it.copyToRealmOrUpdate(this) }
}

/**
 * Creates or updates a entry in database. Requires a RealmObject with primary key, or IllegalArgumentException will be thrown
 * @return a managed version of a saved object
 */
fun <T : RealmModel> T.createOrUpdateManaged(realm: Realm): T {
    var result: T? = null
    realm.transactionManaged { result = it.copyToRealmOrUpdate(this) }
    return result!!
}

/**
 * Creates a new entry in database or updates an existing one. If entity has no primary key, always create a new one.
 * If has primary key, it tries to updates an existing one.
 */
fun <T : RealmModel> T.save() {
    getRealmInstance().transaction { realm ->
        if(isAutoIncrementPK()) {
            initPk(realm)
        }
        if(this.hasPrimaryKey(realm)) realm.copyToRealmOrUpdate(this) else realm.copyToRealm(this)
    }
}

/**
 * Creates a new entry in database or updates an existing one. If entity has no primary key, always create a new one.
 * If has primary key, it tries to update an existing one.
 * @return a managed version of a saved object
 */
inline fun <reified T : RealmModel> T.saveManaged(realm: Realm): T {
    var result: T? = null
    realm.transactionManaged {
        if(isAutoIncrementPK()) {
            initPk(realm)
        }

        result = if(this.hasPrimaryKey(it)) it.copyToRealmOrUpdate(this) else it.copyToRealm(this)
    }
    return result!!
}

inline fun <reified D : RealmModel, T : Collection<D>> T.saveAll() {
    if(size > 0) {
        getRealmInstance().transaction { realm ->
            if(first().isAutoIncrementPK()) {
                initPk(realm)
            }
            forEach { if(it.hasPrimaryKey(realm)) realm.copyToRealmOrUpdate(it) else realm.copyToRealm(it) }
        }
    }
}

inline fun <reified T : RealmModel> Collection<T>.saveAllManaged(realm: Realm): List<T> {
    val results = mutableListOf<T>()
    realm.transactionManaged {
        if(first().isAutoIncrementPK()) {
            initPk(realm)
        }
        forEach { results += if(it.hasPrimaryKey(realm)) realm.copyToRealmOrUpdate(it) else realm.copyToRealm(it) }
    }
    return results
}

inline fun <reified D : RealmModel> Array<D>.saveAll() {
    getRealmInstance().transaction { realm ->
        if(first().isAutoIncrementPK()) {
            initPk(realm)
        }
        forEach { if(it.hasPrimaryKey(realm)) realm.copyToRealmOrUpdate(it) else realm.copyToRealm(it) }
    }
}

inline fun <reified T : RealmModel> Array<T>.saveAllManaged(realm: Realm): List<T> {
    val results = mutableListOf<T>()
    realm.transactionManaged {
        if(first().isAutoIncrementPK()) {
            initPk(realm)
        }
        forEach { results += if(it.hasPrimaryKey(realm)) realm.copyToRealmOrUpdate(it) else realm.copyToRealm(it) }
    }
    return results
}

/**
 * Delete all entries of this type in database
 */
fun <T : RealmModel> T.deleteAll() {
    getRealmInstance().transaction { it.where(this.javaClass).findAll().deleteAllFromRealm() }
}

inline fun <reified T : RealmModel> deleteAll() {
    getRealmInstance<T>().transaction { it.where(T::class.java).findAll().deleteAllFromRealm() }
}

/**
 * Delete all entries returned by the specified query
 */
fun <T : RealmModel> T.delete(myQuery: Query<T>) {
    getRealmInstance().transaction {
        it.where(this.javaClass).withQuery(myQuery).findAll().deleteAllFromRealm()
    }
}

inline fun <reified T : RealmModel> delete(crossinline query: Query<T>) {
    getRealmInstance<T>().transaction {
        it.where(T::class.java).runQuery(query).findAll().deleteAllFromRealm()
    }
}

/**
 * Update first entry returned by the specified query
 */
inline fun <reified T : RealmModel> T.queryAndUpdate(noinline query: Query<T>, noinline modify: (T) -> Unit) {
    queryFirst(query).let {
        modify(this)
        save()
    }
}

/**
 * Use RealmQuery instance as a parameter to query the number of eligible elements in the database
 */
fun <T : RealmModel> T.count(query: Query<T>? = null): Long = count(null, query, this.javaClass)

/**
 * Use RealmQuery instance as a parameter to query the number of eligible elements in the database
 */
inline fun <reified T : RealmModel> count(noinline query: Query<T>? = null): Long = count(null, query, T::class.java)

fun <T : RealmModel> T.count(realm: Realm, query: Query<T>? = null): Long = count(realm, query, this.javaClass)

inline fun <reified T : RealmModel> count(realm: Realm, noinline query: Query<T>? = null): Long = count(realm, query, T::class.java)

@PublishedApi
internal fun <T : RealmModel> count(realm: Realm? = null, query: Query<T>? = null, javaClass: Class<T>): Long {
    fun action(r: Realm): Long {
        val realmQuery = r.where(javaClass)
        query?.invoke(realmQuery)
        return realmQuery.count()
    }
    if(null != realm){
        return action(realm)
    }
    getRealmInstance(javaClass).use { r ->
        return action(r)
    }
}

/**
 * UTILITY METHODS
 */
private fun <T> T.withQuery(block: (T) -> Unit): T {
    block(this); return this
}

/**
 * UTILITY METHODS
 */
inline fun <reified T : RealmModel> RealmQuery<T>.runQuery(block: RealmQuery<T>.() -> Unit): RealmQuery<T> {
    block(this); return this
}

fun <T : RealmModel> T.hasPrimaryKey(realm: Realm): Boolean {
    if(realm.schema.get(this.javaClass.simpleName) == null) {
        throw IllegalArgumentException(this.javaClass.simpleName + " is not part of the schema for this Realm. Did you added realm-android plugin in your build.gradle.kts file?")
    }
    return realm.schema.get(this.javaClass.simpleName)?.hasPrimaryKey()
        ?: false
}

inline fun <reified T : RealmModel> hasPrimaryKey(realm: Realm): Boolean {
    val simpleName = T::class.java.simpleName
    if(realm.schema[simpleName] == null) {
        throw IllegalArgumentException("$simpleName is not part of the schema for this Realm. Did you add realm-android plugin in your build.gradle.kts file?")
    }
    return realm.schema[simpleName]?.hasPrimaryKey()
        ?: false
}

inline fun <reified T : RealmModel> T.getLastPk(realm: Realm): Long {
    return getPrimaryKeyFieldName(realm)?.let { fieldName ->
        val result = realm.where(this.javaClass).max(fieldName)
        result?.toLong()
            ?: 0
    }
        ?: 0
}

inline fun <reified T : RealmModel> getLastPk(realm: Realm): Long {
    return getPrimaryKeyFieldName<T>(realm)?.let { fieldName ->
        val result = realm.where(T::class.java).max(fieldName)
        result?.toLong()
            ?: 0
    }
        ?: 0
}

inline fun <reified T : RealmModel> T.getPrimaryKeyFieldName(realm: Realm): String? {
    return realm.schema.get(this.javaClass.simpleName)?.primaryKey
}

inline fun <reified T : RealmModel> getPrimaryKeyFieldName(realm: Realm): String? {
    return realm.schema.get(T::class.java.simpleName)?.primaryKey
}

inline fun <reified T : RealmModel> T.setPk(realm: Realm, value: Long) {
    getPrimaryKeyFieldName(realm)?.let { fieldName ->
        val f1 = javaClass.getDeclaredField(fieldName)
        if (!f1.type.isInstance(1L)) {
            throw IllegalArgumentException("Primary key field $fieldName must be of type Long to set a primary key automatically")
        }
        f1.safeAccess {
            if (it.isInValidLongPk(this)) {
                //We only set pk value if it does not have any value previously
                it.set(this, value)
            }
        }
    }
}

fun Collection<RealmModel>.initPk(realm: Realm) {
    val nextPk = first().getLastPk(realm) + 1
    for((index, value) in withIndex()) {
        value.setPk(realm, nextPk + index)
    }
}

fun Array<out RealmModel>.initPk(realm: Realm) {
    val nextPk = first().getLastPk(realm) + 1
    for((index, value) in withIndex()) {
        value.setPk(realm, nextPk + index)
    }
}

fun RealmModel.initPk(realm: Realm) {
    setPk(realm, getLastPk(realm) + 1)
}

fun <T : RealmModel> T.isAutoIncrementPK(): Boolean {
    return this.javaClass.declaredAnnotations.any { it.annotationClass == AutoIncrementPK::class }
}

fun <T> RealmQuery<T>.equalToValue(fieldName: String, value: Int) = equalTo(fieldName, value)
fun <T> RealmQuery<T>.equalToValue(fieldName: String, value: Long) = equalTo(fieldName, value)

fun Field.isNullFor(obj: Any) = try {
    get(obj) == null
} catch(ex: NullPointerException) {
    true
}

fun Field.isInValidLongPk(obj: Any) = isNullFor(obj) || get(obj) == Long.MIN_VALUE

fun <T : RealmModel> RealmResults<T>.loadRange(startPos: Int, endPos: Int): List<T> {
    if (startPos < 0) {
        return realm.copy(this)
    }
    if (startPos > endPos) {
        throw IllegalArgumentException("startPos($startPos) > endPos($endPos)")
    }
    val range = mutableListOf<T>()
    for (pos in startPos until endPos) {
        val t = getOrNull(pos) ?: break
        range.add(t)
    }
    return realm.copy(range)
}
