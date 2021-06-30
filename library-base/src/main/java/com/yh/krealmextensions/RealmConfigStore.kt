package com.yh.krealmextensions

import android.util.Log
import com.yh.krealmextensions.ext.printString
import com.yh.krealmextensions.ext.safeCreator
import com.yh.krealmextensions.ext.safeFieldGet
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.RealmModule
import io.realm.internal.RealmProxyMediator
import java.util.HashSet

private const val TAG = "realm-ext"

object RealmConfigManager {

    private val mConfigs = HashMap<Class<out RealmModel>, RealmConfiguration>()

    private val mDefaultConfig by lazy {
        RealmConfiguration.Builder()
            .allowWritesOnUiThread(isEnableUiThreadOption)
            .allowQueriesOnUiThread(isEnableUiThreadOption)
            .build()
    }

    var isEnableUiThreadOption = true
    var isDebugMode = false

    /**
     * Initialize realm configuration for class
     */
    fun <T : RealmModel> init(modelClass: Class<T>, builder: RealmConfiguration.Builder) {
        init(modelClass, builder.applyUiThreadOption(isEnableUiThreadOption).build())
    }

    private fun <T : RealmModel> init(modelClass: Class<T>, config: RealmConfiguration) {
        mConfigs[modelClass] = config
        if (isDebugMode) {
            Log.i(TAG, config.printString(" \nmodel: ${modelClass.canonicalName}"))
        }
    }

    fun <T : Any> initModule(moduleClazz: Class<T>, builder: RealmConfiguration.Builder) {
        // check if class of the module
        val moduleAnnotation = moduleClazz.annotations.filterIsInstance<RealmModule>().firstOrNull()

        if (null != moduleAnnotation) {
            if (isDebugMode) Log.i(TAG, "Got annotation in module $moduleAnnotation")
            val allRealmModelClazz = moduleAnnotation.classes
            val realmModelClazz =
                allRealmModelClazz.filter { it.java.interfaces.contains(RealmModel::class.java) }
                    .map {
                        @Suppress("UNCHECKED_CAST")
                        it.java as Class<RealmModel>
                    }.toMutableList()
            val realmObjClazz =
                allRealmModelClazz.filter { it.java.superclass == RealmObject::class.java }
                    .map {
                        @Suppress("UNCHECKED_CAST")
                        it.java as Class<RealmObject>
                    }.toMutableList()
            ensureModule(moduleClazz, realmModelClazz, realmObjClazz, builder)
            realmModelClazz.forEach {
                init(it, builder)
            }
            realmObjClazz.forEach {
                init(it, builder)
            }
        }
    }

    /**
     * 确保 [moduleClazz] 都已添加到 [RealmConfiguration.Builder.addModule]
     */
    private fun <T : Any> ensureModule(
        moduleClazz: Class<T>,
        realmModelClazz: MutableList<Class<RealmModel>>,
        realmObjClazz: MutableList<Class<RealmObject>>,
        builder: RealmConfiguration.Builder
    ): Boolean {
        val moduleObj = RealmConfiguration.Builder::class.java.safeFieldGet<HashSet<Any>>("modules", builder)
            ?: throw IllegalArgumentException("RealmConfiguration.Builder.modules Type is changed, Please up-level API")

        val needInsertModule = checkDefaultModule(moduleObj, builder, realmModelClazz, realmObjClazz)

        if (needInsertModule && moduleObj.filterIsInstance(moduleClazz).isNullOrEmpty()) {
            val module = moduleClazz.safeCreator()
            if (module != null) {
                builder.addModule(module)
                if (isDebugMode) Log.i(TAG, "load: ${moduleClazz.simpleName}")
            } else {
                Log.e(TAG, "fail: ${moduleClazz.simpleName}")
            }
        } else {
            if (isDebugMode) Log.i(TAG, "ignore: ${moduleClazz.simpleName}")
        }
        return needInsertModule
    }

    /**
     * 检查是否包含 [RealmConfiguration.DEFAULT_MODULE] 且反向检索 Model、Obj 的 Class.
     * 可能会修改 [realmModelClazz] 和 [realmObjClazz]
     */
    private fun checkDefaultModule(
        moduleObj: HashSet<Any>,
        builder: RealmConfiguration.Builder,
        realmModelClazz: MutableList<Class<RealmModel>>,
        realmObjClazz: MutableList<Class<RealmObject>>
    ): Boolean {
        val defaultModuleObj = RealmConfiguration::class.java.safeFieldGet<Any>("DEFAULT_MODULE")
        val defaultModuleMediatorObj = RealmConfiguration::class.java.safeFieldGet<RealmProxyMediator>("DEFAULT_MODULE_MEDIATOR")
        if (defaultModuleObj != null && defaultModuleMediatorObj is RealmProxyMediator) {
            if (!moduleObj.contains(defaultModuleObj)) {
                builder.addModule(defaultModuleObj)
            }
            val hasAllModel = defaultModuleMediatorObj.modelClasses.containsAll(realmModelClazz)
            val hasAllObj = defaultModuleMediatorObj.modelClasses.containsAll(realmObjClazz)
            if (hasAllModel && hasAllObj) {
                val defAllRealmModelClazz = defaultModuleMediatorObj.modelClasses
                val defRealmModelClazz = defAllRealmModelClazz.filter {
                    !realmModelClazz.contains(it) && it.interfaces.contains(RealmModel::class.java)
                }.map {
                    @Suppress("UNCHECKED_CAST")
                    it as Class<RealmModel>
                }
                if (defRealmModelClazz.isNotEmpty()) {
                    realmModelClazz.addAll(defRealmModelClazz)
                    if(isDebugMode) Log.i(TAG, "find: ${defRealmModelClazz.map { it.simpleName }}")
                }
                val defRealmObjClazz = defAllRealmModelClazz.filter {
                    !realmObjClazz.contains(it) && it.superclass == RealmObject::class.java
                }.map {
                    @Suppress("UNCHECKED_CAST")
                    it as Class<RealmObject>
                }
                if (defRealmObjClazz.isNotEmpty()) {
                    realmObjClazz.addAll(defRealmObjClazz)
                    if(isDebugMode) Log.i(TAG, "find: ${defRealmObjClazz.map { it.simpleName }}")
                }
                return false
            }
        }
        return true
    }

    /**
     * Fetches realm configuration for class.
     */
    fun <T : RealmModel> findConfig(modelClass: Class<T>): RealmConfiguration {
        return mConfigs.getOrPut(modelClass, { mDefaultConfig })
    }

}

fun <T : RealmModel> T.getRealmInstance(): Realm {
    return RealmConfigManager.findConfig(this::class.java).realm()
}

fun <T : RealmModel> getRealmInstance(clazz: Class<T>): Realm {
    return RealmConfigManager.findConfig(clazz).realm()
}

inline fun <reified D : RealmModel, T : Collection<D>> T.getRealmInstance(): Realm {
    return RealmConfigManager.findConfig(D::class.java).realm()
}

inline fun <reified T : RealmModel> getRealmInstance(): Realm {
    return RealmConfigManager.findConfig(T::class.java).realm()
}

inline fun <reified D : RealmModel> Array<D>.getRealmInstance(): Realm {
    return RealmConfigManager.findConfig(D::class.java).realm()
}

fun RealmConfiguration.realm(): Realm {
    return Realm.getInstance(this)
}
