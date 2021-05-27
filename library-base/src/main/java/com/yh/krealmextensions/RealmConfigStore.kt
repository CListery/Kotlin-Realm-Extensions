package com.yh.krealmextensions

import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.RealmModule

object RealmConfigManager {
    
    private val TAG = this::class.java.simpleName
    
    private val mConfigs = HashMap<Class<out RealmModel>, RealmConfiguration>()
    
    private val mDefaultConfig by lazy {
        RealmConfiguration.Builder()
            .allowWritesOnUiThread(isEnableUiThreadOption)
            .allowQueriesOnUiThread(isEnableUiThreadOption)
            .build()
    }
    
    var isEnableUiThreadOption = true
    
    /**
     * Initialize realm configuration for class
     */
    fun <T : RealmModel> init(modelClass: Class<T>, builder: RealmConfiguration.Builder) {
        mConfigs[modelClass] = builder.applyUiThreadOption(isEnableUiThreadOption).build()
        Log.d(TAG, "Adding class $modelClass to realm ${mConfigs[modelClass]?.realmFileName}")
    }
    
    fun <T : Any> initModule(cls: Class<T>, builder: RealmConfiguration.Builder) {
        // check if class of the module
        val moduleAnnotation = cls.annotations.filterIsInstance<RealmModule>().firstOrNull()
        
        if(null != moduleAnnotation) {
            Log.i(TAG, "Got annotation in module $moduleAnnotation")
            val realmModelClazz = moduleAnnotation.classes
            realmModelClazz.filter { it.java.interfaces.contains(RealmModel::class.java) }
                .map {
                    it.java as Class<RealmModel>
                }
                .forEach {
                    init(it, builder)
                }
            realmModelClazz.filter { it.java.superclass == RealmObject::class.java }
                .map {
                    it.java as Class<RealmObject>
                }
                .forEach {
                    init(it, builder)
                }
        }
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
