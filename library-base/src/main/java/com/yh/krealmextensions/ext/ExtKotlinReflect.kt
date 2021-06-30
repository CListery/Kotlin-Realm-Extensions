package com.yh.krealmextensions.ext

import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Field

fun <T : Any> Class<T>.safeCreator(
    parameterTypes: List<Class<*>> = emptyList(),
    params: List<Any> = emptyList()
): T? {
    return getDeclaredConstructor(*parameterTypes.toTypedArray()).safeNew(params)
}

fun <T : Any> Constructor<T>.safeNew(params: List<Any> = emptyList()): T? {
    return if (!params.isNullOrEmpty()) {
        safeAccess { it.newInstance(*params.toTypedArray()) }
    } else {
        safeAccess { it.newInstance() }
    }
}

inline fun <reified R : Any> Class<*>.safeFieldGet(
    fieldName: String,
    obj: Any? = null
): R? {
    return getDeclaredField(fieldName).safeGet(obj)
}

fun <R : Any> Class<*>.safeFieldGet(
    fieldName: String,
    fieldClazz: Class<R>,
    obj: Any? = null
): R? {
    return getDeclaredField(fieldName).safeGet(fieldClazz, obj)
}

fun <R : Any> Field.safeGet(clazzR: Class<R>, obj: Any?): R? {
    return safeAccess {
        val result = it.get(obj)
        if (clazzR.isInstance(result)) {
            @Suppress("UNCHECKED_CAST")
            return@safeAccess result as R
        }
        return@safeAccess null
    }
}

inline fun <reified R : Any> Field.safeGet(obj: Any?): R? {
    return this.safeAccess {
        val result = it.get(obj)
        if (result is R) {
            return@safeAccess result
        }
        return@safeAccess null
    }
}

fun <T : AccessibleObject, R : Any> T.safeAccess(block: (T) -> R?): R? {
    val originAccessible = isAccessible
    isAccessible = true
    val result = block.invoke(this)
    isAccessible = originAccessible
    return result
}
