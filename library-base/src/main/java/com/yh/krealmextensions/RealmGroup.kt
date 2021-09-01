package com.yh.krealmextensions

import com.kotlin.safeGet
import io.realm.RealmModel
import io.realm.Sort
import java.lang.reflect.Field


/**
 * field supported types for group-by's
 */
@PublishedApi
internal val GROUP_SUPPORT_TYPE = arrayOf(
    Int::class.java,
    Long::class.java,
    Short::class.java,
    String::class.java
)

/**
 * Query the unique group-by set in a given range
 */
fun <T : RealmModel, K : Any> T.distinctGroup(
    groupFieldName: String,
    groupFieldClazz: Class<K>,
    startPos: Int = -1,
    endPos: Int = -1,
    sortFieldName: String? = null,
    sort: Sort = Sort.ASCENDING,
    query: Query<T>? = null
): List<T> {
    val groupField = this.javaClass.getDeclaredField(groupFieldName)
    check(groupField, groupFieldClazz, startPos, endPos)
    return internalDistinctGroup(
        this.javaClass,
        groupFieldName,
        startPos,
        endPos,
        sortFieldName,
        sort,
        query
    )
}

/**
 * Query the unique group-by set in a given range
 */
inline fun <reified T : RealmModel, reified K : Any> distinctGroup(
    groupFieldName: String,
    startPos: Int = -1,
    endPos: Int = -1,
    sortFieldName: String? = null,
    sort: Sort = Sort.ASCENDING,
    noinline query: Query<T>? = null
): List<T> {
    val groupField = T::class.java.getDeclaredField(groupFieldName)
    check(groupField, K::class.java, startPos, endPos)
    return internalDistinctGroup(
        T::class.java,
        groupFieldName,
        startPos,
        endPos,
        sortFieldName,
        sort,
        query
    )
}

@PublishedApi
internal fun <T : RealmModel> internalDistinctGroup(
    tClazz: Class<T>,
    groupFieldName: String,
    startPos: Int,
    endPos: Int,
    sortFieldName: String? = null,
    sort: Sort = Sort.ASCENDING,
    query: Query<T>? = null
): List<T> {
    getRealmInstance(tClazz).use { realm ->
        val distinctQuery = realm.where(tClazz)
        query?.invoke(distinctQuery)
        if (!sortFieldName.isNullOrEmpty()) {
            distinctQuery.sort(sortFieldName, sort)
        }
        distinctQuery.distinct(groupFieldName)
        val distinctCount = distinctQuery.count()
        if (distinctCount <= 0) {
            return emptyList()
        }
        if (startPos > distinctCount) {
            return emptyList()
        }
        val distinctResult = distinctQuery.findAll()
        return distinctResult.loadRange(startPos, endPos)
    }
}

/**
 * Specify [groupFieldName] to group and take out the mapping set in the given range
 */
fun <T : RealmModel, K : Any> T.groupBy(
    groupFieldName: String,
    groupFieldClazz: Class<K>,
    startPos: Int = -1,
    endPos: Int = -1,
    sortFieldName: String? = null,
    sort: Sort = Sort.ASCENDING,
    query: Query<T>? = null
): Map<K, List<T>> {
    val groupField = this.javaClass.getDeclaredField(groupFieldName)
    check(groupField, groupFieldClazz, startPos, endPos)
    return internalGroupBy(
        this.javaClass,
        groupFieldName,
        startPos,
        endPos,
        groupField,
        groupFieldClazz,
        sortFieldName,
        sort,
        query
    )
}

/**
 * Specify [groupFieldName] to group and take out the mapping set in the given range
 */
inline fun <reified T : RealmModel, reified K : Any> groupBy(
    groupFieldName: String,
    startPos: Int = -1,
    endPos: Int = -1,
    sortFieldName: String? = null,
    sort: Sort = Sort.ASCENDING,
    noinline query: Query<T>? = null
): Map<K, List<T>> {
    val groupField = T::class.java.getDeclaredField(groupFieldName)
    check(groupField, K::class.java, startPos, endPos)
    return internalGroupBy(
        T::class.java,
        groupFieldName,
        startPos,
        endPos,
        groupField,
        K::class.java,
        sortFieldName,
        sort,
        query
    )
}

@PublishedApi
internal fun <T : RealmModel, K : Any> internalGroupBy(
    tClazz: Class<T>,
    groupFieldName: String,
    startPos: Int,
    endPos: Int,
    groupField: Field,
    groupFieldClazz: Class<K>,
    sortFieldName: String? = null,
    sort: Sort = Sort.ASCENDING,
    query: Query<T>? = null
): Map<K, List<T>> {
    getRealmInstance(tClazz).use { realm ->
        val rangeDistinctResult =
            internalDistinctGroup(tClazz, groupFieldName, startPos, endPos, sortFieldName, sort)
        if (rangeDistinctResult.isEmpty()) {
            return emptyMap()
        }
        val rangeDistinctGroupKeys =
            rangeDistinctResult.mapNotNull { groupField.safeGet(groupFieldClazz, it) }
        return rangeDistinctGroupKeys.map {
            val groupQuery = realm.where(tClazz)
            query?.invoke(groupQuery)
            if (!sortFieldName.isNullOrEmpty()) {
                groupQuery.sort(sortFieldName, sort)
            }
            when (it) {
                is Int -> groupQuery.equalTo(groupFieldName, it)
                is Long -> groupQuery.equalTo(groupFieldName, it)
                is Short -> groupQuery.equalTo(groupFieldName, it)
                is String -> groupQuery.equalTo(groupFieldName, it)
                else -> TODO()
            }
            it to realm.copy(groupQuery.findAll())
        }.toMap()
    }
}

@PublishedApi
internal fun <K : Any> check(
    groupField: Field,
    fieldClazz: Class<K>,
    startPos: Int,
    endPos: Int
) {
    if (!GROUP_SUPPORT_TYPE.contains(fieldClazz)) {
        throw IllegalArgumentException("group field type only be ${GROUP_SUPPORT_TYPE.map { it.simpleName }}")
    }
    if (startPos > endPos) {
        throw IllegalArgumentException("startPos($startPos) > endPos($endPos)")
    }
    if (fieldClazz != groupField.type) {
        throw IllegalArgumentException("group field type is ${groupField.type} not is ${fieldClazz.simpleName}")
    }
}
